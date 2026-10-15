package com.hotelcaciquebolivar.reservas.service.backup;

/**
 * Resultado de una ejecucion de mysqldump ya comprimida y almacenada.
 *
 * <p>Los bytes corresponden al tamano del archivo .sql.gz ya escrito, no al del SQL sin comprimir.</p>
 *
 * @param tamanoBytes    tamano del archivo comprimido almacenado
 * @param checksumSha256 SHA-256 en hexadecimal del archivo comprimido
 * @param duracionMs     duracion total de la ejecucion en milisegundos
 */
public record ResultadoDump(long tamanoBytes, String checksumSha256, long duracionMs) {
}
