package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para consultar la disponibilidad de habitaciones en un rango de fechas.
 */
public record DisponibilidadRequest(
        /** Fecha de check-in deseada. */
        @NotNull(message = "{DisponibilidadRequest.checkIn.notnull}")
        LocalDate checkIn,
        /** Fecha de check-out deseada. */
        @NotNull(message = "{DisponibilidadRequest.checkOut.notnull}")
        LocalDate checkOut
) {
}
