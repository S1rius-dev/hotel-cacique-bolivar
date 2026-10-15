package com.hotelcaciquebolivar.reservas.exception.backup;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepcion que indica que el sistema de copias de seguridad esta deshabilitado
 * por configuracion ({@code app.backup.enabled=false}) o que ninguna
 * implementacion de almacenamiento coincide con el proveedor configurado.
 */
public class BackupNoDisponibleException extends BusinessException {

    /**
     * Constructor de la excepcion de servicio no disponible.
     *
     * @param mensaje descripcion en espanol de la causa por la que no esta disponible
     */
    public BackupNoDisponibleException(String mensaje) {
        super("BACKUP_NO_DISPONIBLE", mensaje, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
