package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.math.BigDecimal;

/**
 * DTO de respuesta que contiene la información pública de un tipo de habitación.
 * Utilizado para mostrar las opciones de habitación en la interfaz pública de reservas.
 */
public record TipoHabitacionPublicResponse(
    /** Identificador único del tipo de habitación. */
    Long id,
    /** Nombre del tipo de habitación. */
    String nombre,
    /** Descripción del tipo de habitación. */
    String descripcion,
    /** Capacidad base de la habitación (sin costo adicional). */
    int capacidadBase,
    /** Capacidad máxima de la habitación. */
    int capacidadMaxima,
    /** Precio base por noche. */
    BigDecimal precioBase,
    /** Precio por persona extra por noche. */
    BigDecimal precioPersonaExtra
) {}
