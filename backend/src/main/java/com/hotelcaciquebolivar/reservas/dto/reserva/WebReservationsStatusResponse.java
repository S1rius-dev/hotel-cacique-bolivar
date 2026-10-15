package com.hotelcaciquebolivar.reservas.dto.reserva;

/**
 * DTO de respuesta que indica el estado de habilitación del sistema de reservas web.
 */
public record WebReservationsStatusResponse(
    /** Indica si las reservas web están habilitadas (true) o deshabilitadas (false). */
    boolean enabled
) {}
