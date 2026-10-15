package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Enumeración que define los métodos de pago aceptados por el hotel.
 */
public enum MetodoPago {
    /** Pago en efectivo. */
    EFECTIVO,
    /** Pago con tarjeta de crédito. */
    TARJETA_CREDITO,
    /** Pago con tarjeta de débito. */
    TARJETA_DEBITO,
    /** Pago por transferencia bancaria. */
    TRANSFERENCIA,
    /** Pago con cheque. */
    CHEQUE,
    /** Pago mediante Nequi (billetera móvil colombiana). */
    NEQUI,
    /** Pago mediante PSE (Pago Seguro en Línea). */
    PSE,
    /** Otro método de pago no especificado. */
    OTRO
}
