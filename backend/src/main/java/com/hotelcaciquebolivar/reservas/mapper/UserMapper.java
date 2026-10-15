package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.user.UserRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.dto.user.UserUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.auth.User;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link User} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(UserRequest)} - DTO de solicitud a nueva entidad (flujo administrador, creacion).</li>
 *   <li>{@link #toAdminResponse(User)} - Entidad a DTO de respuesta administrativa con IDs de cliente y empleado.</li>
 *   <li>{@link #updateFromAdminDto(UserRequest, User)} - Actualizacion completa desde admin.</li>
 *   <li>{@link #updateFromDto(UserUpdateRequest, User)} - Actualizacion parcial desde admin (sin roles ni password).</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte un DTO de solicitud {@link UserRequest} en una nueva entidad {@link User}.
     *
     * <p>Los campos {@code id}, {@code createdAt}, {@code updatedAt}, {@code lastLogin},
     * {@code cliente} y {@code empleado} se ignoran porque se asignan durante la persistencia.</p>
     *
     * @param dto el DTO de solicitud con los datos del usuario.
     * @return una nueva entidad {@link User} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "empleado", ignore = true)
    User toEntity(UserRequest dto);

    /**
     * Convierte una entidad {@link User} en un DTO de respuesta administrativa {@link UserResponse}.
     *
     * <p>Extrae los {@code clienteId} y {@code empleadoId} de las entidades asociadas.</p>
     *
     * @param user la entidad de usuario a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "empleadoId", source = "empleado.id")
    UserResponse toAdminResponse(User user);

    /**
     * Actualiza una entidad {@link User} existente con todos los valores no nulos de un DTO {@link UserRequest} (actualizacion completa desde admin).
     *
     * <p>Se ignoran los campos {@code id}, {@code createdAt}, {@code updatedAt}, {@code lastLogin},
     * {@code cliente} y {@code empleado} para preservar su estado actual.</p>
     *
     * @param dto  el DTO con los campos a actualizar.
     * @param user la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "empleado", ignore = true)
    void updateFromAdminDto(UserRequest dto, @MappingTarget User user);

    /**
     * Actualiza una entidad {@link User} existente con valores no nulos de un DTO {@link UserUpdateRequest} (actualizacion parcial desde admin).
     *
     * <p>Se ignoran los campos {@code id}, {@code createdAt}, {@code updatedAt}, {@code lastLogin},
     * {@code cliente}, {@code empleado}, {@code roles} y {@code password} para proteger datos sensibles.</p>
     *
     * @param dto  el DTO con los campos a actualizar.
     * @param user la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "empleado", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateFromDto(UserUpdateRequest dto, @MappingTarget User user);
}
