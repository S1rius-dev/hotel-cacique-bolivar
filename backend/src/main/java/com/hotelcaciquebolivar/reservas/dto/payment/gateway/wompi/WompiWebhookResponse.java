package com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi;

/**
 * DTO de respuesta para las notificaciones de webhook de Wompi.
 * Se retorna como confirmación de recepción del webhook.
 */
public record WompiWebhookResponse(String status) {

    /**
     * Crea una respuesta de confirmación exitosa del webhook.
     *
     * @return respuesta con estado "ok"
     */
    public static WompiWebhookResponse ok() {
        return new WompiWebhookResponse("ok");
    }
}
