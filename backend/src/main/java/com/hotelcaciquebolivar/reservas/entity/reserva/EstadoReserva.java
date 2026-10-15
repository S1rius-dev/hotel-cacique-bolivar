package com.hotelcaciquebolivar.reservas.entity.reserva;

/**
 * Enumeración que define los estados posibles de una reserva en el hotel.
 */
public enum EstadoReserva {
    /** La reserva está pendiente de confirmación. */
    PENDIENTE,
    /** La reserva está pendiente de pago para confirmarse. */
    PENDIENTE_PAGO,
    /** La reserva ha sido confirmada con un pago. */
    CONFIRMADA,
    /** La reserva está en curso (el huésped ya hizo check-in). */
    EN_CURSO,
    /** La reserva ha sido finalizada (el huésped hizo check-out). */
    FINALIZADA,
    /** La reserva ha sido cancelada. */
    CANCELADA,
    /** El cliente no se presentó a la reserva. */
    NO_SHOW
}