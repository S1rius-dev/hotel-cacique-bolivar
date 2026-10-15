package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear una nueva reserva en el sistema.
 * Contiene la información del titular, fechas y detalles de las habitaciones a reservar.
 */
public record ReservaRequest(
        /** Identificador del titular de la reserva. */
        @NotNull(message = "{ReservaRequest.titularReservaId.notnull}")
        Long titularReservaId,
        /** Fecha de check-in. */
        @NotNull(message = "{ReservaRequest.fechaCheckIn.notnull}")
        LocalDate fechaCheckIn,
        /** Fecha de check-out. */
        @NotNull(message = "{ReservaRequest.fechaCheckOut.notnull}")
        LocalDate fechaCheckOut,
        /** Origen de la reserva (walk-in, teléfono, web, etc.). */
        OrigenReserva origen,
        /** Observaciones generales de la reserva. */
        String observaciones,
        /** Lista de detalles de habitaciones a reservar (mínimo una). */
        @NotEmpty(message = "{ReservaRequest.detalles.notempty}")
        @Valid
        List<DetalleReservaRequest> detalles
        ) {

}
