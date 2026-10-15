package com.hotelcaciquebolivar.reservas.dto.password;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para verificar un código de recuperación de contraseña.
 * Contiene el email y el código de verificación a validar.
 */
public record VerifyResetCodeRequest(
    @NotBlank String email,
    @NotBlank String codigo
) {}
