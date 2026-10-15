package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la creación de un nuevo tipo de habitación.
 * Contiene nombre, descripción, capacidades base y máxima, precio base y precio por persona extra.
 */
public record TipoHabitacionRequest(

        @NotBlank(message = "{TipoHabitacionRequest.nombre.notblank}")
        @Size(max = 50, message = "{TipoHabitacionRequest.nombre.size}")
        String nombre,

        @Size(max = 255, message = "{TipoHabitacionRequest.descripcion.size}")
        String descripcion,

        @NotNull(message = "{TipoHabitacionRequest.capacidadBase.notnull}")
        @Min(value = 1, message = "{TipoHabitacionRequest.capacidadBase.min}")
        Integer capacidadBase,

        @NotNull(message = "{TipoHabitacionRequest.capacidadMaxima.notnull}")
        @Min(value = 1, message = "{TipoHabitacionRequest.capacidadMaxima.min}")
        Integer capacidadMaxima,

        @NotNull(message = "{TipoHabitacionRequest.precioBase.notnull}")
        BigDecimal precioBase,

        @NotNull(message = "{TipoHabitacionRequest.precioPersonaExtra.notnull}")
        BigDecimal precioPersonaExtra,

        @NotNull(message = "{TipoHabitacionRequest.precioConIva.notnull}")
        BigDecimal precioConIva
) {}
