package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hotelcaciquebolivar.reservas.dto.auth.LoginResponse;
import com.hotelcaciquebolivar.reservas.dto.auth.RegisterRequest;
import com.hotelcaciquebolivar.reservas.entity.auth.User;

/**
 * Mapper MapStruct para operaciones de autenticacion y registro de usuarios.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toUserFromRegister(RegisterRequest, String)} - DTO de registro a entidad User,
 *       estableciendo la contrasena codificada y el estado activo.</li>
 *   <li>{@link #toAuthResponse(String, String, String)} - Construye un LoginResponse con token, rol y refresh token.</li>
 *   <li>{@link #toAuthResponse(String, String)} - Construye un LoginResponse con token y rol (sin refresh token).</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    /**
     * Convierte un DTO de registro {@link RegisterRequest} en una entidad {@link User}.
     *
     * <p>Se ignoran campos como {@code id}, {@code username}, {@code roles}, {@code createdAt},
     * {@code updatedAt}, {@code lastLogin}, {@code cliente}, {@code empleado}, {@code provider},
     * {@code providerId} y {@code version} porque se asignan durante la persistencia o por logica de negocio.
     * El campo {@code password} se mapea desde {@code encodedPassword} del DTO.</p>
     *
     * @param request         el DTO de registro con los datos del usuario.
     * @param encodedPassword la contrasena ya codificada (BCrypt) que se asignara al campo password.
     * @return una nueva entidad {@link User} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "empleado", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toUserFromRegister(RegisterRequest request, String encodedPassword);

    /**
     * Construye un DTO de respuesta de login {@link LoginResponse} con access token, rol y refresh token.
     *
     * @param accessToken  el token de acceso JWT generado.
     * @param role         el rol del usuario autenticado.
     * @param refreshToken el token de refresco generado.
     * @return un DTO {@link LoginResponse} con los tres campos establecidos.
     */
    default LoginResponse toAuthResponse(String accessToken, String role, String refreshToken) {
        return new LoginResponse(accessToken, role, refreshToken);
    }

    /**
     * Construye un DTO de respuesta de login {@link LoginResponse} con access token y rol.
     *
     * @param accessToken el token de acceso JWT generado.
     * @param role        el rol del usuario autenticado.
     * @return un DTO {@link LoginResponse} con access token y rol establecidos.
     */
    default LoginResponse toAuthResponse(String accessToken, String role) {
        return new LoginResponse(accessToken, role);
    }
}
