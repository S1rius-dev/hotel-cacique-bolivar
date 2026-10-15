package com.hotelcaciquebolivar.reservas.entity.caja;

/**
 * Enumeración que define los estados posibles de un turno de caja.
 */
public enum EstadoTurno {
    /** El turno está activo y puede registrar movimientos. */
    ABIERTO,
    /** El turno ha sido cerrado y no acepta nuevos movimientos. */
    CERRADO
}
