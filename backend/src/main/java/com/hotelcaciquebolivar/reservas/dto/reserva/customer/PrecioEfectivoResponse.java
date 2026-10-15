package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta que contiene el precio efectivo de un tipo de habitación para un rango de fechas.
 * Incluye el desglose por noche con precios base y personas extra.
 */
public record PrecioEfectivoResponse(
        /** Identificador del tipo de habitación. */
        Long tipoHabitacionId,
        /** Nombre del tipo de habitación. */
        String nombreTipo,
        /** Fecha de check-in. */
        LocalDate checkIn,
        /** Fecha de check-out. */
        LocalDate checkOut,
        /** Cantidad de noches. */
        long noches,
        /** Precio base por noche. */
        BigDecimal precioBase,
        /** Precio base por persona extra por noche. */
        BigDecimal precioPersonaExtraBase,
        /** Desglose detallado de precios por noche. */
        List<DetalleNoche> desgloseNoches,
        /** Total acumulado de noches. */
        BigDecimal totalNoches,
        /** Total por persona extra por noche. */
        BigDecimal totalPersonaExtraPorPersona
) {
    /**
     * DTO interno que representa el detalle de precio por noche individual.
     */
    public record DetalleNoche(
        /** Fecha de la noche. */
        LocalDate fecha,
        /** Precio de la habitación para esa noche. */
        BigDecimal precioNoche,
        /** Precio adicional por persona extra para esa noche. */
        BigDecimal precioExtra
    ) {}
}
