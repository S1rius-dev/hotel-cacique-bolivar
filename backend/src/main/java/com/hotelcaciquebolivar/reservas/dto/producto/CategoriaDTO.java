package com.hotelcaciquebolivar.reservas.dto.producto;

/**
 * DTO que representa una categoría de producto en la tienda del hotel.
 * Contiene el identificador y nombre de la categoría.
 */
public record CategoriaDTO(
    /** Identificador único de la categoría. */
    Long id,
    /** Nombre de la categoría. */
    String nombre
) {}
