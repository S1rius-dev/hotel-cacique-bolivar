package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.BloqueoReserva;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link BloqueoReserva} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toResponse(BloqueoReserva)} - Entidad a DTO de respuesta.</li>
 *   <li>{@link #toEntity(BloqueoReservaRequest)} - DTO de solicitud a nueva entidad (id, createdAt, updatedAt ignorados).</li>
 *   <li>{@link #updateFromDto(BloqueoReservaRequest, BloqueoReserva)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface BloqueoReservaMapper {

    /**
     * Convierte una entidad {@link BloqueoReserva} en un DTO de respuesta {@link BloqueoReservaResponse}.
     *
     * @param bloqueo la entidad de bloqueo de reserva a convertir.
     * @return el DTO de respuesta con los campos mapeados directamente.
     */
    BloqueoReservaResponse toResponse(BloqueoReserva bloqueo);

    /**
     * Convierte un DTO de solicitud {@link BloqueoReservaRequest} en una nueva entidad {@link BloqueoReserva}.
     *
     * <p>Los campos {@code id}, {@code createdAt} y {@code updatedAt} se ignoran porque se asignan
     * durante la persistencia. El campo {@code activo} tiene un valor por defecto de {@code true}.</p>
     *
     * @param dto el DTO de solicitud con los datos del bloqueo.
     * @return una nueva entidad {@link BloqueoReserva} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", defaultValue = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BloqueoReserva toEntity(BloqueoReservaRequest dto);

    /**
     * Actualiza una entidad {@link BloqueoReserva} existente con los valores no nulos de un DTO {@link BloqueoReservaRequest}.
     *
     * <p>Se ignoran los campos {@code id}, {@code createdAt} y {@code updatedAt} para preservar su estado actual.
     * Los valores nulos en el DTO no sobrescriben los valores existentes en la entidad.</p>
     *
     * @param dto     el DTO con los campos a actualizar.
     * @param bloqueo la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(BloqueoReservaRequest dto, @MappingTarget BloqueoReserva bloqueo);
}
