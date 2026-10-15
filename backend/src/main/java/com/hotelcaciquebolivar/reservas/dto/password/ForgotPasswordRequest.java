package com.hotelcaciquebolivar.reservas.dto.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para solicitar un código de recuperación de contraseña.
 * Contiene el email del usuario que solicita el restablecimiento.
 */
public record ForgotPasswordRequest(
    @NotBlank @Email String email
) {}
