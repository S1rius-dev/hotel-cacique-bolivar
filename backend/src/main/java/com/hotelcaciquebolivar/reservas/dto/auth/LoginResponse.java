package com.hotelcaciquebolivar.reservas.dto.auth;

/**
 * DTO de respuesta devuelto tras una autenticación exitosa.
 * Contiene el token de acceso JWT, el tipo de token, el rol del usuario y opcionalmente un token de refresco.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        String role,
        String refreshToken
) {

    public LoginResponse(String accessToken, String role, String refreshToken) {
        this(accessToken, "Bearer", role, refreshToken);
    }

    public LoginResponse(String accessToken, String role) {
        this(accessToken, "Bearer", role, null);
    }
}
