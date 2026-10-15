package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.AjusteReserva;

/**
 * Mapper MapStruct para la conversión entre la entidad {@link AjusteReserva} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toResponse(AjusteReserva)} — Entidad a DTO de respuesta.</li>
 *   <li>{@link #toEntity(AjusteRequest)} — DTO de solicitud a nueva entidad (id y reserva ignorados, activo = true).</li>
 *   <li>{@link #updateFromDto(AjusteRequest, AjusteReserva)} — Actualización parcial de una entidad existente desde un DTO,
 *       ignorando valores nulos en el origen.</li>
 * </ul>
 *
 * <p>Este mapper se integra con Spring mediante {@code componentModel = "spring"}.</p>
 */
@Mapper(componentModel = "spring")
public interface AjusteReservaMapper {

    /**
     * Convierte una entidad {@link AjusteReserva} en un DTO de respuesta {@link AjusteResponse}.
     *
     * @param ajuste la entidad de ajuste de reserva a convertir.
     * @return el DTO de respuesta con los campos mapeados directamente.
     */
    AjusteResponse toResponse(AjusteReserva ajuste);

    /**
     * Convierte un DTO de solicitud {@link AjusteRequest} en una nueva entidad {@link AjusteReserva}.
     *
     * <p>Los campos {@code id} y {@code reserva} se ignoran porque se asignan externamente.
     * El campo {@code active} se establece en {@code true} por defecto.</p>
     *
     * @param dto el DTO de solicitud con los datos del ajuste.
     * @return una nueva entidad {@link AjusteReserva} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "activo", constant = "true")
    AjusteReserva toEntity(AjusteRequest dto);

    /**
     * Actualiza una entidad {@link AjusteReserva} existente con los valores no nulos de un DTO {@link AjusteRequest}.
     *
     * <p>Se ignoran los campos {@code id}, {@code reserva} y {@code activo} para preservar su estado actual.
     * Los valores nulos en el DTO no sobrescriben los valores existentes en la entidad.</p>
     *
     * @param dto    el DTO con los campos a actualizar.
     * @param ajuste la entidad objetivo que será actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateFromDto(AjusteRequest dto, @MappingTarget AjusteReserva ajuste);
}
