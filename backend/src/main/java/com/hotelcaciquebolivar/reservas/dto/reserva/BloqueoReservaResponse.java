package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que contiene la información de un bloqueo de habitación.
 */
public record BloqueoReservaResponse(
    /** Identificador único del bloqueo. */
    Long id,
    /** Fecha de inicio del bloqueo. */
    LocalDate fechaInicio,
    /** Fecha de fin del bloqueo. */
    LocalDate fechaFin,
    /** Motivo del bloqueo. */
    String motivo,
    /** Estado del bloqueo (true = activo, false = inactivo). */
    Boolean activo,
    /** Fecha y hora de creación del bloqueo. */
    LocalDateTime createdAt
) {}
