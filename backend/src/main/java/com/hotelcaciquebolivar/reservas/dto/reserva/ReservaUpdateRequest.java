package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;

import jakarta.validation.Valid;

/**
 * DTO de solicitud para actualizar una reserva existente.
 * Todos los campos son opcionales; solo se actualizan los campos proporcionados.
 */
public record ReservaUpdateRequest(
        /** Nueva fecha de check-in. */
        LocalDate fechaCheckIn,
        /** Nueva fecha de check-out. */
        LocalDate fechaCheckOut,
        /** Nuevo identificador del titular de la reserva. */
        Long titularReservaId,
        /** Nuevo origen de la reserva. */
        OrigenReserva origen,
        /** Nuevo estado de la reserva. */
        EstadoReserva estado,
        /** Nuevas observaciones generales. */
        String observaciones,
        /** Lista actualizada de detalles de habitaciones. */
        @Valid
        List<DetalleReservaUpdateRequest> detalles
        ) {

}
