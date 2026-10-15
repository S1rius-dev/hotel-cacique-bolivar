package com.hotelcaciquebolivar.reservas.exception.payment;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando falla la verificación de la firma de un webhook
 * recibido de la pasarela de pagos. Indica que el webhook no es auténtico o
 * ha sido manipulado. Retorna error HTTP 401 (Unauthorized).
 * Hereda de {@link BusinessException} con código de error "WEBHOOK_VERIFICATION_FAILED".
 */
public class WebhookVerificationException extends BusinessException {

    /**
     * Constructor de WebhookVerificationException.
     *
     * @param mensaje descripción del fallo de verificación del webhook
     */
    public WebhookVerificationException(String mensaje) {
        super("WEBHOOK_VERIFICATION_FAILED", mensaje, HttpStatus.UNAUTHORIZED);
    }
}
