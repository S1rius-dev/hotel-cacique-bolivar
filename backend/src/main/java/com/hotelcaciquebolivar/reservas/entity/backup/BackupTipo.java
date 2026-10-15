package com.hotelcaciquebolivar.reservas.entity.backup;

/**
 * Enumeracion que indica el origen de una copia de seguridad.
 *
 * <p>El origen determina el subdirectorio de almacenamiento, la politica de retencion
 * aplicable y si la copia fue solicitada por un administrador o por la tarea programada.
 * La separacion importa porque las automaticas se purgan a los 30 dias y las manuales
 * se conservan: un backup que nadie pidio puede desaparecer sin que se note, uno que
 * alguien pidio no debe.</p>
 *
 * <p>Se persiste como cadena en {@code backup_history.tipo}, asi que renombrar estas
 * constantes romperia el historial existente.</p>
 */
public enum BackupTipo {
    /** Generada por la tarea programada. Su retencion por defecto es de 30 dias. */
    AUTOMATICO,
    /** Solicitada a mano por un administrador. Su retencion por defecto es indefinida. */
    MANUAL
}
