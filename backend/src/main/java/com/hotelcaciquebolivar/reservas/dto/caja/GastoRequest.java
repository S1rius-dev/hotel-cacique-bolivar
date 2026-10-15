package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO de solicitud para registrar un gasto en la caja.
 * Contiene el concepto, monto y observaciones del gasto.
 */
public record GastoRequest(
        @NotBlank(message = "El concepto es obligatorio")
        String concepto,
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal monto,
        String observaciones) {

}
