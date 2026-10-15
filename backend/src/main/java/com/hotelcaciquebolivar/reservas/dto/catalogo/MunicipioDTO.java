package com.hotelcaciquebolivar.reservas.dto.catalogo;

/**
 * DTO de catálogo que representa un municipio.
 * Contiene el identificador y nombre del municipio.
 */
public record MunicipioDTO(
        Long id,
        String nombre
) {
}
