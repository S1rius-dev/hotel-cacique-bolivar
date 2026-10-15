package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;

/**
 * DTO de respuesta que contiene la información de una reserva creada por un cliente.
 * Incluye datos de la reserva, habitaciones asignadas y estado del pago.
 */
public record CustomerReservationResponse(
    /** Identificador único de la reserva. */
    Long id,
    /** Código único generado para la reserva. */
    String codigoReserva,
    /** Fecha programada de check-in. */
    LocalDate fechaCheckIn,
    /** Fecha programada de check-out. */
    LocalDate fechaCheckOut,
    /** Fecha y hora de creación de la reserva. */
    LocalDateTime fechaCreacion,
    /** Fecha y hora de expiración del pago. */
    LocalDateTime fechaExpiracionPago,
    /** Cantidad de noches de la reserva. */
    Long noches,
    /** Estado actual de la reserva. */
    EstadoReserva estado,
    /** Monto total de la reserva. */
    BigDecimal total,
    /** Observaciones generales de la reserva. */
    String observaciones,
    /** Lista de habitaciones asignadas a la reserva. */
    List<HabitacionAsignada> habitaciones
) {
    /**
     * DTO interno que representa una habitación asignada dentro de una reserva del cliente.
     */
    public record HabitacionAsignada(
        /** Identificador de la habitación. */
        Long id,
        /** Número de la habitación. */
        String numeroHabitacion,
        /** Precio por noche de la habitación. */
        BigDecimal precioPorNoche,
        /** Cantidad de ocupantes en la habitación. */
        Integer cantidadOcupantes
    ) {}
}
