package com.hotelcaciquebolivar.reservas.entity.audit;

/**
 * Enumeración que define los tipos de acciones de auditoría que pueden registrarse
 * en el sistema. Cada valor representa una operación específica realizada sobre
 * entidades del dominio del hotel.
 */
public enum AuditAction {
    /** Creación de una nueva entidad. */
    CREATE,
    /** Actualización de una entidad existente. */
    UPDATE,
    /** Eliminación lógica o física de una entidad. */
    DELETE,
    /** Cancelación de una reserva, pago u otra operación. */
    CANCELLED,
    /** Registro de check-in de un huésped en una habitación. */
    CHECK_IN,
    /** Registro de check-out de un huésped de una habitación. */
    CHECK_OUT,
    /** Check-out automático generado por el sistema según la fecha de salida. */
    CHECK_OUT_AUTO,
    /** Marcado automático de no-show cuando un cliente no se presentó. */
    NO_SHOW_AUTO,
    /** Registro de un nuevo cliente en el sistema. */
    CLIENT_REGISTERED,
    /** Registro de un nuevo pago en el sistema. */
    PAYMENT_REGISTERED,
    /** Registro de limpieza completada de una habitación. */
    CLEANED,
    /** Se genero correctamente una copia de seguridad de la base de datos. */
    BACKUP_CREATED,
    /** Fallo la generacion de una copia de seguridad. */
    BACKUP_FAILED,
    /** Se elimino una copia de seguridad y su archivo asociado. */
    BACKUP_DELETED,
    /** Se verifico la integridad de una copia y su checksum coincidio con el registrado. */
    BACKUP_VERIFIED,
    /** Se clonó una copia de seguridad en otra base de datos. */
    BACKUP_CLONED
}
