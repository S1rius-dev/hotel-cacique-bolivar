package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta que representa un tipo de habitación completo.
 * Incluye nombre, descripción, capacidades, precios, cantidad de habitaciones y lista de habitaciones asociadas.
 */
public record TipoHabitacionResponse(

        Long id,

        String nombre,

        String descripcion,

        int capacidadBase,

        int capacidadMaxima,

        BigDecimal precioBase,

        BigDecimal precioPersonaExtra,

        int cantidadHabitaciones,

        List<HabitacionResponse> habitaciones,
        BigDecimal precioConIva
) {}
