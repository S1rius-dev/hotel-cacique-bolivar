package com.hotelcaciquebolivar.reservas.dto.user;

import java.util.Set;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la actualización de datos de un usuario existente.
 * Permite modificar email, nombre de usuario, contraseña, roles y estado de actividad.
 */
public record UserUpdateRequest(
        @NotBlank(message = "{UserUpdateRequest.email.notblank}")
        @Email(message = "{UserUpdateRequest.email.format}")
        @Size(max = 120, message = "{UserUpdateRequest.email.size}")
        String email,
        @NotBlank(message = "{UserUpdateRequest.username.notblank}")
        @Size(max = 100, message = "{UserUpdateRequest.username.size}")
        String username,
        @Size(min = 6, max = 255, message = "{UserUpdateRequest.password.size}")
        String password,
        @NotEmpty(message = "{UserUpdateRequest.roles.notempty}")
        Set<Rol> roles,
        Boolean activo
        ) {

}
