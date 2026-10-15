package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de solicitud para iniciar el proceso de checkout de pago.
 * Contiene la información necesaria para procesar un pago asociado a una reserva.
 */
public record CheckoutRequest(
        /** Identificador de la reserva asociada al pago. */
        @NotNull Long reservaId,
        /** Monto a cobrar, debe ser mayor que cero. */
        @NotNull @Positive BigDecimal monto,
        /** Método de pago utilizado (ej: efectivo, tarjeta, transferencia). */
        MetodoPago metodoPago,
        /** Observaciones adicionales sobre el pago. */
        String observaciones
) {
}
