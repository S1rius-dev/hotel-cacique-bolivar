package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta detallada que contiene toda la información de un producto de la tienda.
 * Incluye datos de categoría, precios, inventario y timestamps.
 */
public record ProductoResponseDTO(
        /** Identificador único del producto. */
        Long id,
        /** Nombre del producto. */
        String nombre,
        /** Código SKU o código de barras del producto. */
        String skuBarcode,
        /** Categoría a la que pertenece el producto. */
        CategoriaDTO categoria,
        /** Precio de venta al público. */
        BigDecimal precioVenta,
        /** Costo de adquisición del producto. */
        BigDecimal costo,
        /** Stock actual disponible en inventario. */
        Integer stockActual,
        /** Nivel mínimo de stock para alertas. */
        Integer stockMinimo,
        /** Indica si el producto está activo. */
        Boolean activo,
        /** Estado del stock (ej: "normal", "bajo", "agotado"). */
        String estadoStock,
        /** Fecha y hora de creación del producto. */
        LocalDateTime createdAt,
        /** Fecha y hora de la última actualización. */
        LocalDateTime updatedAt
) {}
