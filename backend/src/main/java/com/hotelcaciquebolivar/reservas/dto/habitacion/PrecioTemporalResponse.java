package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta que representa un precio temporal configurado.
 * Incluye el ID del tipo de habitación, nombre, rango de fechas y precios configurados.
 */
public record PrecioTemporalResponse(
        Long id,
        Long tipoHabitacionId,
        String tipoHabitacionNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal precioNoche,
        BigDecimal precioPersonaExtra,
        BigDecimal precioNocheIva
) {}