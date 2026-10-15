package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Enumeración que define los estados posibles de una transacción en la pasarela de pago.
 * Representa los estados devueltos por los gateways de pago externos.
 */
public enum PaymentStatus {
    /** La transacción ha sido creada pero no procesada. */
    CREATED,
    /** La transacción está pendiente de aprobación. */
    PENDING,
    /** La transacción ha sido aprobada por la pasarela de pago. */
    APPROVED,
    /** La transacción ha sido rechazada por la pasarela de pago. */
    REJECTED,
    /** La transacción ha sido anulada. */
    VOIDED,
    /** La transacción fue declinada por el emisor de la tarjeta. */
    DECLINED,
    /** La transacción ha expirado sin completarse. */
    EXPIRED,
    /** Ocurrió un error durante el procesamiento de la transacción. */
    ERROR
}
