package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.PrecioTemporal;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link PrecioTemporal} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(PrecioTemporalRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toResponse(PrecioTemporal)} - Entidad a DTO de respuesta con info del tipo de habitacion.</li>
 *   <li>{@link #updateFromDto(PrecioTemporalRequest, PrecioTemporal)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface PrecioTemporalMapper {

    /**
     * Convierte un DTO de solicitud {@link PrecioTemporalRequest} en una nueva entidad {@link PrecioTemporal}.
     *
     * <p>El campo {@code id} se ignora porque se asigna durante la persistencia. El {@code tipoHabitacion.id}
     * se establece a partir del {@code tipoHabitacionId} proporcionado en el DTO.</p>
     *
     * @param dto el DTO de solicitud con los datos del precio temporal.
     * @return una nueva entidad {@link PrecioTemporal} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion.id", source = "tipoHabitacionId")
    PrecioTemporal toEntity(PrecioTemporalRequest dto);

    /**
     * Convierte una entidad {@link PrecioTemporal} en un DTO de respuesta {@link PrecioTemporalResponse}.
     *
     * <p>Extrae el {@code tipoHabitacionId} y {@code tipoHabitacionNombre} del tipo de habitacion asociado.</p>
     *
     * @param precioTemporal la entidad de precio temporal a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "tipoHabitacionId", source = "tipoHabitacion.id")
    @Mapping(target = "tipoHabitacionNombre", source = "tipoHabitacion.nombre")
    PrecioTemporalResponse toResponse(PrecioTemporal precioTemporal);

    /**
     * Actualiza una entidad {@link PrecioTemporal} existente con los valores no nulos de un DTO {@link PrecioTemporalRequest}.
     *
     * <p>Se ignoran los campos {@code id} y {@code tipoHabitacion} para preservar su estado actual.</p>
     *
     * @param dto            el DTO con los campos a actualizar.
     * @param precioTemporal la entidad objetivo que sera actualizada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion", ignore = true)
    void updateFromDto(PrecioTemporalRequest dto, @MappingTarget PrecioTemporal precioTemporal);
}
