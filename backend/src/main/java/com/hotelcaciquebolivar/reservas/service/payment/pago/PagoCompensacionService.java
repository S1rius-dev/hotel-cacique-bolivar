package com.hotelcaciquebolivar.reservas.service.payment.pago;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.mail.EmailService;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaConfirmadaData;
import com.hotelcaciquebolivar.reservas.service.pago.PagoService;

/**
 * Servicio de compensación que registra pagos en línea aprobados por la pasarela activa.
 * Actúa como puente entre la confirmación de transacciones y el registro de pagos,
 * enviando el email de confirmación de reserva al titular con el desglose financiero.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
public class PagoCompensacionService {

    private static final Logger log = LoggerFactory.getLogger(PagoCompensacionService.class);

    private final PagoService pagoService;
    private final EmailService emailService;

    public PagoCompensacionService(PagoService pagoService, EmailService emailService) {
        this.pagoService = pagoService;
        this.emailService = emailService;
    }

    /**
     * Registra un pago en línea a partir de una transacción aprobada.
     * Construye el PagoRequest con los datos de la transacción y delega en PagoService.registrar().
     * Envía email de confirmación de reserva al titular con desglose de total, pagado y saldo.
     * El actor de auditoría es el email del titular de la reserva (quien realmente pagó).
     *
     * @param transaccion transacción aprobada en la pasarela de pago
     * @return el pago registrado con su código de recibo
     */
    @Transactional
    public PagoResponse registrarPagoOnline(TransaccionPago transaccion) {
        // FIX audit: usar titular.usuario.email / username (Google) como actor real, no literal WOMPI
        // Wompi es solo canal (detalle), la identidad es quien pagó
        String auditEmail = null;
        Reserva reserva = transaccion.getReserva();
        if (reserva != null && reserva.getTitularReserva() != null && reserva.getTitularReserva().getUsuario() != null) {
            auditEmail = reserva.getTitularReserva().getUsuario().getEmail();
        }
        String referencia = transaccion.getReferenciaExterna() != null ? transaccion.getReferenciaExterna() : "—";
        String metodo = transaccion.getMetodoPago() != null ? transaccion.getMetodoPago().name() : "—";
        String gatewayName = transaccion.getGateway() != null ? transaccion.getGateway().name() : "ONLINE";
        PagoRequest request = new PagoRequest(
                reserva != null ? reserva.getId() : null,
                transaccion.getMonto(),
                transaccion.getTipoPago(),
                transaccion.getMetodoPago(),
                referencia,
                String.format("Pago en línea aprobado — Canal: %s | Método: %s | Referencia: %s | Gateway: %s",
                        gatewayName, metodo, referencia, gatewayName));
        PagoResponse pago = pagoService.registrar(request, auditEmail);

        // ÚNICO email del flujo WEB: confirmación con desglose total/pagado/saldo (backend fuente de verdad)
        try {
            if (reserva != null && reserva.getTitularReserva() != null
                    && reserva.getTitularReserva().getUsuario() != null
                    && reserva.getTitularReserva().getUsuario().getEmail() != null) {
                String emailTitular = reserva.getTitularReserva().getUsuario().getEmail();
                String nombreCompleto = reserva.getTitularReserva().getNombreCompleto()
                        + " " + reserva.getTitularReserva().getApellidoCompleto();
                var habitaciones = reserva.getDetalles() != null
                        ? reserva.getDetalles().stream()
                            .map(d -> new ReservaConfirmadaData.HabitacionInfo(
                                    d.getHabitacion() != null && d.getHabitacion().getTipo() != null
                                            ? d.getHabitacion().getTipo().getNombre() : "Habitación",
                                    d.getHabitacion() != null ? d.getHabitacion().getNumero() : "N/A"))
                            .toList()
                        : java.util.List.<ReservaConfirmadaData.HabitacionInfo>of();
                java.math.BigDecimal total = reserva.getTotal() != null ? reserva.getTotal() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal pagado = transaccion.getMonto() != null ? transaccion.getMonto() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal saldo = total.subtract(pagado);
                if (saldo.compareTo(java.math.BigDecimal.ZERO) < 0) saldo = java.math.BigDecimal.ZERO;
                emailService.sendReservationConfirmation(new ReservaConfirmadaData(
                        emailTitular,
                        "Reserva confirmada - " + reserva.getCodigoReserva(),
                        nombreCompleto,
                        reserva.getCodigoReserva(),
                        reserva.getFechaCheckIn(),
                        reserva.getFechaCheckOut(),
                        reserva.calcularNoches(),
                        total,
                        pagado,
                        saldo,
                        habitaciones,
                        reserva.getEstado().name()));
            }
        } catch (Exception ex) {
            log.warn("No se pudo enviar email de confirmación tras pago online para reserva {}: {}",
                    transaccion.getReserva().getId(), ex.getMessage());
        }

        return pago;
    }
}
