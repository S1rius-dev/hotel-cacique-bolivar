package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;

/**
 * DTO que representa el estado diario de una habitación para el panel de administración.
 * Incluye información de precios, capacidad y reserva activa si existe.
 */
public record HabitacionEstadoDiario(
        /** Identificador único de la habitación. */
        Long id,
        /** Número de la habitación. */
        String numero,
        /** Nombre del tipo de habitación. */
        String tipoNombre,
        /** Capacidad base de la habitación. */
        int capacidadBase,
        /** Capacidad máxima de la habitación. */
        int capacidadMaxima,
        /** Estado actual de la habitación (disponible, ocupada, mantenimiento, etc.). */
        EstadoHabitacion estado,
        /** Precio base por noche. */
        BigDecimal precioBase,
        /** Precio por persona extra por noche. */
        BigDecimal precioPersonaExtra,
        /** Precio efectivo por noche (con ajustes aplicados). */
        BigDecimal precioNocheEfectivo,
        /** Precio efectivo por persona extra (con ajustes aplicados). */
        BigDecimal precioPersonaExtraEfectivo,
        /** Reserva activa en la habitación (null si está disponible). */
        ReservaActiva reservaActiva
) {
}
