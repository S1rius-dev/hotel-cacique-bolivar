package com.hotelcaciquebolivar.reservas.entity.reserva;

/**
 * Enumeración que define los estados posibles de un detalle de reserva (habitación específica).
 */
public enum EstadoDetalleReserva {
    /** La habitación está reservada pero aún no ha sido ocupada. */
    RESERVADA,
    /** La habitación está ocupada por un huésped. */
    OCUPADA,
    /** La habitación ha sido liberada después del check-out. */
    LIBERADA,
    /** La reserva de esta habitación ha sido cancelada. */
    CANCELADA
}