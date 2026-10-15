package com.hotelcaciquebolivar.reservas.sse.payload;

import java.math.BigDecimal;

/**
 * Payload de datos que se transmite vía SSE cuando se registra un gasto operativo.
 * Contiene la información del gasto para que los clientes conectados actualicen
 * los reportes financieros en tiempo real.
 *
 * @param id        identificador único del gasto
 * @param concepto  descripción o motivo del gasto
 * @param monto     valor monetario del gasto
 * @param usuario   identificador del usuario que registró el gasto
 */
public record GastoPayload(
    Long id,
    String concepto,
    BigDecimal monto,
    String usuario
) {}
