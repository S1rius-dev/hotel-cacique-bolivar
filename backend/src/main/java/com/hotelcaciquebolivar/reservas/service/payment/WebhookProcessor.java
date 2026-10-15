package com.hotelcaciquebolivar.reservas.service.payment;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

/**
 * Interfaz para el procesamiento de notificaciones webhook de pasarelas de pago.
 * Define el contrato para validar firmas y despachar notificaciones al servicio de pagos.
 *
 * @author Hotel Cacique Bolívar
 */
public interface WebhookProcessor {

    /**
     * Procesa una notificación webhook de una pasarela de pago.
     * Verifica la firma, normaliza la notificación y confirma la transacción.
     *
     * @param gateway tipo de pasarela de pago
     * @param payloadCrudo cuerpo JSON del webhook
     * @param firma firma de verificación del webhook
     * @return "ok" si el procesamiento fue exitoso
     */
    String procesar(PaymentGatewayType gateway, String payloadCrudo, String firma);
}
