package com.hotelcaciquebolivar.reservas.sse.payload;

/**
 * Payload de datos que se transmite vía SSE cuando se agrega o elimina un detalle
 * (habitación asignada) de una reserva. Permite a los clientes actualizar la
 * composición de habitaciones de la reserva en tiempo real.
 *
 * @param reservaId       identificador de la reserva afectada
 * @param detalleId       identificador del detalle de reserva
 * @param habitacionNumero número de la habitación involucrada
 * @param estado          estado actual del detalle (por ejemplo: asignada, liberada)
 */
public record DetallePayload(
    Long reservaId,
    Long detalleId,
    String habitacionNumero,
    String estado
) {}
