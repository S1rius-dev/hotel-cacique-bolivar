package com.hotelcaciquebolivar.reservas.exception.payment;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando ocurre un error al comunicarse con la pasarela de pagos
 * externa (ej. Wompi). Indica fallo en la conexión, timeout, o respuesta inesperada
 * del gateway. Retorna error HTTP 502 (Bad Gateway).
 * Hereda de {@link BusinessException} con código de error "PAYMENT_GATEWAY_ERROR".
 */
public class PaymentGatewayException extends BusinessException {

    /**
     * Constructor de PaymentGatewayException sin causa raíz.
     *
     * @param mensaje descripción del error en la pasarela de pagos
     */
    public PaymentGatewayException(String mensaje) {
        super("PAYMENT_GATEWAY_ERROR", mensaje, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Constructor de PaymentGatewayException con causa raíz.
     *
     * @param mensaje descripción del error en la pasarela de pagos
     * @param causa   excepción original que causó el error
     */
    public PaymentGatewayException(String mensaje, Throwable causa) {
        super("PAYMENT_GATEWAY_ERROR", mensaje, HttpStatus.BAD_GATEWAY);
        initCause(causa);
    }
}
