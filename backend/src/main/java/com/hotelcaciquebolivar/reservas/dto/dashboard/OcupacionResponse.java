package com.hotelcaciquebolivar.reservas.dto.dashboard;

import java.util.List;

/**
 * DTO de respuesta que contiene la serie de ocupación diaria para un rango de fechas.
 * Incluye el rango consultado y la lista de registros de ocupación por día.
 */
public record OcupacionResponse(
        String rango,
        List<OcupacionDiaria> data
) {
}
