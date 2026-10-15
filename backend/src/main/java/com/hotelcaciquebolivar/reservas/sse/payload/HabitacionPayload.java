package com.hotelcaciquebolivar.reservas.sse.payload;

/**
 * Payload de datos que se transmite vía SSE cuando se cambia el estado de una habitación.
 * Contiene la información del cambio para que los clientes actualicen el mapa o tabla
 * de estados de habitaciones en tiempo real.
 *
 * @param id             identificador único de la habitación
 * @param numero         número identificador de la habitación
 * @param estadoAnterior estado de la habitación antes del cambio
 * @param estadoNuevo    estado de la habitación después del cambio
 * @param tipoHabitacion tipo o categoría de la habitación (individual, doble, suite, etc.)
 */
public record HabitacionPayload(
    Long id,
    String numero,
    String estadoAnterior,
    String estadoNuevo,
    String tipoHabitacion
) {}
