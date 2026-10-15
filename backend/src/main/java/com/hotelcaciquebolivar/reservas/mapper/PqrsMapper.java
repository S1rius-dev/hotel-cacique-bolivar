package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsResponse;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.pqrs.Pqrs;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Pqrs} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(PqrsRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toResponse(Pqrs)} - Entidad a DTO de respuesta con info de usuario y reserva.</li>
 *   <li>{@link #updateFromDto(PqrsUpdateRequest, Pqrs)} - Actualizacion parcial de una entidad existente.</li>
 * </ul>
 *
 * <p>El mapeo de la reserva se realiza mediante el metodo personalizado {@link #mapReservaFromId(Long)},
 * que construye una referencia parcial de la reserva a partir de su identificador.</p>
 */
@Mapper(componentModel = "spring")
public interface PqrsMapper {

    /**
     * Convierte un DTO de solicitud {@link PqrsRequest} en una nueva entidad {@link Pqrs}.
     *
     * <p>Los campos {@code id}, {@code codigo}, {@code prioridad}, {@code estado}, {@code respuesta},
     * {@code fechaCreacion}, {@code fechaActualizacion}, {@code usuario} y {@code fechaRespuesta} se ignoran
     * porque se asignan durante la persistencia y la logica de negocio. La {@code reserva} se construye
     * a partir del {@code reservaId} proporcionado.</p>
     *
     * @param dto el DTO de solicitud con los datos del PQRS.
     * @return una nueva entidad {@link Pqrs} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "prioridad", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "respuesta", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "reserva", source = "reservaId", qualifiedByName = "mapReservaFromId")
    @Mapping(target = "fechaRespuesta", ignore = true)
    Pqrs toEntity(PqrsRequest dto);

    /**
     * Convierte una entidad {@link Pqrs} en un DTO de respuesta {@link PqrsResponse}.
     *
     * <p>Extrae informacion del usuario asociado ({@code usuarioId}, {@code usuarioEmail}, {@code usuarioNombre})
     * y de la reserva asociada ({@code reservaId}, {@code codigoReserva}). Los campos enum {@code tipo},
     * {@code prioridad} y {@code estado} se convierten a String.</p>
     *
     * @param pqrs la entidad de PQRS a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.email", target = "usuarioEmail")
    @Mapping(source = "usuario.username", target = "usuarioNombre")
    @Mapping(source = "reserva.id", target = "reservaId")
    @Mapping(source = "reserva.codigoReserva", target = "codigoReserva")
    @Mapping(target = "tipo", expression = "java(pqrs.getTipo() != null ? pqrs.getTipo().name() : null)")
    @Mapping(target = "prioridad", expression = "java(pqrs.getPrioridad() != null ? pqrs.getPrioridad().name() : null)")
    @Mapping(target = "estado", expression = "java(pqrs.getEstado() != null ? pqrs.getEstado().name() : null)")
    PqrsResponse toResponse(Pqrs pqrs);

    /**
     * Actualiza una entidad {@link Pqrs} existente con los valores no nulos de un DTO {@link PqrsUpdateRequest}.
     *
     * <p>Se ignoran todos los campos excepto {@code asunto}, {@code descripcion} y {@code tipo}
     * para preservar el estado actual de la entidad.</p>
     *
     * @param dto  el DTO con los campos a actualizar.
     * @param pqrs la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "prioridad", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "respuesta", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "fechaRespuesta", ignore = true)
    void updateFromDto(PqrsUpdateRequest dto, @MappingTarget Pqrs pqrs);

    /**
     * Metodo personalizado que construye una referencia parcial de {@link Reserva} a partir de su identificador.
     *
     * <p>Crea un objeto {@link Reserva} con solo el campo {@code id} establecido, util para mapeos
     * donde solo se necesita la referencia de la FK en la entidad.</p>
     *
     * @param reservaId el identificador de la reserva.
     * @return una referencia parcial de la reserva, o {@code null} si el id es nulo.
     */
    @Named("mapReservaFromId")
    default Reserva mapReservaFromId(Long reservaId) {
        if (reservaId == null) {
            return null;
        }
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        return reserva;
    }
}
