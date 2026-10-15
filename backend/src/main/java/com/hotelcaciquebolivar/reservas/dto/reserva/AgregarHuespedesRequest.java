package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO de solicitud para agregar huéspedes a un detalle de reserva existente.
 * Contiene la lista de identificadores de huéspedes a asociar.
 */
public record AgregarHuespedesRequest(
        /** Lista de identificadores de huéspedes a agregar. */
        @NotEmpty List<Long> huespedesIds
) {
}
