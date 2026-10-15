package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;

/**
 * DTO de respuesta que contiene la información básica de un producto de la tienda.
 */
public record ProductoResponse(
        /** Identificador único del producto. */
        Long id,
        /** Nombre del producto. */
        String nombre,
        /** Código de barras del producto. */
        String codigoBarra,
        /** Cantidad disponible en stock. */
        Integer stock,
        /** Precio del producto. */
        BigDecimal precio,
        /** Indica si el producto está activo en la tienda. */
        boolean activo
        ) {

}
