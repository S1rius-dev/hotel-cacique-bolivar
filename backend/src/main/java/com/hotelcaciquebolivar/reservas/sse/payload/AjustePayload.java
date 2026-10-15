package com.hotelcaciquebolivar.reservas.sse.payload;

import java.math.BigDecimal;

/**
 * Payload de datos que se transmite vía SSE cuando se agrega un ajuste a una reserva.
 * Contiene la información necesaria para que los clientes actualicen la interfaz
 * con los datos del ajuste registrado.
 *
 * @param id        identificador único del ajuste
 * @param reservaId identificador de la reserva a la que pertenece el ajuste
 * @param tipo      tipo de ajuste (por ejemplo: descargo, cargo adicional)
 * @param concepto  descripción o motivo del ajuste
 * @param valor     valor monetario del ajuste
 */
public record AjustePayload(
    Long id,
    Long reservaId,
    String tipo,
    String concepto,
    BigDecimal valor
) {}
