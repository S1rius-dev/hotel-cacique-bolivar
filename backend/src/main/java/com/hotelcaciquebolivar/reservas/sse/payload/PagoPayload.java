package com.hotelcaciquebolivar.reservas.sse.payload;

import java.math.BigDecimal;

/**
 * Payload de datos que se transmite vía SSE cuando se registra o reembolsa un pago.
 * Contiene la información de la transacción para que los clientes conectados actualicen
 * los saldos y estados de pago de las reservas en tiempo real.
 *
 * @param id          identificador único del pago
 * @param codigoPago  código único que identifica la transacción de pago
 * @param reservaId   identificador de la reserva asociada al pago
 * @param monto       valor monetario del pago
 * @param metodoPago  método utilizado para el pago (efectivo, tarjeta, transferencia, etc.)
 */
public record PagoPayload(
    Long id,
    String codigoPago,
    Long reservaId,
    BigDecimal monto,
    String metodoPago
) {}
