package com.hotelcaciquebolivar.reservas.entity.habitacion;

/**
 * Enumeración que define los estados posibles de una habitación del hotel.
 */
public enum EstadoHabitacion {
    /** La habitación está disponible para ser reservada. */
    DISPONIBLE,
    /** La habitación tiene una reserva confirmada pero el huésped aún no ha llegado. */
    RESERVADA,
    /** La habitación está ocupada por un huésped. */
    OCUPADA,
    /** La habitación está en proceso de limpieza después de un check-out. */
    EN_LIMPIEZA,
    /** La habitación está en mantenimiento y no disponible para reservas. */
    MANTENIMIENTO,
    /** La habitación está inhabilitada y no puede ser utilizada. */
    INHABILITADA
}
