package com.hotelcaciquebolivar.reservas.dto.resena;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que contiene la información completa de una reseña del hotel.
 */
public record ResenaResponse(
    /** Identificador único de la reseña. */
    Long id,
    /** Nombre del autor de la reseña. */
    String name,
    /** Ubicación del autor de la reseña. */
    String ubicacion,
    /** URL de la foto de perfil del autor. */
    String pictureUrl,
    /** Comentario o texto de la reseña. */
    String comentario,
    /** Calificación numérica del 1 al 5. */
    Integer calificacion,
    /** Fecha y hora de creación de la reseña. */
    LocalDateTime createdAt
) {}
