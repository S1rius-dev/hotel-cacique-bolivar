package com.hotelcaciquebolivar.reservas.dto.backup;

/**
 * Resultado de verificar la integridad de una copia de seguridad.
 *
 * <p>Se devuelve tanto si coincide como si no: un checksum discrepante es informacion
 * que el operador necesita ver con detalle (esperado frente a calculado), no un error
 * HTTP. La UI decide el tono del aviso a partir de {@code integro}.</p>
 *
 * @param backupId        identificador de la copia verificada
 * @param integro         {@code true} si el SHA-256 recalculado coincide con el registrado
 * @param checksumEsperado checksum registrado al crear la copia
 * @param checksumCalculado checksum recalculado ahora sobre el archivo almacenado
 * @param rutaArchivo     ruta relativa dentro del almacenamiento
 */
public record BackupVerificacionResponse(
        Long backupId,
        boolean integro,
        String checksumEsperado,
        String checksumCalculado,
        String rutaArchivo) {
}
