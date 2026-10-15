package com.hotelcaciquebolivar.reservas.exception.auth;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando un usuario no autenticado intenta acceder a un recurso
 * que requiere autenticación. Retorna error HTTP 401 (Unauthorized).
 * Hereda de {@link BusinessException} con código de error "UNAUTHORIZED".
 */
public class UnauthorizedException extends BusinessException {

    /**
     * Constructor por defecto con mensaje de autenticación requerida.
     */
    public UnauthorizedException() {
        super(
                "UNAUTHORIZED",
                "Debes iniciar sesión para acceder a este recurso",
                HttpStatus.UNAUTHORIZED
        );
    }
}
