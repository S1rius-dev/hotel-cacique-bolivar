package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de solicitud para actualizar un producto existente en la tienda.
 * Todos los campos son opcionales; solo se actualizan los campos proporcionados.
 */
public record ProductoUpdateDTO(
        /** Nuevo nombre del producto. */
        String nombre,
        /** Nuevo código SKU o código de barras. */
        String skuBarcode,
        /** Nuevo identificador de categoría. */
        Long categoriaId,
        /** Nuevo precio de venta (debe ser positivo). */
        @Positive BigDecimal precioVenta,
        /** Nuevo costo de adquisición. */
        @PositiveOrZero BigDecimal costo,
        /** Nuevo nivel mínimo de stock. */
        @PositiveOrZero Integer stockMinimo,
        /** Nuevo estado de actividad del producto. */
        Boolean activo
) {}
