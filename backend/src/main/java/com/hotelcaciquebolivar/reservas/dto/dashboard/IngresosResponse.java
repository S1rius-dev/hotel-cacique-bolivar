package com.hotelcaciquebolivar.reservas.dto.dashboard;

import java.util.List;

/**
 * DTO de respuesta que contiene la serie de ingresos diarios para un rango de fechas.
 * Incluye el rango consultado y la lista de ingresos por día.
 */
public record IngresosResponse(
        String rango,
        List<IngresoDiario> data
) {
}
