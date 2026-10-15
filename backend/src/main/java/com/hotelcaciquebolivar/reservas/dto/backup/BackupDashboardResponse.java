package com.hotelcaciquebolivar.reservas.dto.backup;

import java.time.LocalDateTime;

/**
 * DTO de respuesta del resumen del sistema de copias de seguridad.
 *
 * <p>Consolida lo que el panel de administracion necesita para decidir si el sistema
 * esta sano: cuando fue el ultimo backup exitoso, cuanto ocupa el almacenamiento y
 * cuando se espera el siguiente.</p>
 *
 * @param rutaAlmacenamiento ubicacion fisica de los .sql.gz, para responder "¿donde quedan mis copias?"
 * @param baseDeDatosEnUso   base que usa la aplicacion, que el clon debe rechazar como destino
 */
public record BackupDashboardResponse(
        LocalDateTime ultimoExitoso,
        String ultimoExitosoArchivo,
        boolean ejecucionEnCurso,
        long totalCopias,
        long totalFallidas,
        long tamanoTotalBytes,
        long totalAutomaticos,
        long totalManuales,
        String storageProvider,
        String proximaEjecucion,
        String rutaAlmacenamiento,
        String baseDeDatosEnUso
) {
}
