package com.hotelcaciquebolivar.reservas.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO de solicitud para actualizar una reseña existente del hotel.
 * Todos los campos son opcionales; solo se actualizan los campos proporcionados.
 */
public record ResenaUpdateRequest(
    /** Nuevo comentario o texto de la reseña. */
    String comentario,
    /** Nueva calificación numérica del 1 al 5. */
    @Min(1) @Max(5) Integer calificacion,
    /** Nueva ubicación del autor. */
    String ubicacion,
    /** Nueva URL de la foto de perfil del autor. */
    String pictureUrl
) {}
