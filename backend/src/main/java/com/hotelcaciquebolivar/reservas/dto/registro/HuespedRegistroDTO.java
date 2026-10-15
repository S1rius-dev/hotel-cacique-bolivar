package com.hotelcaciquebolivar.reservas.dto.registro;

import java.time.LocalDate;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;

/**
 * DTO que contiene la información de un huésped para el registro y check-in.
 * Utilizado en el proceso de registro de huéspedes en recepción.
 */
public record HuespedRegistroDTO(
        /** Identificador único del huésped. */
        Long id,
        /** Número de la habitación asignada. */
        String numeroHabitacion,
        /** Tipo de la habitación asignada. */
        String tipoHabitacion,
        /** Número de cédula o identificación del huésped. */
        String cedula,
        /** Nombre(s) del huésped. */
        String nombreCompleto,
        /** Apellido(s) del huésped. */
        String apellidoCompleto,
        /** Código único de la reserva asociada. */
        String codigoReserva,
        /** Fecha programada de check-in. */
        LocalDate fechaCheckIn,
        /** Fecha programada de check-out. */
        LocalDate fechaCheckOut,
        /** Estado actual del detalle de reserva del huésped. */
        EstadoDetalleReserva estadoDetalle,
        /** Identificador de la reserva asociada. */
        Long reservaId
) {
}
