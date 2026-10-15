package com.hotelcaciquebolivar.reservas.dto.consumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un consumo registrado.
 * Incluye información de la reserva, producto consumido, cantidades, montos, estado y movimiento de inventario asociado.
 */
public record ConsumoResponse(
        Long id,
        Long reservaId,
        Long detalleReservaId,
        String detalleHabitacion,
        Long productoId,
        String productoNombre,
        String skuBarcode,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal montoTotal,
        String estado,
        Long movimientoInventarioId,
        String usuarioEmail,
        String observaciones,
        LocalDateTime createdAt
) {}
