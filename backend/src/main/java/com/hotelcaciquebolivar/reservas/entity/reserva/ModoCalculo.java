package com.hotelcaciquebolivar.reservas.entity.reserva;

/**
 * Enumeración que define los modos de cálculo para los ajustes de reserva.
 */
public enum ModoCalculo {
    /** El ajuste es un valor fijo en la moneda del sistema. */
    FIJO,
    /** El ajuste es un porcentaje sobre el total de la reserva. */
    PORCENTUAL,
    /** El ajuste reemplaza el total de la reserva. */
    TOTAL
}
