package com.hotelcaciquebolivar.reservas.sse.payload;

/**
 * Payload de datos que se transmite vía SSE cuando se crea, cambia de estado o recibe
 * respuesta una PQRS (Petición, Queja, Reclamo o Sugerencia). Contiene la información
 * esencial de la PQRS para que los clientes conectados actualicen sus paneles de
 * seguimiento en tiempo real.
 *
 * @param id     identificador único de la PQRS
 * @param codigo código único de la PQRS
 * @param tipo   tipo de PQRS (petición, queja, reclamo, sugerencia)
 * @param estado estado actual de la PQRS (abierta, en proceso, cerrada, etc.)
 */
public record PqrsPayload(
    Long id,
    String codigo,
    String tipo,
    String estado
) {}
