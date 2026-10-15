package com.hotelcaciquebolivar.reservas.service.reserva.extension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.reserva.RoomNotAvailableException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.ReservaPayload;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservaExtensionServiceImpl implements ReservaExtensionService {

    private static final java.time.format.DateTimeFormatter DATE_FMT
            = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", new java.util.Locale("es", "CO"));

    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleReservaRepository;
    private final ReservaMapper reservaMapper;
    private final AuditService auditService;
    private final ReservaPricingService reservaPricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    @Override
    public ReservaResponse extenderEstadia(Long reservaId, LocalDate nuevaFechaCheckOut) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReserva.EN_CURSO
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ValidationException("estado",
                    "solo se puede extender una reserva en curso o confirmada");
        }

        LocalDate checkOutActual = reserva.getFechaCheckOut();
        if (!nuevaFechaCheckOut.isAfter(checkOutActual)) {
            throw new ValidationException("nuevaFechaCheckOut",
                    "debe ser posterior al check-out actual (" + checkOutActual + ")");
        }

        long nuevasNoches = ChronoUnit.DAYS.between(reserva.getFechaCheckIn(), nuevaFechaCheckOut);
        if (nuevasNoches > Reserva.MAX_DIAS_ESTADIA) {
            throw new ValidationException("nuevaFechaCheckOut",
                    "la estadía máxima es de " + Reserva.MAX_DIAS_ESTADIA + " días");
        }

        for (DetalleReserva detalle : reserva.getDetalles()) {
            if (detalle.getEstado() == EstadoDetalleReserva.CANCELADA
                    || detalle.getEstado() == EstadoDetalleReserva.LIBERADA) {
                continue;
            }
            Optional<DetalleReserva> conflicto = detalleReservaRepository.findConflictoExtension(
                    detalle.getHabitacion().getId(),
                    detalle.getId(),
                    checkOutActual,
                    nuevaFechaCheckOut);
            if (conflicto.isPresent()) {
                throw new RoomNotAvailableException(
                        detalle.getHabitacion().getNumero(),
                        checkOutActual,
                        nuevaFechaCheckOut);
            }
        }

        reserva.setFechaCheckOut(nuevaFechaCheckOut);
        // Re-congelar precios para las noches extendidas con tarifas vigentes
        for (DetalleReserva dr : reserva.getDetalles()) {
            if (dr.getEstado() != EstadoDetalleReserva.CANCELADA && dr.getEstado() != EstadoDetalleReserva.LIBERADA) {
                reservaPricingService.congelarPreciosDetalle(dr, reserva.getFechaCheckIn(), nuevaFechaCheckOut);
            }
        }
        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        BigDecimal totalAnterior = reserva.getTotal();
        reserva.setTotal(fullTotal);
        Reserva guardada = reservaRepository.save(reserva);

        BigDecimal saldoPendiente = guardada.getSaldoPendiente();
        String avisoSaldo = saldoPendiente.compareTo(BigDecimal.ZERO) > 0
                ? "\nSALDO PENDIENTE tras extensión: $" + saldoPendiente
                : "";

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Estadía extendida — %s\nCheck-out: %s → %s\nTotal: $%s → $%s%s",
                        guardada.getCodigoReserva(),
                        checkOutActual.format(DATE_FMT), nuevaFechaCheckOut.format(DATE_FMT),
                        totalAnterior, guardada.getTotal(), avisoSaldo));

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_EXTENDIDA,
                new ReservaPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        guardada.getTitularReserva() != null
                        ? guardada.getTitularReserva().getNombreCompleto()
                        + " " + guardada.getTitularReserva().getApellidoCompleto()
                        : "N/A",
                        guardada.getEstado().name(),
                        guardada.getFechaCheckIn().toString(),
                        guardada.getFechaCheckOut().toString(),
                        guardada.getTotal()),
                actor.email()));

        String clienteNombre = guardada.getTitularReserva() != null
                ? guardada.getTitularReserva().getNombreCompleto()
                + " " + guardada.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        detalles.put("nuevoCheckOut", nuevaFechaCheckOut.toString());
        notificacionService.notificarATodos(TipoNotificacion.ESTADIA_EXTENDIDA,
                actor.prefijo() + " " + actor.nombre()
                + " extendió la estadía de " + clienteNombre
                + ". Código: " + guardada.getCodigoReserva()
                + ". Nuevo check-out: " + nuevaFechaCheckOut.format(DATE_FMT) + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return reservaMapper.toResponse(guardada);
    }

    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }

}
