package com.hotelcaciquebolivar.reservas.service.reserva.auto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.CheckInOutPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de finalización automática de reservas.
 * Procesa reservas en estado EN_CURSO cuya fecha de check-out ha vencido,
 * realizando check-out automático de los detalles ocupados, liberando habitaciones
 * y sincronizando el estado de la reserva. Maneja conflictos de concurrencia
 * y errores individualmente para cada reserva.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AutoFinalizacionServiceImpl implements AutoFinalizacionService {

    private final ReservaRepository reservaRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     * Busca todas las reservas en estado EN_CURSO con check-out vencido y procesa
     * cada una individualmente. Si ocurre un conflicto de concurrencia optimista
     * o cualquier otro error, se registra en el log y se continúa con la siguiente reserva.
     *
     * @return número de reservas procesadas exitosamente
     */
    @Override
    public int finalizarReservasVencidas() {
        LocalDate hoy = LocalDate.now();
        List<Reserva> candidatas = reservaRepository
                .findEnCursoConCheckOutVencido(hoy);

        if (candidatas.isEmpty()) {
            return 0;
        }

        int procesadas = 0;
        for (Reserva reserva : candidatas) {
            try {
                procesarUnaReserva(reserva);
                procesadas++;
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Conflicto de concurrencia en reserva {}, saltando auto-checkout",
                        reserva.getId());
            } catch (Exception e) {
                log.error("Error al procesar auto-checkout para reserva {}: {}",
                        reserva.getId(), e.getMessage(), e);
            }
        }
        return procesadas;
    }

    /**
     * Procesa una reserva individual para auto-finalización.
     * Realiza check-out de todos los detalles en estado OCUPADA, establece las fechas
     * de salida de los huéspedes, cambia el estado de las habitaciones a EN_LIMPIEZA,
     * sincroniza el estado de la reserva, guarda los cambios y publica eventos
     * de auditoría y notificación SSE.
     *
     * @param reserva la reserva a procesar
     */
    private void procesarUnaReserva(Reserva reserva) {
        boolean algunOcupado = false;

        for (DetalleReserva detalle : reserva.getDetalles()) {
            if (detalle.getEstado() == EstadoDetalleReserva.OCUPADA) {
                detalle.realizarCheckOut();
                detalle.getHuespedes().forEach(h -> {
                    if (h.getFechaSalida() == null) {
                        h.setFechaSalida(LocalDateTime.now());
                    }
                });
                detalle.getHabitacion().setEstado(
                        EstadoHabitacion.EN_LIMPIEZA);
                algunOcupado = true;
            }
        }

        if (!algunOcupado) {
            log.info("Reserva {} sin detalles OCUPADA, se omite auto-finalización",
                    reserva.getCodigoReserva());
            return;
        }

        sincronizarEstadoReserva(reserva);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.CHECK_OUT_AUTO, "Reserva", guardada.getId(),
                String.format("Check-out automático — %s\nCheck-out original: %s\nHabitaciones liberadas: %s\nSaldo pendiente: $%s",
                        guardada.getCodigoReserva(),
                        guardada.getFechaCheckOut(),
                        guardada.getDetalles().stream()
                                .filter(d -> d.getEstado() == EstadoDetalleReserva.LIBERADA)
                                .map(d -> d.getHabitacion().getNumero())
                                .reduce((a, b) -> a + ", " + b).orElse("N/A"),
                        guardada.getSaldoPendiente()));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CHECK_OUT_AUTO,
                new CheckInOutPayload(
                        guardada.getId(),
                        guardada.getCodigoReserva(),
                        "AUTO",
                        guardada.getDetalles().stream()
                                .filter(d -> d.getEstado() == EstadoDetalleReserva.LIBERADA)
                                .map(d -> d.getHabitacion().getNumero())
                                .toList(),
                        (int) guardada.getDetalles().stream()
                                .flatMap(d -> d.getHuespedes().stream()).count())));
    }

    /**
     * Sincroniza el estado de una reserva después del procesamiento automático.
     * Si todos los detalles están liberados o cancelados, marca la reserva como FINALIZADA
     * y establece la hora de check-out actual. Si hay detalles ocupados y la reserva
     * no está en estado EN_CURSO, la cambia a ese estado.
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
}
