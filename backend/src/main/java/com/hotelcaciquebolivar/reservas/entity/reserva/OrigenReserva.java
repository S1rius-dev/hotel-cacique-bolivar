package com.hotelcaciquebolivar.reservas.entity.reserva;

/**
 * Enumeración que define los orígenes por los cuales se puede crear una reserva.
 */
public enum OrigenReserva {
    /** Reserva realizada a través de la página web del hotel. */
    WEB,
    /** Reserva realizada directamente en la recepción del hotel. */
    RECEPCION,
    /** Reserva realizada por teléfono. */
    TELEFONO,
    /** Reserva realizada a través de WhatsApp. */
    WHATSAPP,
    /** Otro origen no especificado. */
    OTRO
}