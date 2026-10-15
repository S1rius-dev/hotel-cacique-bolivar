package com.hotelcaciquebolivar.reservas.service.reserva.checkin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleCheckInRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.CheckInOutPayload;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de check-in y check-out de reservas.
 * Gestiona el registro de entrada y salida de huéspedes, tanto a nivel de reserva completa
 * como a nivel de detalle individual. Actualiza estados de reserva, detalles y habitaciones,
 * registra fechas de ingreso/salida de huéspedes, calcula totales, envía notificaciones
 * y publica eventos SSE. Sincroniza el estado de la reserva después de cada operación.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CheckInOutServiceImpl implements CheckInOutService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaMapper reservaMapper;
    private final AuditService auditService;
    private final com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService reservaPricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    /**
     * {@inheritDoc}
     * Valida que la fecha actual coincida con la fecha de check-in de la reserva.
     * Cambia el estado de la reserva a EN_CURSO, marca todos los detalles como OCUPADA,
     * establece las habitaciones como OCUPADAS, registra la hora de check-in,
     * envía notificaciones y publica eventos SSE.
     */
    @Override
    public ReservaResponse realizarCheckIn(Long reservaId) {
        Reserva reserva = obtenerReserva(reservaId);

        if (!reserva.getFechaCheckIn().equals(LocalDate.now())) {
            throw new ValidationException("fechaCheckIn",
                    "el check-in solo puede realizarse en la fecha de inicio de la reserva");
        }

        com.hotelcaciquebolivar.reservas.service.reserva.EstadoUtils.aplicarTransicionEstado(reserva, EstadoReserva.EN_CURSO);

        List<Habitacion> habitaciones = reserva.getDetalles().stream()
                .peek(d -> {
                    d.realizarCheckIn();
                    d.getHuespedes().forEach(h -> h.setFechaIngreso(LocalDateTime.now()));
                })
                .map(DetalleReserva::getHabitacion)
                .peek(h -> h.setEstado(EstadoHabitacion.OCUPADA))
                .toList();
        habitacionRepository.saveAll(habitaciones);

        reserva.setHoraCheckIn(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.CHECK_IN, "Reserva", reservaId,
                String.format("Check-in realizado — %s\nHabitación: %s\nHuéspedes: %d",
                        guardada.getCodigoReserva(),
                        guardada.getDetalles().stream()
                                .map(d -> d.getHabitacion().getNumero())
                                .reduce((a, b) -> a + ", " + b).orElse("N/A"),
                        guardada.getDetalles().stream()
                                .flatMap(d -> d.getHuespedes().stream())
                                .count()));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CHECK_IN,
                new CheckInOutPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        "COMPLETO",
                        guardada.getDetalles().stream()
                                .map(d -> d.getHabitacion().getNumero()).toList(),
                        (int) guardada.getDetalles().stream()
                                .flatMap(d -> d.getHuespedes().stream()).count()),
                actor.email()));

        String habStr = String.join(", ", guardada.getDetalles().stream()
                .map(d -> d.getHabitacion().getNumero()).toList());
        String clienteNombre = guardada.getTitularReserva() != null
                ? guardada.getTitularReserva().getNombreCompleto()
                + " " + guardada.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        detalles.put("numeroHabitacion", habStr);
        notificacionService.notificarATodos(TipoNotificacion.CHECK_IN,
                actor.prefijo() + " " + actor.nombre()
                + " realizó el check-in de " + clienteNombre
                + ". Código: " + guardada.getCodigoReserva()
                + ". Habitaciones: " + habStr + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    /**
     * {@inheritDoc}
     * Cambia el estado de la reserva a FINALIZADA, marca todos los detalles como LIBERADA,
     * establece las habitaciones como EN_LIMPIEZA, registra la hora de check-out,
     * envía notificaciones y publica eventos SSE. Incluye información sobre saldo pendiente
     * en el registro de auditoría si existe.
     */
    @Override
    public ReservaResponse realizarCheckOut(Long reservaId) {
        Reserva reserva = obtenerReserva(reservaId);

        com.hotelcaciquebolivar.reservas.service.reserva.EstadoUtils.aplicarTransicionEstado(reserva, EstadoReserva.FINALIZADA);

        List<Habitacion> habitaciones = reserva.getDetalles().stream()
                .peek(d -> {
                    d.realizarCheckOut();
                    d.getHuespedes().forEach(h -> h.setFechaSalida(LocalDateTime.now()));
                })
                .map(DetalleReserva::getHabitacion)
                .peek(h -> h.setEstado(EstadoHabitacion.EN_LIMPIEZA))
                .toList();
        habitacionRepository.saveAll(habitaciones);

        reserva.setHoraCheckOut(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);

        BigDecimal saldoPendiente = guardada.getSaldoPendiente();
        String detalleSaldo = saldoPendiente.compareTo(BigDecimal.ZERO) > 0
                ? "\nSALDO PENDIENTE: $" + saldoPendiente + " (cliente se fue sin pagar la totalidad)"
                : "";

        auditService.log(AuditAction.CHECK_OUT, "Reserva", reservaId,
                String.format("Check-out realizado — %s\nHabitación: %s\nNoches: %d\nTotal: $%s%s",
                        guardada.getCodigoReserva(),
                        guardada.getDetalles().stream()
                                .map(d -> d.getHabitacion().getNumero())
                                .reduce((a, b) -> a + ", " + b).orElse("N/A"),
                        guardada.calcularNoches(), guardada.getTotal(), detalleSaldo));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CHECK_OUT,
                new CheckInOutPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        "COMPLETO",
                        guardada.getDetalles().stream()
                                .map(d -> d.getHabitacion().getNumero()).toList(),
                        (int) guardada.getDetalles().stream()
                                .flatMap(d -> d.getHuespedes().stream()).count()),
                actor.email()));

        String habStr = String.join(", ", guardada.getDetalles().stream()
                .map(d -> d.getHabitacion().getNumero()).toList());
        String clienteNombre = guardada.getTitularReserva() != null
                ? guardada.getTitularReserva().getNombreCompleto()
                + " " + guardada.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        detalles.put("numeroHabitacion", habStr);
        notificacionService.notificarATodos(TipoNotificacion.CHECK_OUT,
                actor.prefijo() + " " + actor.nombre()
                + " realizó el check-out de " + clienteNombre
                + ". Código: " + guardada.getCodigoReserva()
                + ". Habitaciones: " + habStr + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    /**
     * {@inheritDoc}
     * Valida que la reserva no esté en estado finalizado, cancelado o NO_SHOW,
     * y que la fecha actual coincida con la fecha de check-in. Agrega los huéspedes
     * especificados al detalle, realiza el check-in del detalle, actualiza la habitación
     * a OCUPADA, sincroniza el estado de la reserva, recalcula el total,
     * envía notificaciones y publica eventos SSE.
     */
    @Override
    public ReservaResponse checkInPorDetalle(Long reservaId, Long detalleId, DetalleCheckInRequest dto) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReserva.FINALIZADA
                || reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado",
                    "no se puede realizar check-in en una reserva " + reserva.getEstado());
        }

        if (!reserva.getFechaCheckIn().equals(LocalDate.now())) {
            throw new ValidationException("fechaCheckIn",
                    "el check-in solo puede realizarse en la fecha de inicio de la reserva");
        }

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        for (Long clienteId : dto.huespedesIds()) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
            detalle.agregarHuesped(cliente);
        }

        LocalDateTime ahora = LocalDateTime.now();
        detalle.getHuespedes().forEach(h -> {
            if (h.getFechaIngreso() == null) {
                h.setFechaIngreso(ahora);
            }
        });

        detalle.realizarCheckIn();

        Habitacion habitacion = detalle.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        if (reserva.getHoraCheckIn() == null) {
            reserva.setHoraCheckIn(LocalDateTime.now());
        }

        sincronizarEstadoReserva(reserva);

        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(fullTotal);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.CHECK_IN, "DetalleReserva", detalleId,
                String.format("Check-in por habitación — %s\nHabitación: %s\nHuéspedes agregados: %d",
                        guardada.getCodigoReserva(), habitacion.getNumero(), dto.huespedesIds().size()));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CHECK_IN,
                new CheckInOutPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        "POR_DETALLE",
                        List.of(habitacion.getNumero()),
                        dto.huespedesIds().size()),
                actor.email()));

        String clienteNombre = guardada.getTitularReserva() != null
                ? guardada.getTitularReserva().getNombreCompleto()
                + " " + guardada.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        detalles.put("numeroHabitacion", habitacion.getNumero());
        notificacionService.notificarATodos(TipoNotificacion.CHECK_IN,
                actor.prefijo() + " " + actor.nombre()
                + " realizó el check-in de la habitación " + habitacion.getNumero()
                + " de " + clienteNombre + ". Código: " + guardada.getCodigoReserva() + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    /**
     * {@inheritDoc}
     * Realiza el check-out del detalle específico, establece las fechas de salida
     * de los huéspedes, cambia el estado de la habitación a EN_LIMPIEZA,
     * sincroniza el estado de la reserva, envía notificaciones y publica eventos SSE.
     */
    @Override
    public ReservaResponse checkOutPorDetalle(Long reservaId, Long detalleId) {
        Reserva reserva = obtenerReserva(reservaId);

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        detalle.realizarCheckOut();
        LocalDateTime ahora = LocalDateTime.now();
        detalle.getHuespedes().forEach(h -> {
            if (h.getFechaSalida() == null) {
                h.setFechaSalida(ahora);
            }
        });

        Habitacion habitacion = detalle.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.EN_LIMPIEZA);
        habitacionRepository.save(habitacion);

        sincronizarEstadoReserva(reserva);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.CHECK_OUT, "DetalleReserva", detalleId,
                String.format("Check-out por habitación — %s\nHabitación: %s",
                        guardada.getCodigoReserva(), habitacion.getNumero()));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CHECK_OUT,
                new CheckInOutPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        "POR_DETALLE",
                        List.of(habitacion.getNumero()),
                        0),
                actor.email()));

        String clienteNombre = guardada.getTitularReserva() != null
                ? guardada.getTitularReserva().getNombreCompleto()
                + " " + guardada.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        detalles.put("numeroHabitacion", habitacion.getNumero());
        notificacionService.notificarATodos(TipoNotificacion.CHECK_OUT,
                actor.prefijo() + " " + actor.nombre()
                + " realizó el check-out de la habitación " + habitacion.getNumero()
                + " de " + clienteNombre + ". Código: " + guardada.getCodigoReserva() + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    /**
     * Sincroniza el estado de una reserva después de operaciones de check-in/check-out
     * por detalle. Evalúa la proporción de detalles en cada estado para determinar
     * el nuevo estado de la reserva:
     * <ul>
     *   <li>Si todos los detalles están liberados o cancelados: → FINALIZADA</li>
     *   <li>Si hay detalles ocupados y la reserva no está en EN_CURSO: → EN_CURSO</li>
     * </ul>
     *
     * @param reserva la reserva cuyo estado se va a sincronizar
     */
    private void sincronizarEstadoReserva(Reserva reserva) {
        List<DetalleReserva> detalles = reserva.getDetalles();
        long total = detalles.size();
        long ocupadas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.OCUPADA).count();
        long liberadas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.LIBERADA).count();
        long canceladas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.CANCELADA).count();

        if (total > 0 && (liberadas == total || (liberadas + canceladas) == total)) {
            reserva.setEstado(EstadoReserva.FINALIZADA);
            if (reserva.getHoraCheckOut() == null) {
                reserva.setHoraCheckOut(LocalDateTime.now());
            }
        } else if (ocupadas > 0 && reserva.getEstado() != EstadoReserva.EN_CURSO) {
            reserva.setEstado(EstadoReserva.EN_CURSO);
        }
    }

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

}
