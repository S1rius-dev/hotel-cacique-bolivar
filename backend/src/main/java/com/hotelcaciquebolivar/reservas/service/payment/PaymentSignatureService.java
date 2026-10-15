package com.hotelcaciquebolivar.reservas.service.payment;

/**
 * Interfaz para la verificación de firmas de seguridad en notificaciones webhook
 * recibidas de pasarelas de pago.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PaymentSignatureService {

    /**
     * Verifica la firma de un webhook recibido contra el payload crudo.
     *
     * @param firma firma recibida en el header del webhook
     * @param payloadCrudo cuerpo JSON del webhook sin procesar
     * @return true si la firma es válida, false en caso contrario
     */
    boolean verificar(String firma, String payloadCrudo);
}
