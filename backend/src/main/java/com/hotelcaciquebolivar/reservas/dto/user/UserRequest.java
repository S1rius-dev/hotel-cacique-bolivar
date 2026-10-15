package com.hotelcaciquebolivar.reservas.dto.user;

import java.util.Set;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la creación de un nuevo usuario.
 * Contiene email, nombre de usuario, contraseña y conjunto de roles asignados.
 */
public record UserRequest(
        @NotBlank(message = "{UserRequest.email.notblank}")
        @Email(message = "{UserRequest.email.format}")
        @Size(max = 120, message = "{UserRequest.email.size}")
        String email,
        @NotBlank(message = "{UserRequest.username.notblank}")
        @Size(max = 100, message = "{UserRequest.username.size}")
        String username,
        @Size(min = 6, max = 255, message = "{UserRequest.password.size}")
        String password,
        @NotEmpty(message = "{UserRequest.roles.notempty}")
        Set<Rol> roles
        ) {

}
