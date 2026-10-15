package com.hotelcaciquebolivar.reservas.entity.producto;

/**
 * Enumeración que define los tipos de movimiento de inventario.
 */
public enum TipoMovimiento {
    /** Entrada de mercancía al inventario (compra, devolución, etc.). */
    ENTRADA,
    /** Salida de mercancía del inventario (consumo interno, etc.). */
    SALIDA,
    /** Venta de un producto a un cliente. */
    VENTA,
    /** Ajuste de inventario por corrección o conteo físico. */
    AJUSTE
}
