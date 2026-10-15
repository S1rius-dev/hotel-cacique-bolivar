package com.hotelcaciquebolivar.reservas.entity.backup;

/**
 * Enumeracion que identifica el almacenamiento fisico donde vive un archivo de copia.
 *
 * <p>Permite migrar de {@link #LOCAL} a un proveedor de nube sin perder trazabilidad
 * de los artefactos historicos.</p>
 */
public enum ProveedorStorage {
    /** Sistema de archivos local, configurado en app.backup.storage.dir. */
    LOCAL,
    /** Reservado para una futura implementacion sobre Google Cloud Storage u otro proveedor. */
    GCS
}
