package com.hotelcaciquebolivar.reservas.service.reserva.crud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.reserva.RoomNotAvailableException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaSpecification;
import com.hotelcaciquebolivar.reservas.mail.EmailService;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaCanceladaData;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.ReservaPayload;

import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio CRUD de reservas.
 * Gestiona la creación, búsqueda, listado, actualización y eliminación de reservas.
 * Incluye validación de fechas, disponibilidad de habitaciones, precios congelados,
 * transiciones de estado, registro de auditoría, envío de notificaciones y eventos SSE.
 * Maneja tanto reservas de recepción como reservas web con expiración de pago automática.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReservaCrudServiceImpl implements ReservaCrudService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final HabitacionRepository habitacionRepository;
    private final DetalleReservaRepository detalleReservaRepository;
    private final PagoRepository pagoRepository;
    private final ReservaMapper reservaMapper;
    private final PageMapper pageMapper;
    private final AuditService auditService;
    private final com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService reservaPricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;
    private final ReservationProperties reservationProperties;

    /**
     * {@inheritDoc}
     * Valida fechas, verifica disponibilidad de habitaciones, congela precios,
     * calcula el total, registra auditoría y envía notificaciones.
     * Para reservas web, establece ventana de expiración de pago automáticamente.
     */
    @Override
    public ReservaResponse crear(ReservaRequest dto) {
        validarFechasCreacion(dto.fechaCheckIn(), dto.fechaCheckOut());

        Cliente titular = clienteRepository.findById(dto.titularReservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Titular", dto.titularReservaId()));

        List<DetalleReserva> detalles = construirDetalles(
                dto.detalles(), dto.fechaCheckIn(), dto.fechaCheckOut());

        OrigenReserva origen = dto.origen() != null ? dto.origen() : OrigenReserva.RECEPCION;
        EstadoReserva estadoInicial = origen == OrigenReserva.WEB ? EstadoReserva.PENDIENTE_PAGO : EstadoReserva.PENDIENTE;
        Reserva reserva = Reserva.builder()
                .titularReserva(titular)
                .fechaCheckIn(dto.fechaCheckIn())
                .fechaCheckOut(dto.fechaCheckOut())
                .origen(origen)
                .estado(estadoInicial)
                .observaciones(dto.observaciones())
                .build();
        // Backend fuente de verdad para expiración: solo WEB tiene ventana de pago
        if (estadoInicial == EstadoReserva.PENDIENTE_PAGO) {
            int ttl = reservationProperties.getPaymentExpirationMinutes();
            LocalDateTime base = reserva.getFechaCreacion() != null ? reserva.getFechaCreacion() : LocalDateTime.now();
            reserva.setFechaExpiracionPago(base.plusMinutes(ttl));
        }

        detalles.forEach(d -> {
            d.setReserva(reserva);
            reservaPricingService.congelarPreciosDetalle(d, dto.fechaCheckIn(), dto.fechaCheckOut());
        });
        reserva.setDetalles(detalles);
        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(fullTotal);
        // Si fechaExpiracionPago se seteó con base estimada antes del PrePersist, recalibrar tras fechaCreacion definitiva
        if (estadoInicial == EstadoReserva.PENDIENTE_PAGO && reserva.getFechaCreacion() == null) {
            // fechaCreacion se asigna en PrePersist al guardar; ajustaremos tras save si hace falta
        }

        detalles.forEach(d -> d.getHabitacion().setEstado(EstadoHabitacion.RESERVADA));

        Reserva guardada = reservaRepository.save(reserva);
        // Asegurar expiración alineada a fechaCreacion real (PrePersist)
        if (guardada.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
            int ttl = reservationProperties.getPaymentExpirationMinutes();
            LocalDateTime expiracionEsperada = guardada.getFechaCreacion().plusMinutes(ttl);
            if (guardada.getFechaExpiracionPago() == null || !guardada.getFechaExpiracionPago().equals(expiracionEsperada)) {
                guardada.setFechaExpiracionPago(expiracionEsperada);
                guardada = reservaRepository.save(guardada);
            }
        }

        auditService.log(AuditAction.CREATE, "Reserva", guardada.getId(),
                String.format("Reserva creada — %s\nHabitación: %s\nTitular: %s %s\nEstadía: %s → %s",
                        guardada.getCodigoReserva(),
                        detalles.stream().map(d -> d.getHabitacion().getNumero()).reduce((a, b) -> a + ", " + b).orElse("N/A"),
                        titular.getNombreCompleto(), titular.getApellidoCompleto(),
                        guardada.getFechaCheckIn().format(DATE_FMT), guardada.getFechaCheckOut().format(DATE_FMT)));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_CREADA,
                new ReservaPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        titular.getNombreCompleto() + " " + titular.getApellidoCompleto(),
                        guardada.getEstado().name(),
                        guardada.getFechaCheckIn().toString(),
                        guardada.getFechaCheckOut().toString(),
                        guardada.getTotal()),
                actor.email()));

        String titularNombre = titular.getNombreCompleto() + " " + titular.getApellidoCompleto();
        Map<String, Object> notifDetalles = new HashMap<>();
        notifDetalles.put("clienteNombre", titularNombre);
        notifDetalles.put("codigoReserva", guardada.getCodigoReserva());
        notifDetalles.put("fechaCheckIn", guardada.getFechaCheckIn().toString());
        notifDetalles.put("fechaCheckOut", guardada.getFechaCheckOut().toString());
        notifDetalles.put("total", guardada.getTotal());
        String origenNotif = (guardada.getOrigen() == OrigenReserva.WEB) ? "WEB" : actor.origen();
        String origenNombre = (guardada.getOrigen() == OrigenReserva.WEB) ? null : actor.origenNombre();

        String msg;
        if (guardada.getOrigen() == OrigenReserva.WEB) {
            msg = actor.prefijo() + " " + titularNombre
                    + " realizó una nueva reserva desde el sitio web. Código: " + guardada.getCodigoReserva()
                    + ". Check-in: " + guardada.getFechaCheckIn().format(DATE_FMT) + ".";
        } else {
            msg = actor.prefijo() + " " + actor.nombre()
                    + " registró una reserva para " + titularNombre
                    + ". Código: " + guardada.getCodigoReserva()
                    + ". Check-in: " + guardada.getFechaCheckIn().format(DATE_FMT) + ".";
        }
        notificacionService.notificarATodos(TipoNotificacion.NUEVA_RESERVA,
                msg,
                origenNotif, origenNombre,
                notifDetalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    /**
     * {@inheritDoc}
     * Lanza ResourceNotFoundException si no se encuentra la reserva.
     */
    @Override
    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {
        return reservaMapper.toResponse(obtenerReserva(id));
    }

    /**
     * {@inheritDoc}
     * Utiliza especificaciones JPA para construir consultas dinámicas con los filtros proporcionados.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> listarTodos(
            String codigo, String cliente, EstadoReserva estado,
            OrigenReserva origen,
            LocalDate checkInDesde, LocalDate checkInHasta,
            LocalDate checkOutDesde, LocalDate checkOutHasta,
            Pageable pageable) {
        Specification<Reserva> spec = Specification
                .where(ReservaSpecification.codigoContiene(codigo))
                .and(ReservaSpecification.clienteContiene(cliente))
                .and(ReservaSpecification.estadoIgual(estado))
                .and(ReservaSpecification.origenIgual(origen))
                .and(ReservaSpecification.fechaCheckInEntre(checkInDesde, checkInHasta))
                .and(ReservaSpecification.fechaCheckOutEntre(checkOutDesde, checkOutHasta));

        Page<Reserva> page = reservaRepository.findAll(spec, pageable);
        return pageMapper.toPageResponse(page, reservaMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     * Valida permisos de actualización según el estado de la reserva.
     * Actualiza fechas, titular, origen, estado, observaciones y detalles.
     * Si cambian las fechas, recongela precios de detalles activos.
     * Verifica pagos completados antes de cancelar. Envía email de cancelación si aplica.
     */
    @Override
    public ReservaResponse actualizar(Long id, ReservaUpdateRequest dto) {
        Reserva reserva = obtenerReserva(id);

        if (dto.fechaCheckIn() != null) {
            EstadoReserva est = reserva.getEstado();
            if (est != EstadoReserva.PENDIENTE && est != EstadoReserva.PENDIENTE_PAGO && est != EstadoReserva.CONFIRMADA) {
                throw new ValidationException("fechaCheckIn",
                        "solo se puede cambiar la fecha de ingreso en reservas pendientes o confirmadas");
            }
            if (dto.fechaCheckIn().isBefore(LocalDate.now())) {
                throw new ValidationException("fechaCheckIn",
                        "no puede ser una fecha pasada");
            }
            LocalDate checkOut = dto.fechaCheckOut() != null ? dto.fechaCheckOut() : reserva.getFechaCheckOut();
            if (checkOut.isBefore(dto.fechaCheckIn())) {
                throw new ValidationException("fechaCheckIn",
                        "debe ser anterior o igual a fechaCheckOut");
            }
            long nuevasNoches = ChronoUnit.DAYS.between(dto.fechaCheckIn(), checkOut);
            if (nuevasNoches < 0 || nuevasNoches > Reserva.MAX_DIAS_ESTADIA) {
                throw new ValidationException("fechaCheckIn",
                        "la estadía debe ser entre 1 y " + Reserva.MAX_DIAS_ESTADIA + " días");
            }
            reserva.setFechaCheckIn(dto.fechaCheckIn());
        }

        if (dto.fechaCheckOut() != null) {
            if (dto.fechaCheckOut().isBefore(reserva.getFechaCheckIn())) {
                throw new ValidationException("fechaCheckOut", "debe ser posterior o igual a fechaCheckIn");
            }
            reserva.setFechaCheckOut(dto.fechaCheckOut());
        }

        if (dto.titularReservaId() != null) {
            EstadoReserva est = reserva.getEstado();
            if (est != EstadoReserva.PENDIENTE && est != EstadoReserva.PENDIENTE_PAGO && est != EstadoReserva.CONFIRMADA) {
                throw new ValidationException("titularReservaId",
                        "solo se puede cambiar el titular en reservas pendientes o confirmadas");
            }
            Cliente nuevoTitular = clienteRepository.findById(dto.titularReservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.titularReservaId()));
            reserva.setTitularReserva(nuevoTitular);
        }

        if (dto.origen() != null) {
            reserva.setOrigen(dto.origen());
        }

        if (dto.estado() != null) {
            if (dto.estado() == EstadoReserva.CANCELADA) {
                boolean tienePagosCompletados = pagoRepository
                        .findByReservaIdAndEstadoOrderByFechaPagoDesc(id, EstadoPago.COMPLETADO)
                        .stream().anyMatch(p -> p.getEstado() == EstadoPago.COMPLETADO);
                if (tienePagosCompletados) {
                    throw new ValidationException("pagos",
                            "no se puede cancelar una reserva con pagos completados. Si necesita anular la reserva, primero debe reembolsar los pagos");
                }
            }
            com.hotelcaciquebolivar.reservas.service.reserva.EstadoUtils.aplicarTransicionEstado(reserva, dto.estado());
        }

        if (dto.observaciones() != null) {
            reserva.setObservaciones(dto.observaciones());
        }

        boolean fechasCambiadas = dto.fechaCheckIn() != null || dto.fechaCheckOut() != null;
        if (dto.detalles() != null && !dto.detalles().isEmpty()) {
            actualizarDetalles(reserva, dto.detalles());
        }
        if (fechasCambiadas) {
            for (DetalleReserva dr : reserva.getDetalles()) {
                if (dr.getEstado() != com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva.CANCELADA
                        && dr.getEstado() != com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva.LIBERADA) {
                    reservaPricingService.congelarPreciosDetalle(dr, reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
                }
            }
        }
        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(fullTotal);
        Reserva actualizada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", actualizada.getId(),
                String.format("Reserva actualizada — %s\nEstado: %s\nTotal: $%s",
                        actualizada.getCodigoReserva(), actualizada.getEstado(), actualizada.getTotal()));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        if (dto.estado() == EstadoReserva.CANCELADA) {
        String emailTitular = actualizada.getTitularReserva().getUsuario() != null
                ? actualizada.getTitularReserva().getUsuario().getEmail()
                : null;
        if (emailTitular != null) {
            String nombreCompleto = actualizada.getTitularReserva().getNombreCompleto()
                    + " " + actualizada.getTitularReserva().getApellidoCompleto();
            emailService.sendReservationCancellation(new ReservaCanceladaData(
                    emailTitular,
                    "Reserva cancelada - " + actualizada.getCodigoReserva(),
                    nombreCompleto,
                    actualizada.getCodigoReserva(),
                    LocalDateTime.now(),
                    BigDecimal.ZERO,
                    "Cancelación solicitada"));
        }
        String titularNombre = actualizada.getTitularReserva().getNombreCompleto()
                + " " + actualizada.getTitularReserva().getApellidoCompleto();
        Map<String, Object> detallesCancelacion = new HashMap<>();
        detallesCancelacion.put("clienteNombre", titularNombre);
        detallesCancelacion.put("codigoReserva", actualizada.getCodigoReserva());
        notificacionService.notificarATodos(TipoNotificacion.RESERVA_CANCELADA,
                actor.prefijo() + " " + actor.nombre()
                + " canceló la reserva " + actualizada.getCodigoReserva()
                + " de " + titularNombre + ".",
                actor.origen(), actor.origenNombre(),
                detallesCancelacion,
                "RESERVA", actualizada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());
        }

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_ACTUALIZADA,
                new ReservaPayload(
                        actualizada.getId(),
                        actualizada.getCodigoReserva(),
                        actualizada.getTitularReserva().getNombreCompleto()
                        + " " + actualizada.getTitularReserva().getApellidoCompleto(),
                        actualizada.getEstado().name(),
                        actualizada.getFechaCheckIn().toString(),
                        actualizada.getFechaCheckOut().toString(),
                        actualizada.getTotal()),
                actor.email()));

        return reservaMapper.toResponse(actualizada);
    }

    /**
     * {@inheritDoc}
     * No permite eliminar reservas en estado EN_CURSO o con pagos completados.
     * Registra auditoría y publica evento SSE antes de eliminar físicamente la reserva.
     */
    @Override
    public void eliminar(Long id) {
        Reserva reserva = obtenerReserva(id);
        if (reserva.getEstado() == EstadoReserva.EN_CURSO) {
            throw new ValidationException("estado", "no se puede eliminar una reserva en curso");
        }

        boolean tienePagosCompletados = pagoRepository
                .findByReservaIdAndEstadoOrderByFechaPagoDesc(id, EstadoPago.COMPLETADO)
                .stream().anyMatch(p -> p.getEstado() == EstadoPago.COMPLETADO);
        if (tienePagosCompletados) {
            throw new ValidationException("pagos",
                    "no se puede eliminar una reserva con pagos completados. Use cancelar en su lugar");
        }

        auditService.log(AuditAction.DELETE, "Reserva", id,
                String.format("Reserva eliminada — %s\nEstado: %s\nTitular: %s %s",
                        reserva.getCodigoReserva(), reserva.getEstado(),
                        reserva.getTitularReserva().getNombreCompleto(), reserva.getTitularReserva().getApellidoCompleto()));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_CANCELADA,
                new ReservaPayload(
                        reserva.getId(),
                        reserva.getCodigoReserva(),
                        reserva.getTitularReserva().getNombreCompleto()
                        + " " + reserva.getTitularReserva().getApellidoCompleto(),
                        reserva.getEstado().name(),
                        reserva.getFechaCheckIn().toString(),
                        reserva.getFechaCheckOut().toString(),
                        reserva.getTotal()),
                actor.email()));

        reservaRepository.delete(reserva);
    }

    // ====== VALIDACIONES COMPARTIDAS ======
    /**
     * Formato de fecha para mostrar en registros de auditoría (español Colombia).
     */
    public static final java.time.format.DateTimeFormatter DATE_FMT
            = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", new java.util.Locale("es", "CO"));

    /**
     * Valida las fechas de creación de una reserva.
     * Verifica que no sean nulas, que check-out sea posterior a check-in,
     * que check-in no sea fecha pasada y que la estadía esté dentro del rango permitido.
     *
     * @param checkIn  fecha de check-in
     * @param checkOut fecha de check-out
     * @throws ValidationException si las fechas no son válidas
     */
    public static void validarFechasCreacion(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut == null || checkIn == null) {
            throw new ValidationException("fechas", "las fechas son obligatorias");
        }
        if (checkOut.isBefore(checkIn)) {
            throw new ValidationException("fechaCheckOut", "debe ser posterior o igual a fechaCheckIn");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("fechaCheckIn", "no puede ser una fecha pasada");
        }
        long noches = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (noches < 0) {
            throw new ValidationException("fechas", "la estadía mínima es de 1 noche");
        }
        if (noches > Reserva.MAX_DIAS_ESTADIA) {
            throw new ValidationException("fechas",
                    "la estadía máxima es de " + Reserva.MAX_DIAS_ESTADIA + " días");
        }
    }

    /**
     * Valida que el precio por noche sea mayor a cero.
     *
     * @param precio    el precio por noche a validar
     * @param habitacion la habitación asociada (para contexto de error)
     * @throws ValidationException si el precio es nulo o menor o igual a cero
     */
    public static void validarPrecio(BigDecimal precio, Habitacion habitacion) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("precioPorNoche",
                    "debe ser mayor a 0");
        }
    }

    /**
     * Valida que la cantidad de ocupantes no supere la capacidad máxima de la habitación.
     *
     * @param cantidadOcupantes cantidad de ocupantes a validar
     * @param habitacion        la habitación cuya capacidad se verifica
     * @throws ValidationException si la cantidad supera la capacidad máxima
     */
    public static void validarCapacidad(Integer cantidadOcupantes, Habitacion habitacion) {
        if (cantidadOcupantes == null) {
            return;
        }
        if (cantidadOcupantes > habitacion.getTipo().getCapacidadMaxima()) {
            throw new ValidationException("cantidadOcupantes",
                    "supera la capacidad maxima de la habitacion ("
                    + habitacion.getTipo().getCapacidadMaxima() + ")");
        }
    }

    // ====== MÉTODOS PRIVADOS ======
    /**
     * Obtiene una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada
     * @throws ResourceNotFoundException si no se encuentra la reserva
     */
    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }

    /**
     * Construye la lista de detalles de reserva a partir de los DTOs de solicitud.
     * Verifica disponibilidad de habitaciones con bloqueo pessimista, valida precios
     * y capacidad, y congela precios para el rango de fechas.
     *
     * @param detallesDto lista de DTOs de detalles
     * @param checkIn     fecha de check-in
     * @param checkOut    fecha de check-out
     * @return lista de detalles de reserva construidos
     * @throws RoomNotAvailableException si alguna habitación no está disponible
     */
    private List<DetalleReserva> construirDetalles(
            List<DetalleReservaRequest> detallesDto,
            LocalDate checkIn,
            LocalDate checkOut) {

        LocalDate queryCheckOut = checkIn.equals(checkOut) ? checkOut.plusDays(1) : checkOut;
        return detallesDto.stream().map(d -> {
            Habitacion habitacion = habitacionRepository.findDisponiblePorIdConLock(
                    d.habitacionId(), checkIn, queryCheckOut)
                    .orElseThrow(() -> new RoomNotAvailableException(
                    habitacionRepository.findById(d.habitacionId())
                            .map(Habitacion::getNumero)
                            .orElse(String.valueOf(d.habitacionId())),
                    checkIn, checkOut));

            validarPrecio(d.precioPorNoche(), habitacion);
            validarCapacidad(d.cantidadOcupantes(), habitacion);

            DetalleReserva detalle = DetalleReserva.builder()
                    .habitacion(habitacion)
                    .precioPorNoche(d.precioPorNoche())
                    .cantidadOcupantes(d.cantidadOcupantes())
                    .observaciones(d.observaciones())
                    .precioUsadoIncluyeIva(d.precioIncluyeIva())
                    .build();

            detalle.validarCapacidad();
            return detalle;

        }).toList();
    }

    /**
     * Actualiza los detalles existentes de una reserva según los DTOs de actualización.
     * Si el detalle tiene ID, actualiza habitación, precio y capacidad.
     * Si no tiene ID, crea un nuevo detalle. Verifica disponibilidad de habitaciones
     * y congela precios cuando es necesario.
     *
     * @param reserva    la reserva cuyos detalles se van a actualizar
     * @param detallesDto lista de DTOs de actualización de detalles
     */
    private void actualizarDetalles(Reserva reserva, List<DetalleReservaUpdateRequest> detallesDto) {
        detallesDto.forEach(d -> {
            if (d.detalleId() != null) {
                DetalleReserva existente = reserva.getDetalles().stream()
                        .filter(x -> x.getId().equals(d.detalleId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", d.detalleId()));

                if (d.habitacionId() != null) {
                    LocalDate qCheckOut = reserva.getFechaCheckIn().equals(reserva.getFechaCheckOut())
                            ? reserva.getFechaCheckOut().plusDays(1) : reserva.getFechaCheckOut();
                    Habitacion habitacion = habitacionRepository.findDisponiblePorIdConLock(
                            d.habitacionId(), reserva.getFechaCheckIn(), qCheckOut)
                            .orElseThrow(() -> new RoomNotAvailableException(
                            habitacionRepository.findById(d.habitacionId())
                                    .map(Habitacion::getNumero)
                                    .orElse(String.valueOf(d.habitacionId())),
                            reserva.getFechaCheckIn(), reserva.getFechaCheckOut()));
                    Habitacion vieja = existente.getHabitacion();
                    if (vieja.getEstado() == EstadoHabitacion.RESERVADA) {
                        vieja.setEstado(EstadoHabitacion.DISPONIBLE);
                    }
                    existente.setHabitacion(habitacion);
                    reservaPricingService.congelarPreciosDetalle(existente, reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
                }

                if (d.precioPorNoche() != null) {
                    validarPrecio(d.precioPorNoche(), existente.getHabitacion());
                    existente.setPrecioPorNoche(d.precioPorNoche());
                }

                if (d.cantidadOcupantes() != null) {
                    if (existente.getHuespedes() != null
                            && d.cantidadOcupantes() < existente.getHuespedes().size()) {
                        throw new ValidationException("cantidadOcupantes",
                                String.format("no puede ser menor a los huéspedes ya registrados (%d)",
                                        existente.getHuespedes().size()));
                    }
                    validarCapacidad(d.cantidadOcupantes(), existente.getHabitacion());
                    existente.setCantidadOcupantes(d.cantidadOcupantes());
                }

                existente.validarCapacidad();

            } else {
                DetalleReservaRequest nuevo = new DetalleReservaRequest(
                        d.habitacionId(),
                        d.precioPorNoche(),
                        d.cantidadOcupantes(),
                        d.observaciones(),
                        null
                );
                List<DetalleReserva> nuevos = construirDetalles(List.of(nuevo),
                        reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
                nuevos.forEach(n -> {
                    n.setReserva(reserva);
                    reservaPricingService.congelarPreciosDetalle(n, reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
                });
                reserva.agregarDetalle(nuevos.get(0));
            }
        });
    }
}
