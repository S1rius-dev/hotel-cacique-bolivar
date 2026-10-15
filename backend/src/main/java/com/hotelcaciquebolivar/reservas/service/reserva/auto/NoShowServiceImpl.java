package com.hotelcaciquebolivar.reservas.service.reserva.auto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de procesamiento automático de No-Show.
 * Marca como NO_SHOW las reservas confirmadas o pendientes cuya fecha de check-in
 * ha vencido sin que se haya realizado check-in. Cancela los detalles reservados,
 * libera las habitaciones, recalcula totales y envía notificaciones a los roles
 * ADMIN y RECEPCIONISTA. Maneja conflictos de concurrencia individualmente.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoShowServiceImpl implements NoShowService {

    private final ReservaRepository reservaRepository;
    private final AuditService auditService;
    private final ReservaPricingService reservaPricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    /**
     * {@inheritDoc}
     * Busca reservas en estado PENDIENTE o CONFIRMADA con check-in vencido y procesa
     * cada una individualmente. Si ocurre un conflicto de concurrencia o cualquier otro error,
     * se registra en el log y se continúa con la siguiente reserva.
     *
     * @return número de reservas procesadas exitosamente como NO_SHOW
     */
    @Override
    public int procesarNoShow() {
        LocalDate hoy = LocalDate.now();
        List<Reserva> candidatas = reservaRepository
                .findPendienteConfirmadaConCheckInVencido(hoy);

        if (candidatas.isEmpty()) {
            return 0;
        }

        int procesadas = 0;
        for (Reserva reserva : candidatas) {
            try {
                procesarUnaReserva(reserva);
                procesadas++;
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Conflicto de concurrencia en reserva {}, saltando NO_SHOW",
                        reserva.getId());
            } catch (Exception e) {
                log.error("Error al procesar NO_SHOW para reserva {}: {}",
                        reserva.getId(), e.getMessage(), e);
            }
        }
        return procesadas;
    }

    /**
     * Procesa una reserva individual para marcado como NO_SHOW.
     * Verifica que no haya detalles en estado OCUPADA, marca la reserva como NO_SHOW,
     * cancela los detalles reservados, libera las habitaciones, recalcula el total,
     * registra auditoría, publica eventos SSE y envía notificaciones a ADMIN y RECEPCIONISTA.
     *
     * @param reserva la reserva a procesar como NO_SHOW
     */
    private void procesarUnaReserva(Reserva reserva) {
        boolean algunOcupado = reserva.getDetalles().stream()
                .anyMatch(d -> d.getEstado() == EstadoDetalleReserva.OCUPADA);
        if (algunOcupado) {
            log.info("Reserva {} tiene detalles OCUPADA, se omite NO_SHOW",
                    reserva.getCodigoReserva());
            return;
        }

        reserva.marcarNoShow();

        for (DetalleReserva detalle : reserva.getDetalles()) {
            if (detalle.getEstado() == EstadoDetalleReserva.RESERVADA) {
                detalle.cancelar();
                if (detalle.getHabitacion().getEstado()
                        == EstadoHabitacion.RESERVADA) {
                    detalle.getHabitacion().setEstado(
                            EstadoHabitacion.DISPONIBLE);
                }
            }
        }

        BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(fullTotal);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.NO_SHOW_AUTO, "Reserva", guardada.getId(),
                String.format("NO_SHOW automático — %s\nCheck-in: %s\nHabitaciones: %s",
                        guardada.getCodigoReserva(),
                        guardada.getFechaCheckIn(),
                        guardada.getDetalles().stream()
                                .map(d -> d.getHabitacion().getNumero())
                                .reduce((a, b) -> a + ", " + b).orElse("N/A")));

        ActorInfo actor = notificacionMessageBuilder.obtenerActorSistema();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.NO_SHOW_AUTO,
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
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoReserva", guardada.getCodigoReserva());
        notificacionService.notificarATodos(TipoNotificacion.NO_SHOW,
                actor.prefijo() + " No-show automático para " + clienteNombre
                + ". Código: " + guardada.getCodigoReserva()
                + ". Check-in no realizado el " + guardada.getFechaCheckIn() + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "RESERVA", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());
    }
}
