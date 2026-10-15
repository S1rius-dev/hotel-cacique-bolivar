package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoRequest;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoResponse;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.empleado.Empleado;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Empleado} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(EmpleadoRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toAdminResponse(Empleado)} - Entidad a DTO de respuesta administrativa con info de usuario.</li>
 *   <li>{@link #updateFromAdminDto(EmpleadoUpdateRequest, Empleado)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 *
 * <p>El mapeo del usuario se realiza mediante el metodo personalizado {@link #mapUserFromId(Long)},
 * que construye una referencia parcial del usuario a partir de su identificador.</p>
 */
@Mapper(componentModel = "spring")
public interface EmpleadoMapper {

    /**
     * Convierte un DTO de solicitud {@link EmpleadoRequest} en una nueva entidad {@link Empleado}.
     *
     * <p>El campo {@code id} se ignora porque se asigna durante la persistencia. El {@code usuario} se
     * construye a partir del {@code usuarioId} proporcionado.</p>
     *
     * @param dto el DTO de solicitud con los datos del empleado.
     * @return una nueva entidad {@link Empleado} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "mapUserFromId")
    Empleado toEntity(EmpleadoRequest dto);

    /**
     * Convierte una entidad {@link Empleado} en un DTO de respuesta administrativa {@link EmpleadoResponse}.
     *
     * <p>Extrae el {@code usuarioId}, {@code usuarioUsername} y {@code usuarioEmail} del usuario asociado.</p>
     *
     * @param empleado la entidad de empleado a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.username", target = "usuarioUsername")
    @Mapping(source = "usuario.email", target = "usuarioEmail")
    EmpleadoResponse toAdminResponse(Empleado empleado);

    /**
     * Actualiza una entidad {@link Empleado} existente con los valores no nulos de un DTO {@link EmpleadoUpdateRequest}.
     *
     * <p>Se ignoran los campos {@code id} para preservar su estado actual. El {@code usuario} se
     * reconstruye a partir del {@code usuarioId} proporcionado en el DTO.</p>
     *
     * @param dto     el DTO con los campos a actualizar.
     * @param empleado la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "mapUserFromId")
    void updateFromAdminDto(EmpleadoUpdateRequest dto, @MappingTarget Empleado empleado);

    /**
     * Metodo personalizado que construye una referencia parcial de {@link User} a partir de su identificador.
     *
     * <p>Crea un objeto {@link User} con solo el campo {@code id} establecido, util para mapeos
     * donde solo se necesita la referencia de la FK en la entidad.</p>
     *
     * @param usuarioId el identificador del usuario.
     * @return una referencia parcial del usuario, o {@code null} si el id es nulo.
     */
    @Named("mapUserFromId")
    default User mapUserFromId(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }

        User user = new User();
        user.setId(usuarioId);
        return user;
    }
}
