package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;

/**
 * DTO de respuesta que representa el saldo pendiente de pago de una reserva.
 * Incluye el total de la reserva, el total pagado y el saldo pendiente.
 */
public record SaldoPendiente(
        Long reservaId,
        String codigoReserva,
        BigDecimal totalReserva,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente
) {
}
