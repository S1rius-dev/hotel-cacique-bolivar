package com.hotelcaciquebolivar.reservas.sse.payload;

/**
 * Payload de datos que se transmite vía SSE cuando se crea un nuevo cliente en el sistema.
 * Contiene la información del cliente para que los clientes conectados actualicen
 * sus listas y formularios en tiempo real.
 *
 * @param id             identificador único del cliente
 * @param nombreCompleto nombre completo del cliente
 * @param cedula         número de cédula o documento de identidad
 * @param email          dirección de correo electrónico del cliente
 * @param creadoPor      identificador del usuario que registró al cliente
 */
public record ClientePayload(
    Long id,
    String nombreCompleto,
    String cedula,
    String email,
    String creadoPor
) {}
