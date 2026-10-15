package com.hotelcaciquebolivar.reservas.entity.consumo;

/**
 * Enumeración que define los estados posibles de un consumo de reserva.
 */
public enum EstadoConsumo {
    /** El consumo está registrado pero aún no ha sido facturado. */
    PENDIENTE,
    /** El consumo ha sido incluido en una factura. */
    FACTURADO,
    /** El consumo ha sido anulado y no se cobrará. */
    ANULADO
}
