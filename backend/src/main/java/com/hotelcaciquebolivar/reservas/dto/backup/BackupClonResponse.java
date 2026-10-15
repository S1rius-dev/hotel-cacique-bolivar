package com.hotelcaciquebolivar.reservas.dto.backup;

/**
 * Confirmacion de que una copia fue restaurada en una base de datos nueva.
 *
 * <p>Devolver el nombre de la base creada evita que el operador tenga que buscarlo:
 * es el dato que necesita para conectar el cliente o su inspector y comparar.</p>
 *
 * @param backupId   identificador de la copia usada como origen
 * @param baseDestino nombre de la base creada
 * @param mensaje    confirmacion legible para mostrar en la UI
 */
public record BackupClonResponse(Long backupId, String baseDestino, String mensaje) {
}
