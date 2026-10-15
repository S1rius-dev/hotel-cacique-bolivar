package com.hotelcaciquebolivar.reservas.sse.payload;

/**
 * Payload de datos que se transmite vía SSE cuando se crea una alerta para un cliente.
 * Contiene la información básica de la alerta para notificar a los usuarios conectados.
 *
 * @param id        identificador único de la alerta
 * @param clienteId identificador del cliente asociado a la alerta
 * @param tipo      tipo de alerta (por ejemplo: mora, documentos pendientes)
 * @param mensaje   mensaje descriptivo de la alerta
 */
public record AlertaPayload(
    Long id,
    Long clienteId,
    String tipo,
    String mensaje
) {}
