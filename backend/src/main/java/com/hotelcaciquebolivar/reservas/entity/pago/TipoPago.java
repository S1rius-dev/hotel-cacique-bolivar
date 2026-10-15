package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Enumeración que define los tipos de pago que pueden registrarse.
 */
public enum TipoPago {
    /** Pago total de la reserva. */
    TOTAL,
    /** Pago parcial de la reserva. */
    PARCIAL,
    /** Anticipo para reservar una habitación. */
    ANTICIPO,
    /** Depósito de garantía que se reembolsa al finalizar la estadía. */
    DEPOSITO_GARANTIA
}
