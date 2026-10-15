package com.hotelcaciquebolivar.reservas.dto.auth;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para autenticación mediante Google OAuth.
 * Contiene el token ID proporcionado por Google para validar la identidad del usuario.
 */
public record GoogleLoginRequest(
        @NotNull(message = "{GoogleLoginRequest.idToken.notnull}")
        String idToken
        ) {

}
