package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoResponse;
import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Consumo} y su DTO de respuesta {@link ConsumoResponse}.
 *
 * <p>Realiza mapeos que extraen informacion de multiples entidades relacionadas como
 * {@code reserva}, {@code detalleReserva}, {@code producto}, {@code movimientoInventario} y {@code usuario}.
 * El campo {@code estado} se convierte de enum a String mediante una expresion Java.</p>
 */
@Mapper(componentModel = "spring")
public interface ConsumoMapper {

    /**
     * Convierte una entidad {@link Consumo} en un DTO de respuesta {@link ConsumoResponse}.
     *
     * <p>Mapea los identificadores y nombres de entidades relacionadas, convirtiendo el campo
     * {@code estado} de enum a su representacion de cadena de texto.</p>
     *
     * @param consumo la entidad de consumo a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "reservaId", source = "reserva.id")
    @Mapping(target = "detalleReservaId", source = "detalleReserva.id")
    @Mapping(target = "detalleHabitacion", source = "detalleReserva.habitacion.numero")
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "skuBarcode", source = "producto.skuBarcode")
    @Mapping(target = "movimientoInventarioId", source = "movimientoInventario.id")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "estado", expression = "java(consumo.getEstado().name())")
    ConsumoResponse toResponse(Consumo consumo);
}
