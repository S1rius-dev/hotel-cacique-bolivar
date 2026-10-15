package com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa la firma de integridad de una transacción de Wompi.
 * Se utiliza para validar la autenticidad de las notificaciones de pago.
 */
public record WompiSignature(
        /** Cadena de integridad de la firma. */
        @JsonProperty("integrity") String integrity
) {
}
