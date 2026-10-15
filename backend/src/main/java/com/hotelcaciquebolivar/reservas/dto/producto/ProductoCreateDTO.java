package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de solicitud para crear un nuevo producto en la tienda del hotel.
 * Contiene la información necesaria para registrar un producto con su inventario.
 */
public record ProductoCreateDTO(
        /** Nombre del producto. */
        @NotBlank String nombre,
        /** Código SKU o código de barras del producto. */
        String skuBarcode,
        /** Identificador de la categoría a la que pertenece el producto. */
        Long categoriaId,
        /** Precio de venta al público, debe ser mayor que cero. */
        @NotNull @Positive BigDecimal precioVenta,
        /** Costo de adquisición del producto. */
        @PositiveOrZero BigDecimal costo,
        /** Stock actual disponible en inventario. */
        @PositiveOrZero Integer stockActual,
        /** Nivel mínimo de stock para alertas de reabastecimiento. */
        @PositiveOrZero Integer stockMinimo
) {}
