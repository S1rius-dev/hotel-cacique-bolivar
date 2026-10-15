package com.hotelcaciquebolivar.reservas.dto.payment;

import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

/**
 * DTO que representa una notificación de webhook recibida de la pasarela de pago.
 * Contiene la información del evento de pago notificado por la pasarela externa.
 */
public record PaymentWebhookNotification(
        /** Tipo de pasarela de pago que envió la notificación. */
        PaymentGatewayType gateway,
        /** Referencia externa de la transacción notificada. */
        String referenciaExterna,
        /** Estado del pago reportado por la pasarela. */
        PaymentStatus estado,
        /** Clave de idempotencia de la transacción. */
        String idempotenciaKey,
        /** Firma de integridad del webhook para validación. */
        String firma,
        /** Payload crudo del webhook recibido. */
        String payloadCrudo,
        /** Método de pago utilizado en la transacción. */
        MetodoPago metodoPago
) {

    public PaymentWebhookNotification(
            PaymentGatewayType gateway,
            String referenciaExterna,
            PaymentStatus estado,
            String idempotenciaKey,
            String firma,
            String payloadCrudo) {
        this(gateway, referenciaExterna, estado, idempotenciaKey, firma, payloadCrudo, null);
    }
}
