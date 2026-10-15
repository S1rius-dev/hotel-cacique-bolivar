package com.hotelcaciquebolivar.reservas.service.reserva.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.mail.EmailService;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaCanceladaData;
import com.hotelcaciquebolivar.reservas.repository.pago.TransaccionPagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;

@Component
public class ReservaPendienteExpirador {

    private static final Logger log = LoggerFactory.getLogger(ReservaPendienteExpirador.class);

    private final ReservaRepository reservaRepository;
    private final TransaccionPagoRepository transaccionRepository;
    private final EmailService emailService;
    private final ReservationProperties reservationProperties;

    public ReservaPendienteExpirador(ReservaRepository reservaRepository,
            TransaccionPagoRepository transaccionRepository,
            EmailService emailService,
            ReservationProperties reservationProperties) {
        this.reservaRepository = reservaRepository;
        this.transaccionRepository = transaccionRepository;
        this.emailService = emailService;
        this.reservationProperties = reservationProperties;
    }

    private long ttlMinutes() {
        return reservationProperties.getPaymentExpirationMinutes();
    }

    @Scheduled(fixedDelay = 60000) // cada 1 minuto
    @Transactional
    public void expirarPendientes() {
        long ttl = ttlMinutes();
        List<Reserva> expiradas = reservaRepository.findPendientePagoExpiradas(LocalDateTime.now());
        // Compatibilidad: también expira legacy PENDIENTE+WEB sin fechaExpiracionPago
        if (expiradas.isEmpty()) {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(ttl);
            expiradas = reservaRepository.findPendientesWebExpiradas(threshold);
        }
        if (expiradas.isEmpty()) {
            return;
        }

        int canceladas = 0;
        for (Reserva reservaPre : expiradas) {
            try {
                // Re-bloquear con lock pesimista para evitar carrera con webhook (minuto 20)
                Reserva reserva = reservaRepository.findByIdWithLock(reservaPre.getId()).orElse(null);
                if (reserva == null) continue;
                if (reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO && reserva.getEstado() != EstadoReserva.PENDIENTE) {
                    continue;
                }
                // Doble verificación backend como fuente de verdad
                boolean tienePago = reserva.getPagos() != null && reserva.getPagos().stream()
                        .anyMatch(p -> p.getEstado() == EstadoPago.COMPLETADO);
                if (tienePago) {
                    continue;
                }
                boolean tieneAprobada = reserva.getTransaccionesPago() != null && reserva.getTransaccionesPago().stream()
                        .anyMatch(t -> t.getEstadoPasarela() == PaymentStatus.APPROVED);
                if (tieneAprobada) {
                    continue;
                }
                // Forzar carga fresca si colección lazy no inicializada: verificar via existencia de APPROVED en BD sería ideal,
                // pero con JOIN FETCH en query ya viene cargada. Mantenemos fallback.
                // Cancelar reserva
                String codigo = reserva.getCodigoReserva();
                reserva.cancelar();
                // Liberar habitaciones
                if (reserva.getDetalles() != null) {
                    reserva.getDetalles().forEach(d -> {
                        if (d.getHabitacion() != null && d.getHabitacion().getEstado() == EstadoHabitacion.RESERVADA) {
                            d.getHabitacion().setEstado(EstadoHabitacion.DISPONIBLE);
                        }
                    });
                }
                // Marcar transacciones huérfanas como EXPIRED
                if (reserva.getTransaccionesPago() != null) {
                    reserva.getTransaccionesPago().forEach(t -> {
                        if (t.getEstadoPasarela() == PaymentStatus.CREATED
                                || t.getEstadoPasarela() == PaymentStatus.PENDING) {
                            t.setEstadoPasarela(PaymentStatus.EXPIRED);
                            transaccionRepository.save(t);
                        }
                    });
                }
                reservaRepository.save(reserva);
                canceladas++;

                // Email de cancelación automática (único email de cancelación para expiración)
                try {
                    String emailTitular = reserva.getTitularReserva() != null
                            && reserva.getTitularReserva().getUsuario() != null
                            ? reserva.getTitularReserva().getUsuario().getEmail() : null;
                    if (emailTitular != null) {
                        String nombre = reserva.getTitularReserva().getNombreCompleto() + " "
                                + reserva.getTitularReserva().getApellidoCompleto();
                        emailService.sendReservationCancellation(new ReservaCanceladaData(
                                emailTitular,
                                "Reserva cancelada por falta de pago - " + codigo,
                                nombre,
                                codigo,
                                LocalDateTime.now(),
                                java.math.BigDecimal.ZERO,
                                "La reserva fue cancelada automáticamente por no completarse el pago en "
                                        + ttl + " minutos. Las habitaciones han sido liberadas."));
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo enviar email de cancelación automática para reserva {}: {}", codigo, ex.getMessage());
                }

                log.info("Reserva {} (id={}) expirada tras {} min sin pago — cancelada y habitaciones liberadas",
                        codigo, reserva.getId(), ttl);
            } catch (Exception ex) {
                log.error("Error al expirar reserva id={}: {}", reservaPre.getId(), ex.getMessage(), ex);
            }
        }
        if (canceladas > 0) {
            log.info("Expirador PENDIENTE_PAGO/WEB: {} reservas canceladas", canceladas);
        }
    }
}
