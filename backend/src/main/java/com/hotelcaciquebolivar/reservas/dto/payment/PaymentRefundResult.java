package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

/**
 * DTO que representa el resultado de una operación de reembolso de pago.
 * Contiene la referencia externa, el estado del reembolso y el monto reembolsado.
 */
public record PaymentRefundResult(
        /** Referencia externa de la transacción original. */
        String referenciaExterna,
        /** Estado del reembolso en la pasarela de pago. */
        PaymentStatus estado,
        /** Monto reembolsado. */
        BigDecimal monto
) {
}
