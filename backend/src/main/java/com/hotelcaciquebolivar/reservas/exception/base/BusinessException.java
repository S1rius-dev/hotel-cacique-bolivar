package com.hotelcaciquebolivar.reservas.exception.base;

import org.springframework.http.HttpStatus;

/**
 * Clase base abstracta para todas las excepciones de negocio de la aplicación.
 * Proporciona un código de error lógico y un estado HTTP asociado.
 * Las excepciones concretas que extienden esta clase representan errores específicos
 * del dominio del hotel (reservaciones, pagos, autenticación, etc.).
 * Hereda de {@link RuntimeException} por lo que es una excepción no controlada.
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    /**
     * Constructor de BusinessException.
     *
     * @param errorCode código de error lógico único para el tipo de excepción
     * @param message   mensaje descriptivo del error en español
     * @param status    estado HTTP que se debe retornar al cliente
     */
    protected BusinessException(
            String errorCode,
            String message,
            HttpStatus status
    ) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
