package com.hotelcaciquebolivar.reservas.sse.payload;

import java.util.List;

/**
 * Payload de datos que se transmite vía SSE cuando se realiza un check-in o check-out
 * de una reserva. Contiene la información relevante de la operación para que los
 * clientes actualicen el estado de las habitaciones y reservas en tiempo real.
 *
 * @param reservaId      identificador de la reserva afectada
 * @param codigoReserva  código único de la reserva
 * @param tipo           tipo de operación realizada ("CHECK_IN" o "CHECK_OUT")
 * @param habitaciones   lista de números de habitaciones involucradas en la operación
 * @param huespedesCount cantidad total de huéspedes registrados en la reserva
 */
public record CheckInOutPayload(
    Long reservaId,
    String codigoReserva,
    String tipo,
    List<String> habitaciones,
    int huespedesCount
) {}
