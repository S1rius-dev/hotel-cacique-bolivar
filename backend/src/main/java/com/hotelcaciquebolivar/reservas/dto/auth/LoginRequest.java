package com.hotelcaciquebolivar.reservas.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para autenticación de usuario con email y contraseña.
 * Se utiliza en el endpoint de inicio de sesión estándar.
 */
public record LoginRequest(
        @NotBlank(message = "{LoginRequest.email.notblank}")
        @Email(message = "{LoginRequest.email.format}")
        String email,
        @NotBlank(message = "{LoginRequest.password.notblank}")
        String password
        ) {

}
