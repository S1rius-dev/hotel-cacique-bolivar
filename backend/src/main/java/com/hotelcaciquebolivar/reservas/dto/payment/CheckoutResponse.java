package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

/**
 * DTO de respuesta que contiene los datos resultantes del proceso de checkout de pago.
 * Incluye información de la transacción, estado del pago y datos para el widget de pago.
 */
public record CheckoutResponse(
        /** Identificador de la transacción interna. */
        Long transaccionId,
        /** Referencia externa generada para la pasarela de pago. */
        String referenciaExterna,
        /** Tipo de pasarela de pago utilizada. */
        PaymentGatewayType gateway,
        /** Monto total del pago. */
        BigDecimal monto,
        /** Monto en centavos para la pasarela de pago. */
        Long amountInCents,
        /** Código de moneda (ej: COP, USD). */
        String moneda,
        /** Estado actual del pago en la pasarela. */
        PaymentStatus estado,
        /** URL de pago directo para la pasarela. */
        String urlPago,
        /** URL de redirección después del pago. */
        String redireccion,
        /** Llave pública del widget de pago. */
        String publicKey,
        /** Firma de integridad para validación del widget. */
        String signatureIntegrity,
        /** URL de redirección configurada para el widget. */
        String redirectUrl,
        /** Fecha y hora de expiración del pago. */
        java.time.LocalDateTime fechaExpiracionPago,
        /** Segundos restantes antes de la expiración del pago. */
        Long segundosRestantes
) {
}
