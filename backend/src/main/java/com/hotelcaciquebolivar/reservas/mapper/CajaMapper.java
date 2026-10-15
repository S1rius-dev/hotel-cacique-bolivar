package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hotelcaciquebolivar.reservas.dto.caja.MovimientoResponse;
import com.hotelcaciquebolivar.reservas.entity.caja.MovimientoCaja;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link MovimientoCaja} y su DTO de respuesta {@link MovimientoResponse}.
 *
 * <p>Realiza mapeos que transforman enum a sus representaciones de cadena de texto, y extrae
 * informacion de entidades relacionadas como {@code pago} y {@code usuario}.</p>
 */
@Mapper(componentModel = "spring")
public interface CajaMapper {

    /**
     * Convierte una entidad {@link MovimientoCaja} en un DTO de respuesta {@link MovimientoResponse}.
     *
     * <p>Los campos {@code tipo} y {@code metodoPago} se convierten de enum a String mediante expresiones Java.
     * Los campos {@code pagoId} y {@code usuario} se extraen de las entidades relacionadas.</p>
     *
     * @param movimiento la entidad de movimiento de caja a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "tipo", expression = "java(movimiento.getTipo().name())")
    @Mapping(target = "metodoPago", expression = "java(movimiento.getMetodoPago().name())")
    @Mapping(target = "pagoId", source = "pago.id")
    @Mapping(target = "usuario", source = "usuario.username")
    @Mapping(target = "caja", expression = "java(movimiento.getCaja() != null ? movimiento.getCaja().getNombre() : null)")
    @Mapping(target = "turnoId", expression = "java(movimiento.getTurno() != null ? movimiento.getTurno().getId() : null)")
    MovimientoResponse toMovimientoResponse(MovimientoCaja movimiento);
}
