package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

/**
 * DTO de respuesta que contiene el estado completo de un pago asociado a una reserva.
 * Incluye información de la pasarela de pago, montos pagados y saldo pendiente.
 */
public record PaymentStatusResponse(
        /** Identificador de la reserva asociada. */
        Long reservaId,
        /** Código único de la reserva. */
        String codigoReserva,
        /** Estado del pago en la pasarela de pago externa. */
        PaymentStatus estadoPasarela,
        /** Estado del pago en el sistema interno. */
        EstadoPago estadoPago,
        /** Monto del anticipo requerido. */
        BigDecimal montoAnticipo,
        /** Monto total de la reserva. */
        BigDecimal totalReserva,
        /** Monto total ya pagado. */
        BigDecimal totalPagado,
        /** Saldo pendiente de pago. */
        BigDecimal saldoPendiente,
        /** Referencia externa de la transacción. */
        String referenciaExterna,
        /** Código del pago generado. */
        String codigoPago,
        /** Fecha y hora de expiración del pago. */
        java.time.LocalDateTime fechaExpiracionPago,
        /** Segundos restantes antes de la expiración del pago. */
        Long segundosRestantes
) {
}
