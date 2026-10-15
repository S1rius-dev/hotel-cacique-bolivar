package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.TipoHabitacionPublicResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link TipoHabitacion} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(TipoHabitacionRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toResponse(TipoHabitacion)} - Entidad a DTO de respuesta con cantidad de habitaciones calculada.</li>
 *   <li>{@link #updateFromDto(TipoHabitacionUpdateRequest, TipoHabitacion)} - Actualizacion parcial de una entidad existente.</li>
 *   <li>{@link #toPublicResponse(TipoHabitacion)} - Entidad a DTO de respuesta publica para clientes.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface TipoHabitacionMapper {

    /**
     * Convierte un DTO de solicitud {@link TipoHabitacionRequest} en una nueva entidad {@link TipoHabitacion}.
     *
     * <p>Los campos {@code id} y {@code habitaciones} se ignoran porque se asignan durante la persistencia.</p>
     *
     * @param dto el DTO de solicitud con los datos del tipo de habitacion.
     * @return una nueva entidad {@link TipoHabitacion} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    TipoHabitacion toEntity(TipoHabitacionRequest dto);

    /**
     * Convierte una entidad {@link TipoHabitacion} en un DTO de respuesta {@link TipoHabitacionResponse}.
     *
     * <p>Calcula la {@code cantidadHabitaciones} a partir del tamanio de la lista de habitaciones asociadas.
     * Se ignora la lista de habitaciones en el DTO de respuesta.</p>
     *
     * @param tipo la entidad de tipo de habitacion a convertir.
     * @return el DTO de respuesta con los campos mapeados y la cantidad de habitaciones.
     */
    @Mapping(target = "cantidadHabitaciones",
            expression = "java(tipo.getHabitaciones() != null ? tipo.getHabitaciones().size() : 0)")
    @Mapping(target = "habitaciones", ignore = true)
    TipoHabitacionResponse toResponse(TipoHabitacion tipo);

    /**
     * Actualiza una entidad {@link TipoHabitacion} existente con los valores no nulos de un DTO {@link TipoHabitacionUpdateRequest}.
     *
     * <p>Se ignoran los campos {@code id} y {@code habitaciones} para preservar su estado actual.</p>
     *
     * @param dto  el DTO con los campos a actualizar.
     * @param tipo la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    void updateFromDto(TipoHabitacionUpdateRequest dto, @MappingTarget TipoHabitacion tipo);

    /**
     * Convierte una entidad {@link TipoHabitacion} en un DTO de respuesta publica {@link TipoHabitacionPublicResponse}.
     *
     * <p>Mapeo directo de todos los campos, util para la vista publica del catalogo de tipos de habitacion.</p>
     *
     * @param tipo la entidad de tipo de habitacion a convertir.
     * @return el DTO de respuesta publica con los campos mapeados directamente.
     */
    TipoHabitacionPublicResponse toPublicResponse(TipoHabitacion tipo);
}
