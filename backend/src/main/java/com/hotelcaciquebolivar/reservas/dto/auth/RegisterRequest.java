package com.hotelcaciquebolivar.reservas.dto.auth;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para el registro de un nuevo usuario en el sistema.
 * Contiene email, contraseña y rol asignado.
 */
public record RegisterRequest(
        @NotBlank(message = "{RegisterRequest.email.notblank}")
        @Email(message = "{RegisterRequest.email.format}")
        String email,
        @NotBlank(message = "{RegisterRequest.password.notblank}")
        String password,
        Rol role
        ) {

}
