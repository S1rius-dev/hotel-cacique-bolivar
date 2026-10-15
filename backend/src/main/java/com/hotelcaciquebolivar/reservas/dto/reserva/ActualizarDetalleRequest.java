package com.hotelcaciquebolivar.reservas.dto.reserva;

/**
 * DTO de solicitud para actualizar la información de un detalle de reserva existente.
 * Permite modificar la cantidad de ocupantes y las observaciones.
 */
public record ActualizarDetalleRequest(
        /** Nueva cantidad de ocupantes para la habitación. */
        Integer cantidadOcupantes,
        /** Nuevas observaciones o notas sobre el detalle. */
        String observaciones
) {
}
