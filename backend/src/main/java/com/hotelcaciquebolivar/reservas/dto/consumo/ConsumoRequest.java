package com.hotelcaciquebolivar.reservas.dto.consumo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para registrar un consumo de producto.
 * Contiene el ID del producto, cantidad, opcionalmente el ID del detalle de reserva y observaciones.
 */
public record ConsumoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        Long detalleReservaId,
        String observaciones
) {}
