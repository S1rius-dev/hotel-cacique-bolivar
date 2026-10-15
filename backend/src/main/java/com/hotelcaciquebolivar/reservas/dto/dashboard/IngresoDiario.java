package com.hotelcaciquebolivar.reservas.dto.dashboard;

/**
 * DTO que representa el ingreso total de un día específico.
 * Se utiliza como elemento de la serie de ingresos diarios en el dashboard.
 */
public record IngresoDiario(
        String fecha,
        long total
) {
}
