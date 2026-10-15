package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO de solicitud para marcar múltiples habitaciones como limpias.
 * Contiene la lista de IDs de las habitaciones a procesar.
 */
public record LimpiarHabitacionesRequest(
    @NotEmpty List<Long> habitacionesIds
) {}
