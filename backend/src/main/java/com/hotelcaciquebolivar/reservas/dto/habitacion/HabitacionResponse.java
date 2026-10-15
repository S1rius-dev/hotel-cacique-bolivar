package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;

/**
 * DTO de respuesta que representa una habitación con su información completa.
 * Incluye número, estado, descripción y datos del tipo de habitación asociado.
 * Contiene un record interno {@link TipoHabitacionInfo} con la información del tipo.
 */
public record HabitacionResponse(
        Long id,
        String numero,
        String estado,
        String descripcion,
        TipoHabitacionInfo tipo
) {

    public record TipoHabitacionInfo(
            Long id,
            String nombre,
            Integer capacidadMaxima,
            BigDecimal precioBase,
            BigDecimal precioPersonaExtra
    ) {}
}
