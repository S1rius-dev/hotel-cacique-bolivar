package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para el dashboard de pagos.
 * Representa un pago con información de reserva, cliente, habitación, monto, canal, estado y detalles de transacción.
 */
public record PagoDashboardResponse(
        Long id,
        Long reservaId,
        String codigoReserva,
        String clienteNombre,
        String habitacion,
        BigDecimal monto,
        String canal, // wompi | efectivo
        String estado, // aprobada | pendiente | rechazada
        LocalDateTime createdAt,
        String wompiTransactionId,
        String paymentMethod,
        String wompiStatusRaw,
        String motivoRechazo,
        String recibidoPor,
        String numeroRecibo
) {}
