package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;

/**
 * DTO de solicitud para extender la estancia de una reserva.
 * Permite modificar la fecha de check-out a una fecha futura.
 */
public record ExtenderEstadiaRequest(
        /** Nueva fecha de check-out deseada (debe ser futura). */
        @Future(message = "La nueva fecha de check-out debe estar en el futuro")
        LocalDate nuevaFechaCheckOut
) {
}
