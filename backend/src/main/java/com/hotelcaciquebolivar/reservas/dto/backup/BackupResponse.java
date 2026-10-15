package com.hotelcaciquebolivar.reservas.dto.backup;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;

/**
 * DTO de respuesta de una ejecucion de copia de seguridad.
 *
 * <p>No expone el mensaje de error interno ni la ruta del almacenamiento: el primero solo
 * interesa en logs y el segundo no debe filtrarse al cliente. El detalle del fallo se
 * devuelve en el mensaje de error de la respuesta HTTP cuando la peticion falla.</p>
 */
public record BackupResponse(
        Long id,
        LocalDateTime fecha,
        LocalDateTime fechaFin,
        BackupTipo tipo,
        BackupEstado estado,
        String archivoNombre,
        long tamanoBytes,
        Long duracionMs,
        String usuario,
        String checksumSha256,
        ProveedorStorage proveedor
) {
}
