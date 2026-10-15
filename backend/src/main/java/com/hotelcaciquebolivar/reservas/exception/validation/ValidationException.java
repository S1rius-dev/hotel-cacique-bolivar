package com.hotelcaciquebolivar.reservas.exception.validation;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se detecta un error de validación en los datos
 * de entrada de una solicitud. Retorna error HTTP 400 (Bad Request).
 * Hereda de {@link BusinessException} con código de error "VALIDATION_ERROR".
 * Proporciona constructores para errores de campo específico o mensajes genéricos.
 */
public class ValidationException extends BusinessException {

    /**
     * Constructor de ValidationException para error de campo específico.
     *
     * @param field  nombre del campo que falló la validación
     * @param reason razón por la cual el campo no es válido
     */
    public ValidationException(String field, String reason) {
        super(
                "VALIDATION_ERROR",
                "El campo '" + field + "' no es válido: " + reason,
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Constructor de ValidationException con mensaje genérico de validación.
     *
     * @param message mensaje descriptivo del error de validación
     */
    public ValidationException(String message) {
        super(
                "VALIDATION_ERROR",
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
