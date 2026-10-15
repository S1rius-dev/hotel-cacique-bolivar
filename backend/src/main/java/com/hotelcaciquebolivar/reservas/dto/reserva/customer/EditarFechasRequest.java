package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para que un cliente edite las fechas de una reserva existente.
 * Permite modificar las fechas de check-in y check-out.
 */
public record EditarFechasRequest(
        /** Nueva fecha de check-in. */
        @NotNull(message = "La fecha de check-in es obligatoria") LocalDate fechaCheckIn,
        /** Nueva fecha de check-out. */
        @NotNull(message = "La fecha de check-out es obligatoria") LocalDate fechaCheckOut
) {}
