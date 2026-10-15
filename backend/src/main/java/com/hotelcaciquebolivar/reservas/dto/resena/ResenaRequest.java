package com.hotelcaciquebolivar.reservas.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear una nueva reseña del hotel.
 * Contiene la calificación, comentario e información opcional del autor.
 */
public record ResenaRequest(
    /** Comentario o texto de la reseña. */
    @NotBlank String comentario,
    /** Calificación numérica del 1 al 5. */
    @NotNull @Min(1) @Max(5) Integer calificacion,
    /** Nombre del autor de la reseña (opcional). */
    String name,
    /** Ubicación o ciudad del autor de la reseña (opcional). */
    String ubicacion,
    /** URL de la foto de perfil del autor (opcional). */
    String pictureUrl
) {}
