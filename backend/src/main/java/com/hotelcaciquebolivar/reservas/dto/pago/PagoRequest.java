package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.TipoPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de solicitud para registrar un nuevo pago.
 * Contiene el ID de la reserva, monto, tipo de pago, método de pago y opcionalmente referencia externa y observaciones.
 */
public record PagoRequest(
        @NotNull Long reservaId,
        @NotNull @Positive BigDecimal monto,
        @NotNull TipoPago tipoPago,
        @NotNull MetodoPago metodoPago,
        String referenciaExterna,
        String observaciones
) {
}
