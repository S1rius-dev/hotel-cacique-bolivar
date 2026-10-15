package com.hotelcaciquebolivar.reservas.exception.backup;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepcion que indica que una ejecucion de copia de seguridad fallo por una causa
 * operativa: mysqldump no disponible, error de conexion, timeout, o fallo al escribir
 * el archivo comprimido.
 */
public class BackupExecutionException extends BusinessException {

    /**
     * Constructor de la excepcion de fallo de ejecucion.
     *
     * @param mensaje descripcion en espanol de la causa del fallo
     */
    public BackupExecutionException(String mensaje) {
        super("BACKUP_EXECUTION_ERROR", mensaje, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructor de la excepcion de fallo de ejecucion con causa underlying.
     *
     * @param mensaje descripcion en espanol de la causa del fallo
     * @param causa   excepcion original que provoco el fallo
     */
    public BackupExecutionException(String mensaje, Throwable causa) {
        super("BACKUP_EXECUTION_ERROR", mensaje, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(causa);
    }
}
