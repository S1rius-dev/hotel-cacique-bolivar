package com.hotelcaciquebolivar.reservas.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para restablecer la contraseña de un usuario.
 * Contiene el email, el código de verificación y la nueva contraseña (mínimo 6 caracteres).
 */
public record ResetPasswordRequest(
    @NotBlank String email,
    @NotBlank String codigo,
    @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password
) {}
