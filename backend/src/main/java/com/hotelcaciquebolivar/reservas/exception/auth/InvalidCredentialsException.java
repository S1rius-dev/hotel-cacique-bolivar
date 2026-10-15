package com.hotelcaciquebolivar.reservas.exception.auth;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando un usuario intenta autenticarse con credenciales inválidas
 * (correo o contraseña incorrectos). Retorna error HTTP 401 (Unauthorized).
 * Hereda de {@link BusinessException} con código de error "INVALID_CREDENTIALS".
 */
public class InvalidCredentialsException extends BusinessException {

    /**
     * Constructor por defecto con mensaje genérico de credenciales inválidas.
     */
    public InvalidCredentialsException() {
        this("Correo o contraseña incorrectos");
    }

    /**
     * Constructor con mensaje personalizado de credenciales inválidas.
     *
     * @param message mensaje descriptivo del error de credenciales
     */
    public InvalidCredentialsException(String message) {
        super(
                "INVALID_CREDENTIALS",
                message,
                HttpStatus.UNAUTHORIZED
        );
    }
}
