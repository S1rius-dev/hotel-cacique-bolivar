package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Enumeración que define los estados posibles de un pago.
 */
public enum EstadoPago {
    /** El pago ha sido procesado exitosamente. */
    COMPLETADO,
    /** El pago está pendiente de procesamiento. */
    PENDIENTE,
    /** El pago ha sido reembolsado al cliente. */
    REEMBOLSADO,
    /** El pago ha sido cancelado. */
    CANCELADO
}
