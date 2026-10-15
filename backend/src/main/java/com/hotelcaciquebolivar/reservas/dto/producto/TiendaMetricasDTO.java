package com.hotelcaciquebolivar.reservas.dto.producto;

import java.math.BigDecimal;

/**
 * DTO que contiene las métricas generales de la tienda del hotel.
 * Utilizado para el panel de administración y dashboard de la tienda.
 */
public record TiendaMetricasDTO(
        /** Total de productos activos en la tienda. */
        long totalProductos,
        /** Valor total del inventario actual. */
        BigDecimal valorInventario,
        /** Cantidad de productos con stock bajo el nivel mínimo. */
        long stockBajoCount,
        /** Cantidad de productos vendidos hoy. */
        long vendidosHoyCount,
        /** Monto total de ventas del día actual. */
        BigDecimal vendidosHoyMonto
) {}
