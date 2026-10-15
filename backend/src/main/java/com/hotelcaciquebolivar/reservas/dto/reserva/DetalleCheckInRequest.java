package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO de solicitud para registrar el check-in de un detalle de reserva.
 * Contiene la lista de huéspedes que se están registrando en la habitación.
 */
public record DetalleCheckInRequest(
        /** Lista de identificadores de huéspedes a registrar en el check-in. */
        @NotEmpty(message = "{DetalleCheckInRequest.huespedesIds.notempty}") List<Long> huespedesIds
) {
}
