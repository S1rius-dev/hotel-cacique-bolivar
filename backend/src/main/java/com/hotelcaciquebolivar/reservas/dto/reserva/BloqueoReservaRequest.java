package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para bloquear o desbloquear una habitación en un rango de fechas.
 * Se utiliza para维护 de habitaciones o eventos especiales que impiden reservas.
 */
public record BloqueoReservaRequest(
    /** Fecha de inicio del bloqueo. */
    LocalDate fechaInicio,
    /** Fecha de fin del bloqueo. */
    LocalDate fechaFin,
    /** Motivo del bloqueo de la habitación. */
    @NotBlank(message = "{bloqueo.motivo.obligatorio}")
    @Size(max = 255, message = "{bloqueo.motivo.tamanio}")
    String motivo,
    /** Estado del bloqueo (true = activo, false = inactivo). */
    Boolean activo
) {}