package com.hotelcaciquebolivar.reservas.entity.backup;

/**
 * Enumeracion que representa el estado de una ejecucion de copia de seguridad.
 */
public enum BackupEstado {
    /** La ejecucion esta en curso. Un registro en este estado mas antiguo que
     *  app.backup.timeout-seconds se marca como FALLIDO en el siguiente barrido. */
    EN_PROGRESO,
    /** mysqldump termino correctamente y el archivo fue almacenado. */
    EXITOSO,
    /** mysqldump fallo, o el archivo no pudo escribirse o almacenarse. */
    FALLIDO
}
