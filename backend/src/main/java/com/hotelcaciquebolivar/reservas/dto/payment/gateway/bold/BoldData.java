package com.hotelcaciquebolivar.reservas.dto.payment.gateway.bold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa datos de transacción de Bold.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BoldData(
        @JsonProperty("id") String id,
        @JsonProperty("payment_id") String paymentId,
        @JsonProperty("orderId") String orderId,
        @JsonProperty("reference") String reference,
        @JsonProperty("status") String status,
        @JsonProperty("currency") String currency,
        @JsonProperty("amount") Long amount,
        @JsonProperty("amount_in_cents") Long amountInCents,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("payment_method_type") String paymentMethodType
) {
    public String effectiveReference() {
        if (reference != null && !reference.isBlank()) return reference;
        if (orderId != null && !orderId.isBlank()) return orderId;
        return null;
    }

    public String effectiveId() {
        if (paymentId != null && !paymentId.isBlank()) return paymentId;
        return id;
    }

    public Long effectiveAmountInCents() {
        if (amountInCents != null) return amountInCents;
        if (amount != null) return amount;
        return null;
    }
}
