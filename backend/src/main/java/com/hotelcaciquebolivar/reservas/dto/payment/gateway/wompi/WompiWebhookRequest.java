package com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que representa una solicitud de webhook recibida de la pasarela de pago Wompi.
 * Contiene el evento notificado, los datos de la transacción y la firma de integridad.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WompiWebhookRequest(
        /** Tipo de evento notificado por Wompi. */
        String event,
        /** Datos asociados al evento de la transacción. */
        Map<String, Object> data,
        /** Firma de integridad del webhook para validación. */
        WompiWebhookSignature signature,
        /** Marca de tiempo de la notificación. */
        Long timestamp,
        /** Entorno de ejecución (sandbox o production). */
        String environment,
        /** Fecha y hora de envío del webhook. */
        String sent_at
) {
    /**
     * DTO interno que representa la firma de integridad del webhook de Wompi.
     */
    public record WompiWebhookSignature(
        /** Checksum de la firma para validación. */
        String checksum,
        /** Lista de propiedades utilizadas para generar la firma. */
        List<String> properties
    ) {
    }
}
