package com.hotelcaciquebolivar.reservas.dto.catalogo;

/**
 * DTO de catálogo que representa un tipo de identificación.
 * Contiene el identificador y nombre del tipo de documento de identidad.
 */
public record TipoIdentificacionDTO(
        Long id,
        String nombre
        ) {

}
