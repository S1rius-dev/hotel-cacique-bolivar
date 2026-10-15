package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud simplificado para operaciones básicas de producto.
 * Utilizado para crear o actualizar productos con información esencial.
 */
public record ProductoRequest(
        /** Nombre del producto. */
        @NotNull
        String nombre,
        /** Código de barras del producto. */
        String codigoBarra,
        /** Cantidad en stock. */
        @NotNull
        Integer stock,
        /** Precio del producto. */
        @NotNull
        BigDecimal precio
        ) {

}
