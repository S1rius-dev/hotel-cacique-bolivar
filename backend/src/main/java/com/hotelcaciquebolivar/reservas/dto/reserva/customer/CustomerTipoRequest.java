package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud que representa un tipo de habitación solicitado por un cliente.
 * Contiene el tipo de habitación, cantidad de habitaciones y ocupantes por habitación.
 */
public record CustomerTipoRequest(
    /** Identificador del tipo de habitación solicitado. */
    @NotNull Long tipoHabitacionId,
    /** Cantidad de habitaciones de este tipo (1 a 10). */
    @Min(1) @Max(10) Integer cantidad,
    /** Cantidad de ocupantes por habitación. */
    @NotNull @Min(1) Integer ocupantes
) {}
