package com.hotelcaciquebolivar.reservas.service.reserva.customer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerTipoRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.DisponibilidadPorTipoResponse;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.exception.reserva.ReservasBloqueadasException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.TipoHabitacionRepository;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;
import com.hotelcaciquebolivar.reservas.service.reserva.bloqueo.BloqueoReservaService;
import com.hotelcaciquebolivar.reservas.service.reserva.crud.ReservaCrudService;
import com.hotelcaciquebolivar.reservas.service.reserva.customer.assignment.RoomAssignmentStrategy;

/**
 * Implementación del servicio de reservas para clientes (frontend público).
 * Gestiona la creación de reservas web con asignación automática de habitaciones,
 * consulta de disponibilidad por tipo y consulta de precios efectivos.
 * Valida fechas, bloqueos de reserva y capacidades. Las reservas web quedan
 * en estado PENDIENTE_PAGO con ventana de expiración automática.
 */
@Service
@Transactional
public class CustomerReservationServiceImpl implements CustomerReservationService {

    private static final int MAX_DIAS_ESTADIA = 30;

    private final ClienteRepository clienteRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HabitacionRepository habitacionRepository;
    private final RoomAssignmentStrategy assignmentStrategy;
    private final ReservaCrudService reservaCrudService;
    private final BloqueoReservaService bloqueoReservaService;
    private final PrecioTemporalService precioTemporalService;

    public CustomerReservationServiceImpl(
            ClienteRepository clienteRepository,
            TipoHabitacionRepository tipoHabitacionRepository,
            HabitacionRepository habitacionRepository,
            RoomAssignmentStrategy assignmentStrategy,
            ReservaCrudService reservaCrudService,
            BloqueoReservaService bloqueoReservaService,
            PrecioTemporalService precioTemporalService) {
        this.clienteRepository = clienteRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
        this.assignmentStrategy = assignmentStrategy;
        this.reservaCrudService = reservaCrudService;
        this.bloqueoReservaService = bloqueoReservaService;
        this.precioTemporalService = precioTemporalService;
    }

    /**
     * {@inheritDoc}
     * Valida fechas, bloqueos, capacidad y disponibilidad. Asigna habitaciones
     * automáticamente usando la estrategia configurada. Crea la reserva con origen WEB
     * en estado PENDIENTE_PAGO. El frontend debe mostrar countdown de expiración.
     */
    @Override
    public CustomerReservationResponse crear(CustomerReservationRequest request) {
        validarFechas(request.fechaCheckIn(), request.fechaCheckOut());
        validarBloqueo(request.fechaCheckIn(), request.fechaCheckOut());

        Cliente titular = clienteRepository.findById(request.titularId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.titularId()));

        List<DetalleReservaRequest> detalles = new ArrayList<>();
        validarTotalOcupantes(request);

        for (CustomerTipoRequest tipoReq : request.tiposHabitacion()) {
            TipoHabitacion tipo = tipoHabitacionRepository.findById(tipoReq.tipoHabitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion", tipoReq.tipoHabitacionId()));

            if (tipoReq.ocupantes() > tipo.getCapacidadMaxima()) {
                throw new ValidationException("ocupantes",
                        "La capacidad m" + "\u00e1" + "xima del tipo " + tipo.getNombre()
                        + " es " + tipo.getCapacidadMaxima() + " hu" + "\u00e9" + "spedes");
            }

            List<Habitacion> disponibles = habitacionRepository.findDisponiblesPorTipo(
                    tipoReq.tipoHabitacionId(),
                    request.fechaCheckIn(),
                    request.fechaCheckOut());

            if (disponibles.size() < tipoReq.cantidad()) {
                throw new ValidationException("disponibilidad",
                        "No hay suficientes habitaciones disponibles del tipo \"" + tipo.getNombre()
                        + "\" para las fechas seleccionadas. Actualmente no hay disponibilidad.");
            }

            List<Habitacion> seleccionadas = assignmentStrategy.asignar(disponibles, tipoReq.cantidad());

            for (Habitacion h : seleccionadas) {
                detalles.add(new DetalleReservaRequest(
                        h.getId(),
                        tipo.getPrecioBase(),
                        tipoReq.ocupantes(),
                        null,
                        null));
            }
        }

        ReservaRequest reservaRequest = new ReservaRequest(
                request.titularId(),
                request.fechaCheckIn(),
                request.fechaCheckOut(),
                OrigenReserva.WEB,
                request.observaciones(),
                detalles);

        ReservaResponse creada = reservaCrudService.crear(reservaRequest);

        // WEB reservas quedan en PENDIENTE_PAGO sin email; el backend es fuente de verdad para expiración
        // El frontend debe mostrar countdown usando creada.fechaExpiracionPago()
        return toResponse(creada);
    }

    private void validarTotalOcupantes(CustomerReservationRequest request) {
        int totalOcupantes = request.tiposHabitacion().stream()
                .mapToInt(t -> t.ocupantes() * t.cantidad())
                .sum();
        if (totalOcupantes != request.cantidadHuespedes()) {
            throw new ValidationException("cantidadHuespedes",
                    "La suma de ocupantes (" + totalOcupantes
                    + ") no coincide con la cantidad de hu" + "\u00e9" + "spedes declarada ("
                    + request.cantidadHuespedes() + ")");
        }
    }

    private void validarBloqueo(LocalDate checkIn, LocalDate checkOut) {
        bloqueoReservaService.encontrarBloqueoParaRango(checkIn, checkOut)
                .ifPresent(bloqueo -> {
                    throw new ReservasBloqueadasException(bloqueo.getMotivo());
                });
    }

    private void validarFechas(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new ValidationException("fechas", "Las fechas de check-in y check-out son obligatorias");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("fechaCheckIn", "La fecha de check-in no puede ser anterior a hoy");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("fechaCheckOut", "La fecha de check-out debe ser posterior al check-in");
        }
        long noches = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (noches < 1) {
            throw new ValidationException("fechas", "La estad" + "\u00ed" + "a debe ser de al menos 1 noche");
        }
        if (noches > MAX_DIAS_ESTADIA) {
            throw new ValidationException("fechas",
                    "La estad" + "\u00ed" + "a no puede exceder " + MAX_DIAS_ESTADIA + " d" + "\u00ed" + "as");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadPorTipoResponse> consultarDisponibilidadPorTipo(LocalDate checkIn, LocalDate checkOut) {
        validarFechas(checkIn, checkOut);
        validarBloqueo(checkIn, checkOut);

        LocalDate queryCheckOut = checkIn.equals(checkOut) ? checkOut.plusDays(1) : checkOut;

        List<TipoHabitacion> tipos = tipoHabitacionRepository.findAll();
        List<DisponibilidadPorTipoResponse> resultado = new ArrayList<>();

        for (TipoHabitacion tipo : tipos) {
            long total = habitacionRepository.countActivasPorTipo(tipo.getId());
            List<Habitacion> disponibles = habitacionRepository.findDisponiblesPorTipo(
                    tipo.getId(), checkIn, queryCheckOut);

            java.math.BigDecimal precioNoche = precioTemporalService.resolverPrecioNoche(tipo, checkIn);
            // si la estadía cruza temporadas, mostrar precio de primera noche como referencia;
            // total estimado se calcula noche a noche
            java.math.BigDecimal precioExtra = precioTemporalService.resolverPrecioPersonaExtra(tipo, checkIn);
            java.math.BigDecimal totalEstimado = precioTemporalService.calcularTotalNoches(tipo, null, checkIn, checkOut);

            resultado.add(new DisponibilidadPorTipoResponse(
                    tipo.getId(),
                    disponibles.size(),
                    (int) total,
                    precioNoche,
                    precioExtra,
                    totalEstimado));
        }

        return resultado;
    }

    @Override
    public com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse consultarPrecioEfectivo(Long tipoId, LocalDate checkIn, LocalDate checkOut) {
        validarFechas(checkIn, checkOut);
        TipoHabitacion tipo = tipoHabitacionRepository.findById(tipoId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion", tipoId));
        long noches = ChronoUnit.DAYS.between(checkIn, checkOut);
        java.util.List<com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse.DetalleNoche> desglose = new java.util.ArrayList<>();
        for (LocalDate f = checkIn; f.isBefore(checkOut); f = f.plusDays(1)) {
            desglose.add(new com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse.DetalleNoche(
                    f, precioTemporalService.resolverPrecioNoche(tipo, f), precioTemporalService.resolverPrecioPersonaExtra(tipo, f)));
        }
        java.math.BigDecimal totalNoches = precioTemporalService.calcularTotalNoches(tipo, null, checkIn, checkOut);
        java.math.BigDecimal totalExtraPorPersona = precioTemporalService.calcularTotalPersonasExtra(tipo, null, 1, checkIn, checkOut);
        return new com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse(
                tipo.getId(), tipo.getNombre(), checkIn, checkOut, noches, tipo.getPrecioBase(), tipo.getPrecioPersonaExtra(), desglose, totalNoches, totalExtraPorPersona);
    }

    private CustomerReservationResponse toResponse(ReservaResponse r) {
        List<CustomerReservationResponse.HabitacionAsignada> habitaciones = r.detalles().stream()
                .map(d -> new CustomerReservationResponse.HabitacionAsignada(
                d.id(),
                d.habitacionNumero(),
                d.precioPorNoche(),
                d.cantidadOcupantes()))
                .toList();

        return new CustomerReservationResponse(
                r.id(),
                r.codigoReserva(),
                r.fechaCheckIn(),
                r.fechaCheckOut(),
                r.fechaCreacion(),
                r.fechaExpiracionPago(),
                r.noches(),
                r.estado(),
                r.total(),
                r.observaciones(),
                habitaciones);
    }
}
