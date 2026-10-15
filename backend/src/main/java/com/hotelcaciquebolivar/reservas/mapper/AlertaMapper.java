package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaRequest;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaResponse;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.alerta.Alerta;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Alerta} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(AlertaRequest)} - DTO de solicitud a nueva entidad (id, cliente, estado y fechaCreacion ignorados).</li>
 *   <li>{@link #toResponse(Alerta)} - Entidad a DTO de respuesta, extrayendo informacion del cliente asociado.</li>
 *   <li>{@link #updateFromDto(AlertaUpdateRequest, Alerta)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 *
 * <p>El mapeo del cliente se realiza mediante el metodo personalizado {@link #mapClienteFromId(Long)},
 * que construye una referencia parcial del cliente a partir de su identificador.</p>
 */
@Mapper(componentModel = "spring")
public interface AlertaMapper {

    /**
     * Convierte un DTO de solicitud {@link AlertaRequest} en una nueva entidad {@link Alerta}.
     *
     * <p>Los campos {@code id}, {@code estado} y {@code fechaCreacion} se ignoran porque se asignan
     * durante la persistencia. El {@code cliente} se construye a partir del {@code clienteId} proporcionado.</p>
     *
     * @param dto el DTO de solicitud con los datos de la alerta.
     * @return una nueva entidad {@link Alerta} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", source = "clienteId", qualifiedByName = "mapClienteFromId")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Alerta toEntity(AlertaRequest dto);

    /**
     * Convierte una entidad {@link Alerta} en un DTO de respuesta {@link AlertaResponse}.
     *
     * <p>Extrae el {@code clienteId} y {@code clienteNombre} a partir de la referencia al cliente asociado.</p>
     *
     * @param alerta la entidad de alerta a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nombreCompleto", target = "clienteNombre")
    AlertaResponse toResponse(Alerta alerta);

    /**
     * Actualiza una entidad {@link Alerta} existente con los valores no nulos de un DTO {@link AlertaUpdateRequest}.
     *
     * <p>Se ignoran los campos {@code id}, {@code cliente}, {@code tipo}, {@code mensaje} y {@code fechaCreacion}
     * para preservar su estado actual. Solo se actualizan campos como {@code estado} y {@code fechaActualizacion}.</p>
     *
     * @param dto    el DTO con los campos a actualizar.
     * @param alerta la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "mensaje", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    void updateFromDto(AlertaUpdateRequest dto, @MappingTarget Alerta alerta);

    /**
     * Metodo personalizado que construye una referencia parcial de {@link Cliente} a partir de su identificador.
     *
     * <p>Crea un objeto {@link Cliente} con solo el campo {@code id} establecido, util para mapeos
     * donde solo se necesita la referencia de la FK en la entidad.</p>
     *
     * @param clienteId el identificador del cliente.
     * @return una referencia parcial del cliente, o {@code null} si el id es nulo.
     */
    @Named("mapClienteFromId")
    default Cliente mapClienteFromId(Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        return cliente;
    }
}
