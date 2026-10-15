package com.hotelcaciquebolivar.reservas.dto.dashboard;

/**
 * DTO de respuesta que resume las métricas principales del dashboard.
 * Contiene habitaciones ocupadas, total de habitaciones, reservas activas, ingresos del día y huéspedes del día.
 */
public record DashboardResumen(
        int habitacionesOcupadas,
        int totalHabitaciones,
        int reservasActivas,
        long ingresosHoy,
        int huespedesHoy
) {
}
