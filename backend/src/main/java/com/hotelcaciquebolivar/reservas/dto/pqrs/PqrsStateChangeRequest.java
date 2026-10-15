package com.hotelcaciquebolivar.reservas.dto.pqrs;

import com.hotelcaciquebolivar.reservas.entity.pqrs.EstadosPqrs;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para cambiar el estado de un PQRS.
 * Permite actualizar el estado y opcionalmente agregar una respuesta.
 */
public record PqrsStateChangeRequest(
    /** Nuevo estado a asignar al PQRS. */
    @NotNull EstadosPqrs estado,
    /** Respuesta o comentario asociado al cambio de estado. */
    String respuesta
) {}
