package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear una reserva rápida desde recepción.
 * Permite reservar una habitación específica con los huéspedes preseleccionados.
 */
public record ReservaRapidaRequest(
        /** Identificador de la habitación a reservar. */
        @NotNull(message = "{ReservaRapidaRequest.habitacionId.notnull}") Long habitacionId,
        /** Fecha de check-in. */
        @NotNull(message = "{ReservaRapidaRequest.fechaCheckIn.notnull}") LocalDate fechaCheckIn,
        /** Fecha de check-out. */
        @NotNull(message = "{ReservaRapidaRequest.fechaCheckOut.notnull}") LocalDate fechaCheckOut,
        /** Cantidad de ocupantes. */
        @NotNull(message = "{ReservaRapidaRequest.cantidadOcupantes.notnull}") Integer cantidadOcupantes,
        /** Lista de identificadores de huéspedes a asignar. */
        @NotEmpty(message = "{ReservaRapidaRequest.huespedesIds.notempty}") List<Long> huespedesIds
) {
}
