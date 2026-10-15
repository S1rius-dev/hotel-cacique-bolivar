package com.hotelcaciquebolivar.reservas.dto.resena;

import java.util.List;

/**
 * DTO de respuesta que contiene la lista de reseñas públicas del hotel.
 * Utilizado para mostrar reseñas en la página pública del hotel.
 */
public record PublicResenasResponse(
    /** Lista de reseñas públicas. */
    List<ResenaResponse> content
) {}
