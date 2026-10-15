package com.hotelcaciquebolivar.reservas.dto.reserva;

/**
 * DTO de solicitud para habilitar o deshabilitar el sistema de reservas web.
 */
public record WebReservationsToggleRequest(
    /** Nuevo estado de habilitación (true = habilitar, false = deshabilitar). */
    boolean enabled
) {}
