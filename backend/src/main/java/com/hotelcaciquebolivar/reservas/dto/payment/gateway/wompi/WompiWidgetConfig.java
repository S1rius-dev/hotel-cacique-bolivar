package com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de configuración para el widget de pago de Wompi.
 * Contiene los parámetros necesarios para inicializar el widget de Wompi en el frontend.
 */
public record WompiWidgetConfig(
        /** Llave pública de Wompi para autenticación del widget. */
        @JsonProperty("public_key") String publicKey,
        /** Código de moneda de la transacción. */
        @JsonProperty("currency") String currency,
        /** Monto en centavos de la transacción. */
        @JsonProperty("amount_in_cents") long amountInCents,
        /** Referencia única de la transacción. */
        @JsonProperty("reference") String reference,
        /** URL de redirección después del pago. */
        @JsonProperty("redirect_url") String redirectUrl,
        /** Firma de integridad para validación del widget. */
        @JsonProperty("signature") WompiSignature signature
) {
}
