package com.hotelcaciquebolivar.reservas.exception.backup;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * El destino de un clon es invalido y por tanto se rechaza antes de tocar el servidor.
 *
 * <p>Se distingue de {@link BackupExecutionException} a proposito: un nombre de base
 * equivocado o pedir restaurar sobre la base en uso es un error de quien hace la
 * peticion, no una falla del servidor. Devolver 500 haria que el panel lo mostrara como
 * un incidente y esconderia que solo hay que corregir el nombre.</p>
 *
 * <p>Tampoco es 409: no hay conflicto de estado ni concurrencia, simplemente la entrada
 * no es aceptable tal como vino.</p>
 */
public class BackupDestinoInvalidoException extends BusinessException {

    /**
     * @param mensaje descripcion en espanol de por que se rechaza el destino
     */
    public BackupDestinoInvalidoException(String mensaje) {
        super("BACKUP_DESTINO_INVALIDO", mensaje, HttpStatus.BAD_REQUEST);
    }
}
