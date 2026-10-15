package com.hotelcaciquebolivar.reservas.exception.auth;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando un usuario intenta realizar una operación
 * para la que no tiene permisos. Retorna error HTTP 403 (Forbidden).
 * Hereda de {@link BusinessException} con código de error "FORBIDDEN_OPERATION".
 */
public class ForbiddenOperationException extends BusinessException {

    /**
     * Constructor de ForbiddenOperationException.
     *
     * @param action descripción de la acción que el usuario no está autorizado a realizar
     */
    public ForbiddenOperationException(String action) {
        super(
                "FORBIDDEN_OPERATION",
                "No tienes permisos para " + action,
                HttpStatus.FORBIDDEN
        );
    }
}
