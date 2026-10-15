package com.hotelcaciquebolivar.reservas.exception.backup;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepcion que indica que ya hay una copia de seguridad en ejecucion.
 *
 * <p>Se lanza cuando un administrador pide un backup manual mientras la tarea
 * programada ya esta corriendo, o cuando dos solicitudes manuales coinciden.
 * Evita que dos procesos mysqldump compitan por el mismo ancho de banda de disco
 * y por la entrada EN_PROGRESO del historial.</p>
 */
public class BackupEnProgresoException extends BusinessException {

    /**
     * Constructor de la excepcion de conflicto por ejecucion en curso.
     */
    public BackupEnProgresoException() {
        super("BACKUP_AL_EN_PROGRESO",
                "Ya hay una copia de seguridad en ejecucion. Espera a que termine antes de solicitar otra.",
                HttpStatus.CONFLICT);
    }
}
