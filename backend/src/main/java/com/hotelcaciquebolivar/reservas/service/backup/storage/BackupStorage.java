package com.hotelcaciquebolivar.reservas.service.backup.storage;

import java.io.InputStream;
import java.nio.file.Path;

import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;

/**
 * Contrato del almacenamiento fisico donde se conservan los archivos de copia de seguridad.
 *
 * <p>Abstrae el sistema de archivos para que {@code BackupService} no dependa de donde
 * viven los archivos. La implementacion vigente escribe en disco local; una futura
 * implementacion sobre Google Cloud Storage (u otro proveedor) encaja aqui sin cambios
 * en el servicio, siempre que reporte el mismo {@link ProveedorStorage}.</p>
 *
 * <p>Las rutas producidas por {@link #ruta(BackupTipo, String)} son relativas al proveedor
 * y se persisten en {@code backup_history.storage_path}. Deben ser estables: son la
 * referencia que permite recuperar un archivo months despues de creado.</p>
 */
public interface BackupStorage {

    /**
     * Devuelve el identificador del proveedor, que se persiste en cada registro.
     *
     * @return el proveedor fisico de esta implementacion
     */
    ProveedorStorage proveedor();

    /**
     * Construye la ruta relativa del archivo dentro del proveedor.
     *
     * @param tipo  origen de la copia, determina el subdirectorio
     * @param nombre nombre del archivo, ya con extension
     * @return ruta relativa, por ejemplo {@code automatic/backup-2026-09-25T02-00-00.sql.gz}
     */
    String ruta(BackupTipo tipo, String nombre);

    /**
     * Crea el archivo destino y devuelve un stream de escritura para el contenido comprimido.
     *
     * <p>El llamador es responsable de cerrar el stream y de publicar el archivo moviendolo
     * desde la ruta temporal con {@link #confirmar(Path, String)}, de modo que un dump fallido
     * nunca deje un archivo visible en su ubicacion final.</p>
     *
     * @param rutaRelativa ruta devuelta por {@link #ruta(BackupTipo, String)}
     * @return un {@link java.io.OutputStream} sin bufferizar que el llamador debe cerrar
     * @throws java.io.IOException si el archivo no puede crearse
     */
    java.io.OutputStream abrirEscritura(String rutaRelativa) throws java.io.IOException;

    /**
     * Publica un archivo temporal en su ruta definitiva, creando los directorios intermedios.
     *
     * @param temporal    archivo ya escrito y cerrado
     * @param rutaRelativa ruta definitiva
     * @throws java.io.IOException si el archivo no puede moverse
     */
    void confirmar(Path temporal, String rutaRelativa) throws java.io.IOException;

    /**
     * Abre un archivo almacenado para lectura.
     *
     * @param rutaRelativa ruta persistida en {@code backup_history.storage_path}
     * @return stream de lectura que el llamador debe cerrar
     * @throws java.io.IOException si el archivo no existe o no puede leerse
     */
    InputStream abrirLectura(String rutaRelativa) throws java.io.IOException;

    /**
     * Indica si el archivo existe en el proveedor.
     *
     * @param rutaRelativa ruta persistida en {@code backup_history.storage_path}
     * @return {@code true} si el archivo esta almacenado
     */
    boolean existe(String rutaRelativa);

    /**
     * Elimina un archivo almacenado. No falla si el archivo ya no existe.
     *
     * @param rutaRelativa ruta persistida en {@code backup_history.storage_path}
     * @throws java.io.IOException si el archivo existe pero no puede eliminarse
     */
    void eliminar(String rutaRelativa) throws java.io.IOException;

    /**
     * Devuelve donde estan físicamente los backups, para mostrarlo al operador.
     *
     * <p>La UI lo usa para responder "¿donde queda mi .sql.gz?" sin que el operador
     * tenga que interpretar una ruta relativa al directorio de trabajo. No es una ruta
     * para abrir el archivo: el acceso sigue pasando por {@link #abrirLectura(String)}.</p>
     *
     * @return ubicacion legible, por ejemplo {@code C:/data/backups} o {@code gs://bucket/backups}
     */
    String ubicacion();

    /**
     * Indica el tamano en bytes de un archivo almacenado, o {@code 0} si no existe.
     *
     * @param rutaRelativa ruta persistida en {@code backup_history.storage_path}
     * @return tamano en bytes
     * @throws java.io.IOException si el archivo existe pero no puede medirse
     */
    long tamano(String rutaRelativa) throws java.io.IOException;
}
