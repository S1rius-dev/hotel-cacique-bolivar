package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Enumeración que define los tipos de pasarelas de pago soportadas por el sistema.
 */
public enum PaymentGatewayType {
    /** Pasarela de pago Wompi (sandbox y producción). */
    WOMPI,
    /** Pasarela de pago Bold. */
    BOLD,
    /** Pasarela de pago Mercado Pago. */
    MERCADO_PAGO,
    /** Pasarela de pago PayU / Rapyd. */
    PAYU_RAPYD
}
