package com.hotelcaciquebolivar.reservas.entity.reserva;

/**
 * Enumeración que define los tipos de ajuste que pueden aplicarse a una reserva.
 */
public enum TipoAjuste {
    /** Cargo adicional que incrementa el total de la reserva. */
    CARGO,
    /** Descuento que reduce el total de la reserva. */
    DESCUENTO,
    /** Cortesía o cortesía especial que reduce o elimina un cargo. */
    CORTESIA
}
