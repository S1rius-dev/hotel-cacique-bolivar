package com.hotelcaciquebolivar.reservas.dto.habitacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la creación de una nueva habitación.
 * Contiene el número de habitación, el ID del tipo de habitación y una descripción opcional.
 */
public record HabitacionRequest(
        @NotBlank(message = "{HabitacionRequest.numero.notblank}")
        @Size(max = 20, message = "{HabitacionRequest.numero.size}")
        String numero,
        @NotNull(message = "{HabitacionRequest.tipoId.notnull}")
        Long tipoId,
        @Size(max = 255, message = "{HabitacionRequest.descripcion.size}")
        String descripcion
        ) {

}
