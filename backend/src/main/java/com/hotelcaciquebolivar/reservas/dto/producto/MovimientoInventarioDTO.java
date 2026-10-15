package com.hotelcaciquebolivar.reservas.dto.producto;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.producto.TipoMovimiento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO que representa un movimiento de inventario de productos.
 * Registra entradas, salidas y ajustes de stock en la tienda del hotel.
 */
public record MovimientoInventarioDTO(
        /** Identificador único del movimiento de inventario. */
        Long id,
        /** Identificador del producto afectado. */
        Long productoId,
        /** Nombre del producto afectado. */
        String productoNombre,
        /** Tipo de movimiento (entrada, salida, ajuste). */
        TipoMovimiento tipo,
        /** Cantidad movida (positiva para entradas, negativa para salidas). */
        Integer cantidad,
        /** Stock resultante después del movimiento. */
        Integer stockResultante,
        /** Motivo o descripción del movimiento. */
        String motivo,
        /** Usuario que realizó el movimiento. */
        String usuario,
        /** Identificador de referencia asociado (ej: ID de reserva, ID de venta). */
        Long referenciaId,
        /** Fecha y hora de creación del movimiento. */
        LocalDateTime createdAt
) {
    /**
     * DTO de solicitud para registrar un nuevo movimiento de inventario.
     */
    public record MovimientoRequest(
        /** Tipo de movimiento a realizar. */
        @NotNull TipoMovimiento tipo,
        /** Cantidad a mover (debe ser positiva). */
        @NotNull @Positive Integer cantidad,
        /** Motivo del movimiento. */
        String motivo,
        /** Identificador de referencia asociado. */
        Long referenciaId,
        /** Método de pago utilizado (para ventas). */
        MetodoPago metodoPago
    ) {
        /**
         * Constructor simplificado sin método de pago.
         */
        public MovimientoRequest(@NotNull TipoMovimiento tipo, @NotNull @Positive Integer cantidad, String motivo, Long referenciaId) {
            this(tipo, cantidad, motivo, referenciaId, null);
        }
    }
}
