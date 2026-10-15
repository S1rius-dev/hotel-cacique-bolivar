package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un movimiento de caja (ingreso o egreso).
 * Contiene el tipo, concepto, monto, método de pago y información del usuario que lo registró.
 */
public record MovimientoResponse(
        Long id,
        String tipo,
        String concepto,
        BigDecimal monto,
        String metodoPago,
        Long pagoId,
        String usuario,
        String caja,
        Long turnoId,
        LocalDateTime fecha,
        String observaciones) {

}
