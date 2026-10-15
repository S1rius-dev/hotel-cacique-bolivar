package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de solicitud para apertura de un turno de caja.
 * Contiene el fondo inicial con el que se inicia el turno.
 */
public record AbrirTurnoRequest(
        @PositiveOrZero(message = "El fondo inicial debe ser mayor o igual a cero")
        BigDecimal fondoInicial) {

}
