package com.hotelcaciquebolivar.reservas.dto.habitacion;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la actualización de datos de una habitación existente.
 * Permite modificar número, tipo, descripción y estado de la habitación.
 */
public record HabitacionUpdateRequest(

        @NotBlank(message = "{HabitacionUpdateRequest.numero.notblank}")
        @Size(max = 20, message = "{HabitacionUpdateRequest.numero.size}")
        String numero,

        Long tipoId,

        @Size(max = 255, message = "{HabitacionUpdateRequest.descripcion.size}")
        String descripcion,

        EstadoHabitacion estado
) {}
