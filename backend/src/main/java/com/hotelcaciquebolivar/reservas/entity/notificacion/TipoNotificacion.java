package com.hotelcaciquebolivar.reservas.entity.notificacion;

/**
 * Enumeración que define los tipos de notificaciones que puede generar el sistema.
 */
public enum TipoNotificacion {
    /** Se ha creado una nueva reserva en el sistema. */
    NUEVA_RESERVA,
    /** Una reserva ha sido cancelada. */
    RESERVA_CANCELADA,
    /** Se ha registrado un check-in de un huésped. */
    CHECK_IN,
    /** Se ha registrado un check-out de un huésped. */
    CHECK_OUT,
    /** Se ha registrado un nuevo pago. */
    PAGO_REGISTRADO,
    /** Se ha procesado un reembolso de pago. */
    PAGO_REEMBOLSADO,
    /** Se ha creado una nueva PQRS (Petición, Queja, Reclamo o Sugerencia). */
    PQRS_CREADA,
    /** Se ha dado respuesta a una PQRS. */
    PQRS_RESPONDIDA,
    /** Se ha extendido la estadía de un huésped. */
    ESTADIA_EXTENDIDA,
    /** Un cliente no se presentó a su reserva (no-show). */
    NO_SHOW,
    /** Se ha registrado una alerta sobre un cliente. */
    ALERTA_CLIENTE,
    /** Se generó correctamente una copia de seguridad de la base de datos. */
    BACKUP_EXITOSO,
    /** Falló la generación de una copia de seguridad. */
    BACKUP_FALLIDO
}
