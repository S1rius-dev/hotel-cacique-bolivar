package com.hotelcaciquebolivar.reservas.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClientePublicResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Cliente} y sus DTOs correspondientes.
 *
 * <p>Proporciona mapeos para los flujos de administrador y cliente:</p>
 * <ul>
 *   <li>{@link #toEntity(ClienteRequest)} - DTO de solicitud a entidad (uso administrador, creacion).</li>
 *   <li>{@link #toAdminResponse(Cliente)} - Entidad a DTO de respuesta administrativa con info de usuario y reservas.</li>
 *   <li>{@link #updateFromAdminDto(ClienteRequest, Cliente)} - Actualizacion completa desde admin.</li>
 *   <li>{@link #updateFromUpdateDto(ClienteUpdateRequest, Cliente)} - Actualizacion parcial desde admin.</li>
 *   <li>{@link #toEntityFromPublic(ClienteRequest)} - DTO de solicitud a entidad (uso cliente-facing, creacion).</li>
 *   <li>{@link #toClienteResponse(Cliente)} - Entidad a DTO de respuesta publica del cliente.</li>
 * </ul>
 *
 * <p>El metodo personalizado {@link #mapReservasIds(List)} extrae los identificadores de las reservas asociadas.</p>
 */
@Mapper(componentModel = "spring")
public interface ClienteMapper {

    /**
     * Convierte un DTO de solicitud {@link ClienteRequest} en una nueva entidad {@link Cliente} (flujo administrador).
     *
     * <p>Los campos {@code id}, {@code createdAt}, {@code reservas}, {@code usuario} y {@code activo} se ignoran
     * porque se asignan durante la persistencia o por logica de negocio.</p>
     *
     * @param dto el DTO de solicitud con los datos del cliente.
     * @return una nueva entidad {@link Cliente} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Cliente toEntity(ClienteRequest dto);

    /**
     * Convierte una entidad {@link Cliente} en un DTO de respuesta administrativa {@link ClienteResponse}.
     *
     * <p>Extrae informacion del usuario asociado ({@code usuarioId}, {@code usuarioUsername}, {@code usuarioEmail})
     * y una lista de identificadores de reservas asociadas.</p>
     *
     * @param cliente la entidad de cliente a convertir.
     * @return el DTO de respuesta administrativa con los campos mapeados.
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "reservasIds", source = "reservas", qualifiedByName = "mapReservasIds")
    ClienteResponse toAdminResponse(Cliente cliente);

    /**
     * Actualiza una entidad {@link Cliente} existente con todos los valores no nulos de un DTO {@link ClienteRequest} (flujo administrador completo).
     *
     * <p>Se ignoran los campos {@code id}, {@code createdAt}, {@code reservas}, {@code usuario} y {@code activo}
     * para preservar su estado actual.</p>
     *
     * @param dto     el DTO con los campos a actualizar.
     * @param cliente la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateFromAdminDto(ClienteRequest dto, @MappingTarget Cliente cliente);

    /**
     * Actualiza una entidad {@link Cliente} existente con valores no nulos de un DTO {@link ClienteUpdateRequest} (actualizacion parcial).
     *
     * <p>Se ignoran los campos {@code id}, {@code createdAt}, {@code reservas}, {@code usuario},
     * {@code cedula}, {@code tipoIdentificacion} y {@code municipioExpedicion} para proteger datos sensibles.</p>
     *
     * @param dto     el DTO con los campos a actualizar.
     * @param cliente la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "tipoIdentificacion", ignore = true)
    @Mapping(target = "municipioExpedicion", ignore = true)
    void updateFromUpdateDto(ClienteUpdateRequest dto, @MappingTarget Cliente cliente);

    /**
     * Convierte un DTO de solicitud {@link ClienteRequest} en una nueva entidad {@link Cliente} (flujo cliente-facing).
     *
     * <p>Los campos {@code id}, {@code createdAt}, {@code reservas}, {@code usuario} y {@code activo} se ignoran.</p>
     *
     * @param dto el DTO de solicitud con los datos del cliente.
     * @return una nueva entidad {@link Cliente} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Cliente toEntityFromPublic(ClienteRequest dto);

    /**
     * Convierte una entidad {@link Cliente} en un DTO de respuesta publica {@link ClientePublicResponse}.
     *
     * <p>Mapea el {@code id} del cliente y el {@code email} del usuario asociado.</p>
     *
     * @param cliente la entidad de cliente a convertir.
     * @return el DTO de respuesta publica con los campos mapeados.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "usuario.email")
    ClientePublicResponse toClienteResponse(Cliente cliente);

    /**
     * Metodo personalizado que extrae los identificadores de una lista de reservas.
     *
     * @param reservas la lista de entidades {@link Reserva}.
     * @return una lista de identificadores de reserva, o una lista vacia si la entrada es nula.
     */
    @Named("mapReservasIds")
    default List<Long> mapReservasIds(List<Reserva> reservas) {
        if (reservas == null) {
            return List.of();
        }
        return reservas.stream()
                .map(Reserva::getId)
                .toList();
    }
}
