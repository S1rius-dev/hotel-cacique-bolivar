package com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa los datos de una transacción recibida de la pasarela de pago Wompi.
 * Mapea la estructura JSON de respuesta de Wompi para transacciones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WompiData(
        /** Identificador único de la transacción en Wompi. */
        String id,
        /** Referencia de la transacción. */
        String reference,
        /** Estado de la transacción en Wompi. */
        String status,
        /** Código de moneda de la transacción. */
        String currency,
        /** Monto en centavos de la transacción. */
        @JsonProperty("amount_in_cents") Long amountInCents,
        /** Tipo de método de pago utilizado. */
        @JsonProperty("payment_method_type") String paymentMethodType,
        /** Detalles del método de pago utilizado. */
        @JsonProperty("payment_method") WompiPaymentMethod paymentMethod
) {

    /**
     * DTO interno que representa los detalles del método de pago utilizado en Wompi.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WompiPaymentMethod(
            /** Tipo de método de pago (ej: CARD, PSE, NEQUI). */
            String type,
            /** Número de teléfono asociado al pago (para métodos móviles). */
            @JsonProperty("phone_number") String phoneNumber,
            /** Número de cuotas del pago. */
            String installments,
            /** Información adicional del método de pago. */
            String extra
    ) {
    }
}
