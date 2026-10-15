package com.hotelcaciquebolivar.reservas.dto.dashboard;

/**
 * DTO que representa la información resumida de la última reserva registrada.
 * Se utiliza en el dashboard para mostrar la actividad reciente del sistema.
 */
public record UltimaReserva(
        Long id,
        String codigoReserva,
        String titularNombre,
        String habitacionNumero,
        String tipoHabitacion,
        String tipo,
        String fecha
) {
}
