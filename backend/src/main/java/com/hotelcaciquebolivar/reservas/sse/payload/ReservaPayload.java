package com.hotelcaciquebolivar.reservas.sse.payload;

import java.math.BigDecimal;

/**
 * Payload de datos que se transmite vía SSE cuando se crea, actualiza, cancela,
 * extiende o vence una reserva. Contiene la información completa de la reserva
 * para que los clientes conectados mantengan sus listas y vistas actualizadas
 * en tiempo real.
 *
 * @param id             identificador único de la reserva
 * @param codigoReserva  código único que identifica la reserva
 * @param clienteNombre  nombre del cliente titular de la reserva
 * @param estado         estado actual de la reserva (confirmada, cancelada, etc.)
 * @param checkIn        fecha y hora programada o realizada de check-in
 * @param checkOut       fecha y hora programada o realizada de check-out
 * @param total          valor total de la reserva
 */
public record ReservaPayload(
    Long id,
    String codigoReserva,
    String clienteNombre,
    String estado,
    String checkIn,
    String checkOut,
    BigDecimal total
) {}
