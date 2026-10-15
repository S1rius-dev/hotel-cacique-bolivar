package com.hotelcaciquebolivar.reservas.entity.pqrs;

/**
 * Enumeración que define los estados posibles de una PQRS (Petición, Queja, Reclamo o Sugerencia).
 */
public enum EstadosPqrs {
    /** La PQRS está abierta y pendiente de revisión. */
    ABIERTA,
    /** La PQRS está siendo revisada por el personal del hotel. */
    EN_REVISION,
    /** La PQRS ha sido respondida. */
    RESPONDIDA,
    /** La PQRS ha sido cerrada. */
    CERRADA,
    /** La PQRS ha sido rechazada. */
    RECHAZADA
}
