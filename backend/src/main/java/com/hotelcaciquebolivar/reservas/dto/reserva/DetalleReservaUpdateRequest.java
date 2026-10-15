package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

/**
 * DTO de solicitud para actualizar un detalle de reserva existente.
 * Todos los campos son opcionales; solo se actualizan los campos proporcionados.
 */
public record DetalleReservaUpdateRequest(
        /** Identificador del detalle de reserva a actualizar. */
        Long detalleId,
        /** Nuevo identificador de habitación a asignar. */
        Long habitacionId,
        /** Nuevo precio por noche. */
        BigDecimal precioPorNoche,
        /** Nueva cantidad de ocupantes. */
        Integer cantidadOcupantes,
        /** Nuevas observaciones o notas. */
        String observaciones
        ) {

}
