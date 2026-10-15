package com.hotelcaciquebolivar.reservas.dto.payment.gateway.bold;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que representa una notificación webhook de Bold.
 * Estructura flexible para tolerar variaciones del payload Bold
 * (embedded checkout notifica venta aprobada con referencia/estado).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BoldWebhookRequest(
        /** ID de la notificación (UUID). */
        String id,
        /** Tipo de evento (SALE_APPROVED, SALE_REJECTED, VOID_APPROVED, VOID_REJECTED). */
        String type,
        /** ID de la transacción (payment_id). */
        String subject,
        /** Recurso origen (/payments, /payments/links, etc). */
        String source,
        /** Versión spec CloudEvents. */
        String spec_version,
        /** Tiempo POSIX nanos. */
        Long time,
        /** Evento alternativo (algunas variantes usan "event"). */
        String event,
        /** Datos asociados al evento. */
        Map<String, Object> data,
        /** Firma de integridad si viene en body. */
        String signature,
        /** Checksum alternativo. */
        String hash,
        /** Timestamp alternativo. */
        Long timestamp,
        /** Entorno (sandbox/production). */
        String environment,
        /** Content type. */
        String datacontenttype
) {
}
