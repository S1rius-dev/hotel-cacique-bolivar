package com.hotelcaciquebolivar.reservas.service.reserva.rapida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRapidaRequest;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.reserva.RoomNotAvailableException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;
import com.hotelcaciquebolivar.reservas.service.reserva.crud.ReservaCrudServiceImpl;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.CheckInOutPayload;
import com.hotelcaciquebolivar.reservas.sse.payload.ReservaPayload;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservaRapidaServiceImpl implements ReservaRapidaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final HabitacionRepository habitacionRepository;
    private final ReservaMapper reservaMapper;
    private final AuditService auditService;
    private final com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService reservaPricingService;
    private final PrecioTemporalService precioTemporalService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ReservaResponse crearReservaRapida(ReservaRapidaRequest dto) {
        ReservaCrudServiceImpl.validarFechasCreacion(dto.fechaCheckIn(), dto.fechaCheckOut());
        if (dto.huespedesIds().isEmpty()) {
            throw new ValidationException("huespedesIds", "debe haber al menos un huesped");
        }

        List<Cliente> huespedes = dto.huespedesIds().stream()
                .map(id -> clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id)))
                .toList();

        boolean esHoy = dto.fechaCheckIn().equals(LocalDate.now());

        LocalDate queryCheckOut = dto.fechaCheckIn().equals(dto.fechaCheckOut())
                ? dto.fechaCheckOut().plusDays(1) : dto.fechaCheckOut();
        Habitacion habitacion = habitacionRepository.findDisponiblePorIdConLock(
                dto.habitacionId(), dto.fechaCheckIn(), queryCheckOut)
                .orElseThrow(() -> new RoomNotAvailableException(
                habitacionRepository.findById(dto.habitacionId())
                        .map(Habitacion::getNumero)
                        .orElse(String.valueOf(dto.habitacionId())),
                dto.fechaCheckIn(), dto.fechaCheckOut()));

        ReservaCrudServiceImpl.validarCapacidad(dto.cantidadOcupantes(), habitacion);

        Cliente titular = huespedes.get(0);

        if (esHoy) {
            Reserva reserva = Reserva.builder()
                    .titularReserva(titular)
                    .fechaCheckIn(dto.fechaCheckIn())
                    .fechaCheckOut(dto.fechaCheckOut())
                    .origen(OrigenReserva.RECEPCION)
                    .estado(EstadoReserva.EN_CURSO)
                    .build();

            DetalleReserva detalle = DetalleReserva.builder()
                    .habitacion(habitacion)
                    .precioPorNoche(precioTemporalService.resolverPrecioNoche(habitacion.getTipo(), dto.fechaCheckIn()))
                    .cantidadOcupantes(dto.cantidadOcupantes())
                    .estado(EstadoDetalleReserva.OCUPADA)
                    .build();

            detalle.validarCapacidad();
            detalle.setReserva(reserva);
            reservaPricingService.congelarPreciosDetalle(detalle, dto.fechaCheckIn(), dto.fechaCheckOut());

            for (Cliente huesped : huespedes) {
                detalle.agregarHuesped(huesped);
            }

            List<DetalleReserva> detalles = new ArrayList<>();
            detalles.add(detalle);
            reserva.setDetalles(detalles);

            reserva.setHoraCheckIn(LocalDateTime.now());
            BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
            reserva.setTotal(fullTotal);
            habitacion.setEstado(EstadoHabitacion.OCUPADA);

            Reserva guardada = reservaRepository.save(reserva);

            auditService.log(AuditAction.CREATE, "Reserva", guardada.getId(),
                    String.format("Reserva rápida + check-in — %s\nHabitación: %s\nTitular: %s %s\nEstadía: %s → %s",
                            guardada.getCodigoReserva(), habitacion.getNumero(),
                            titular.getNombreCompleto(), titular.getApellidoCompleto(),
                            guardada.getFechaCheckIn().format(ReservaCrudServiceImpl.DATE_FMT),
                            guardada.getFechaCheckOut().format(ReservaCrudServiceImpl.DATE_FMT)));

            eventPublisher.publishEvent(new SseDomainEvent(
                    SseEventTypes.RESERVA_CREADA,
                    new ReservaPayload(
                            guardada.getId(),
                            guardada.getCodigoReserva(),
                            titular.getNombreCompleto() + " " + titular.getApellidoCompleto(),
                            guardada.getEstado().name(),
                            guardada.getFechaCheckIn().toString(),
                            guardada.getFechaCheckOut().toString(),
                            guardada.getTotal())));

            eventPublisher.publishEvent(new SseDomainEvent(
                    SseEventTypes.CHECK_IN,
                    new CheckInOutPayload(
                            guardada.getId(),
                            guardada.getCodigoReserva(),
                            "RAPIDO",
                            List.of(habitacion.getNumero()),
                            dto.huespedesIds().size())));

            return reservaMapper.toResponse(guardada);
        }

        Reserva reserva = Reserva.builder()
                .titularReserva(titular)
                .fechaCheckIn(dto.fechaCheckIn())
                .fechaCheckOut(dto.fechaCheckOut())
                .origen(OrigenReserva.RECEPCION)
                .estado(EstadoReserva.PENDIENTE)
                .build();

        DetalleReserva detalle = DetalleReserva.builder()
                .habitacion(habitacion)
                .precioPorNoche(precioTemporalService.resolverPrecioNoche(habitacion.getTipo(), dto.fechaCheckIn()))
                .cantidadOcupantes(dto.cantidadOcupantes())
                .estado(EstadoDetalleReserva.RESERVADA)
                .build();

        detalle.validarCapacidad();
        detalle.setReserva(reserva);
        reservaPricingService.congelarPreciosDetalle(detalle, dto.fechaCheckIn(), dto.fechaCheckOut());

        for (Cliente huesped : huespedes) {
            detalle.agregarHuesped(huesped);
        }

        List<DetalleReserva> detalles = new ArrayList<>();
        detalles.add(detalle);
        reserva.setDetalles(detalles);

        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(fullTotal);
        habitacion.setEstado(EstadoHabitacion.RESERVADA);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.CREATE, "Reserva", guardada.getId(),
                String.format("Reserva rápida (futura) — %s\nHabitación: %s\nTitular: %s %s\nEstadía: %s → %s",
                        guardada.getCodigoReserva(), habitacion.getNumero(),
                        titular.getNombreCompleto(), titular.getApellidoCompleto(),
                        guardada.getFechaCheckIn().format(ReservaCrudServiceImpl.DATE_FMT),
                        guardada.getFechaCheckOut().format(ReservaCrudServiceImpl.DATE_FMT)));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_CREADA,
                new ReservaPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        titular.getNombreCompleto() + " " + titular.getApellidoCompleto(),
                        guardada.getEstado().name(),
                        guardada.getFechaCheckIn().toString(),
                        guardada.getFechaCheckOut().toString(),
                        guardada.getTotal())));

        return reservaMapper.toResponse(guardada);
    }
}
