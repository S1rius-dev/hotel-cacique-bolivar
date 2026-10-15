package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Habitacion} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(HabitacionRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toAdminResponse(Habitacion)} - Entidad a DTO de respuesta administrativa con info del tipo.</li>
 *   <li>{@link #updateFromDto(HabitacionUpdateRequest, Habitacion)} - Actualizacion parcial de una entidad existente.</li>
 *   <li>{@link #toDisponibilidadResponse(Habitacion)} - Entidad a DTO de disponibilidad con precios y capacidades.</li>
 * </ul>
 *
 * <p>El mapeo del tipo de habitacion se realiza mediante el metodo personalizado {@link #mapTipo(Long)},
 * que construye una referencia parcial del tipo a partir de su identificador.</p>
 */
@Mapper(componentModel = "spring")
public interface HabitacionMapper {

    /**
     * Convierte un DTO de solicitud {@link HabitacionRequest} en una nueva entidad {@link Habitacion}.
     *
     * <p>Los campos {@code id}, {@code detalles} y {@code estado} se ignoran porque se asignan
     * durante la persistencia. El {@code tipo} se construye a partir del {@code tipoId} proporcionado.</p>
     *
     * @param dto el DTO de solicitud con los datos de la habitacion.
     * @return una nueva entidad {@link Habitacion} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(source = "tipoId", target = "tipo", qualifiedByName = "mapTipo")
    Habitacion toEntity(HabitacionRequest dto);

    /**
     * Convierte una entidad {@link Habitacion} en un DTO de respuesta administrativa {@link HabitacionResponse}.
     *
     * <p>Construye un objeto anidado {@link HabitacionResponse.TipoHabitacionInfo} con la informacion
     * completa del tipo de habitacion asociado.</p>
     *
     * @param habitacion la entidad de habitacion a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "tipo", expression = "java(new HabitacionResponse.TipoHabitacionInfo(habitacion.getTipo().getId(), habitacion.getTipo().getNombre(), habitacion.getTipo().getCapacidadMaxima(), habitacion.getTipo().getPrecioBase(), habitacion.getTipo().getPrecioPersonaExtra()))")
    HabitacionResponse toAdminResponse(Habitacion habitacion);

    /**
     * Actualiza una entidad {@link Habitacion} existente con los valores no nulos de un DTO {@link HabitacionUpdateRequest}.
     *
     * <p>Se ignoran los campos {@code id} y {@code detalles} para preservar su estado actual.
     * El {@code tipo} se reconstruye a partir del {@code tipoId} proporcionado en el DTO.</p>
     *
     * @param dto       el DTO con los campos a actualizar.
     * @param habitacion la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(source = "tipoId", target = "tipo", qualifiedByName = "mapTipo")
    void updateFromDto(HabitacionUpdateRequest dto, @MappingTarget Habitacion habitacion);

    /**
     * Convierte una entidad {@link Habitacion} en un DTO de disponibilidad {@link DisponibilidadResponse}.
     *
     * <p>Mapea los precios y capacidades desde el tipo de habitacion asociado.</p>
     *
     * @param habitacion la entidad de habitacion a convertir.
     * @return el DTO de disponibilidad con precios y capacidades.
     */
    @Mapping(target = "tipoNombre", source = "tipo.nombre")
    @Mapping(target = "precioBase", source = "tipo.precioBase")
    @Mapping(target = "capacidadBase", source = "tipo.capacidadBase")
    @Mapping(target = "capacidadMaxima", source = "tipo.capacidadMaxima")
    @Mapping(target = "precioPersonaExtra", source = "tipo.precioPersonaExtra")
    DisponibilidadResponse toDisponibilidadResponse(Habitacion habitacion);

    /**
     * Metodo personalizado que construye una referencia parcial de {@link TipoHabitacion} a partir de su identificador.
     *
     * <p>Crea un objeto {@link TipoHabitacion} con solo el campo {@code id} establecido, util para mapeos
     * donde solo se necesita la referencia de la FK en la entidad.</p>
     *
     * @param tipoId el identificador del tipo de habitacion.
     * @return una referencia parcial del tipo de habitacion, o {@code null} si el id es nulo.
     */
    @Named("mapTipo")
    default TipoHabitacion mapTipo(Long tipoId) {
        if (tipoId == null) {
            return null;
        }
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setId(tipoId);
        return tipo;
    }
}
