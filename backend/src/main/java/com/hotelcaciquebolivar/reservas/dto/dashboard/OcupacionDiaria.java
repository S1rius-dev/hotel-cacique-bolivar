package com.hotelcaciquebolivar.reservas.dto.dashboard;

/**
 * DTO que representa la ocupación de un día específico.
 * Contiene la fecha, porcentaje de ocupación, habitaciones ocupadas y total de habitaciones.
 */
public record OcupacionDiaria(
        String fecha,
        double porcentaje,
        int ocupadas,
        int total
) {
}
