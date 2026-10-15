package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de solicitud para cierre de un turno de caja.
 * Contiene el conteo final de efectivo y observaciones adicionales del cierre.
 */
public record CerrarTurnoRequest(
        @PositiveOrZero(message = "El conteo final debe ser mayor o igual a cero")
        BigDecimal contadoFinal,
        String observaciones) {

}
