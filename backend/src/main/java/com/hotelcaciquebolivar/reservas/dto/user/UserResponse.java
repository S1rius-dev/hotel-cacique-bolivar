package com.hotelcaciquebolivar.reservas.dto.user;

import java.time.LocalDateTime;
import java.util.Set;

import com.hotelcaciquebolivar.reservas.entity.auth.AuthProvider;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;

/**
 * DTO de respuesta que representa un usuario completo con toda su información.
 * Incluye datos de autenticación, proveedor, roles, fechas de actividad y referencias a cliente y empleado asociados.
 */
public record UserResponse(
        Long id,
        String email,
        String username,
        Boolean activo,
        AuthProvider provider,
        String providerId,
        String profilePicture,
        Set<Rol> roles,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long clienteId,
        Long empleadoId
        ) {

}
