package com.hotelcaciquebolivar.reservas.service.backup.mysql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;

import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;

/**
 * Archivo temporal de credenciales para las utilidades de linea de comandos de MySQL
 * ({@code mysqldump} y {@code mysql}).
 *
 * <p>La clave nunca viaja en la linea de comandos: {@code -p<clave>} queda visible en la lista
 * de procesos del sistema y en el historial del servidor. En su lugar se escribe un
 * {@code --defaults-extra-file} temporal que se borra en {@link #close()}. Debe ser el primer
 * argumento porque MySQL lee ese archivo antes de aplicar cualquier otra opcion.</p>
 *
 * <p>Es {@link AutoCloseable} para forzar el borrado con try-with-resources, incluso si el
 * proceso externo falla a mitad.</p>
 */
public final class MySqlClientCredentials implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MySqlClientCredentials.class);

    private final Path archivo;

    private MySqlClientCredentials(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Escribe un archivo temporal con usuario y clave de la datasource configurada.
     *
     * @param dataSourceProperties datasource de la aplicacion
     * @return el archivo, listo para pasar por {@link #argumento()}
     * @throws BackupExecutionException si el archivo no se puede crear o escribir
     */
    public static MySqlClientCredentials crear(DataSourceProperties dataSourceProperties) {
        Path archivo;
        try {
            archivo = Files.createTempFile("hotel-mysql-", ".cnf");
            String contenido = """
                    [client]
                    user=%s
                    password=%s
                    """.formatted(dataSourceProperties.determineUsername(),
                    dataSourceProperties.determinePassword());
            Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo crear el archivo temporal de credenciales: " + e.getMessage(), e);
        }
        restringirAcceso(archivo);
        return new MySqlClientCredentials(archivo);
    }

    /**
     * Argumento {@code --defaults-extra-file} con la ruta absoluta.
     *
     * @return el argumento listo para ProcessBuilder
     */
    public String argumento() {
        return "--defaults-extra-file=" + archivo.toAbsolutePath();
    }

    @Override
    public void close() {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo temporal de credenciales {}: {}", archivo, e.getMessage());
        }
    }

    /**
     * Limita el archivo al usuario actual en sistemas POSIX.
     *
     * <p>En Windows no hay equivalente portatil y el archivo hereda los permisos del
     * directorio temporal del usuario, que el cliente MySQL acepta sin cambios.</p>
     */
    private static void restringirAcceso(Path archivo) {
        try {
            Files.setPosixFilePermissions(archivo, java.util.EnumSet.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException e) {
            log.debug("No se pudieron aplicar permisos POSIX al archivo de credenciales: {}", e.getMessage());
        }
    }
}
