package com.hotelcaciquebolivar.reservas.dto.payment;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadata pública de la pasarela de pago activa.
 * Contiene solo información segura para exponer al frontend (sin secretos).
 */
@Schema(description = "Metadata pública de la pasarela de pago activa")
public record PaymentGatewayMetadata(
        @Schema(description = "Tipo de pasarela configurada", example = "BOLD")
        PaymentGatewayType gateway,

        @Schema(description = "Nombre interno de la pasarela", example = "BOLD")
        String name,

        @Schema(description = "Nombre para mostrar en UI", example = "Bold")
        String displayName,

        @Schema(description = "Texto para banner de pago seguro", example = "Pago seguro con Bold")
        String securePaymentText,

        @Schema(description = "Ruta del logo (relativa a assets frontend)", example = "/assets/gateways/bold/Bold_Logo.svg")
        String logoUrl,

        @Schema(description = "Moneda configurada", example = "COP")
        String currency
) {
}
