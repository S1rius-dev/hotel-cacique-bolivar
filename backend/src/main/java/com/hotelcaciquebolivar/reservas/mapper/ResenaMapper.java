package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.resena.ResenaRequest;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.resena.Resena;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Resena} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(ResenaRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toResponse(Resena)} - Entidad a DTO de respuesta.</li>
 *   <li>{@link #updateFromDto(ResenaUpdateRequest, Resena)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface ResenaMapper {

    /**
     * Convierte un DTO de solicitud {@link ResenaRequest} en una nueva entidad {@link Resena}.
     *
     * <p>Los campos {@code id}, {@code cliente} y {@code createdAt} se ignoran porque se asignan
     * durante la persistencia.</p>
     *
     * @param dto el DTO de solicitud con los datos de la resena.
     * @return una nueva entidad {@link Resena} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Resena toEntity(ResenaRequest dto);

    /**
     * Convierte una entidad {@link Resena} en un DTO de respuesta {@link ResenaResponse}.
     *
     * @param resena la entidad de resena a convertir.
     * @return el DTO de respuesta con los campos mapeados directamente.
     */
    ResenaResponse toResponse(Resena resena);

    /**
     * Actualiza una entidad {@link Resena} existente con los valores no nulos de un DTO {@link ResenaUpdateRequest}.
     *
     * <p>Se ignoran los campos {@code id}, {@code name}, {@code cliente}, {@code createdAt} y {@code pictureUrl}
     * para preservar su estado actual.</p>
     *
     * @param dto   el DTO con los campos a actualizar.
     * @param resena la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "pictureUrl", ignore = true)
    void updateFromDto(ResenaUpdateRequest dto, @MappingTarget Resena resena);
}
