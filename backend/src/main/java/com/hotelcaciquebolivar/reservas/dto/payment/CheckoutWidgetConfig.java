package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

/**
 * DTO de configuración para el widget de pago del checkout.
 * Contiene los parámetros necesarios para inicializar el widget de pago en el frontend.
 */
public record CheckoutWidgetConfig(
        /** Referencia externa de la transacción. */
        String referenciaExterna,
        /** Monto a cobrar. */
        BigDecimal monto,
        /** Monto en centavos para la pasarela de pago. */
        long amountInCents,
        /** Código de moneda. */
        String moneda,
        /** Llave pública del widget de pago. */
        String publicKey,
        /** Firma de integridad para validación del widget. */
        String signatureIntegrity,
        /** URL de redirección después del pago. */
        String redirectUrl
) {
}
