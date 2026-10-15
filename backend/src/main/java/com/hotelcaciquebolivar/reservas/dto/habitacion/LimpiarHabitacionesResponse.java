package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.util.List;

/**
 * DTO de respuesta que resume el resultado de la operación de limpieza de habitaciones.
 * Contiene la cantidad de habitaciones procesadas, las omitidas y un mensaje descriptivo.
 */
public record LimpiarHabitacionesResponse(
    int procesadas,
    List<Long> omitidas,
    String mensaje
) {}
