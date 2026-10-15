package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la actualización de datos de un tipo de habitación existente.
 * Permite modificar nombre, descripción, capacidades, precio base y precio por persona extra.
 */
public record TipoHabitacionUpdateRequest(

        @Size(max = 50, message = "{TipoHabitacionUpdateRequest.nombre.size}")
        String nombre,

        @Size(max = 255, message = "{TipoHabitacionUpdateRequest.descripcion.size}")
        String descripcion,

        @Min(value = 1, message = "{TipoHabitacionUpdateRequest.capacidadBase.min}")
        Integer capacidadBase,

        @Min(value = 1, message = "{TipoHabitacionUpdateRequest.capacidadMaxima.min}")
        Integer capacidadMaxima,

        BigDecimal precioBase,

        BigDecimal precioPersonaExtra,
        BigDecimal precioConIva
) {}
