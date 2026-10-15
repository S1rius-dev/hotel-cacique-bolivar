package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

/**
 * DTO de respuesta que contiene la información de una habitación disponible.
 * Utilizado para mostrar las habitaciones disponibles en un rango de fechas.
 */
public record DisponibilidadResponse(
        /** Identificador único de la habitación. */
        Long id,
        /** Número de la habitación. */
        String numero,
        /** Nombre del tipo de habitación. */
        String tipoNombre,
        /** Precio base por noche. */
        BigDecimal precioBase,
        /** Capacidad base de la habitación (sin costo adicional). */
        int capacidadBase,
        /** Capacidad máxima de la habitación. */
        int capacidadMaxima,
        /** Precio por persona extra por noche. */
        BigDecimal precioPersonaExtra
) {
}
