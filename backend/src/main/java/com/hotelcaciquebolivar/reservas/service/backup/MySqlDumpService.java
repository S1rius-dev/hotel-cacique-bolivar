package com.hotelcaciquebolivar.reservas.service.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Service;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlClientCredentials;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlTarget;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MysqlProcessRunner;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;

import lombok.RequiredArgsConstructor;

/**
 * Ejecuta mysqldump como proceso externo y entrega el resultado comprimido al almacenamiento.
 *
 * <h2>Flujo de un solo pass</h2>
 * <p>La salida de mysqldump se canaliza directamente hacia un {@link GZIPOutputStream} sobre el
 * archivo temporal, sin generar un .sql intermedio, lo que evita duplicar el espacio requerido
 * en una base que puede pesar cientos de MB. El {@link DigestOutputStream} envuelve al
 * {@code GZIPOutputStream} y no al revés: el checksum registrado debe corresponder al .sql.gz
 * publicado y descargable, no al SQL sin comprimir.</p>
 *
 * <h2>Procesos y timeout</h2>
 * <p>El manejo de procesos vive en {@link MysqlProcessRunner}: separacion de stdout y stderr,
 * copia en hilo aparte para que el timeout aplique de verdad, y borrado del archivo temporal de
 * credenciales pase lo que pase.</p>
 *
 * <h2>Credenciales</h2>
 * <p>La clave nunca viaja en la linea de comandos ({@code -p<clave>} es visible en la lista
 * de procesos del sistema). Se escribe un {@code --defaults-extra-file} temporal con permisos
 * restringidos, se le pasa a mysqldump como primer argumento y se borra al terminar.</p>
 */
@Service
@RequiredArgsConstructor
public class MySqlDumpService {

    private static final Logger log = LoggerFactory.getLogger(MySqlDumpService.class);

    private final BackupProperties properties;
    private final DataSourceProperties dataSourceProperties;
    private final BackupStorage storage;

    /**
     * Ejecuta mysqldump, comprime la salida y publica el archivo en el almacenamiento.
     *
     * <p>Es bloqueante. Ante cualquier fallo se elimina el archivo temporal, de modo que nunca
     * quede un archivo incompleto en la ruta definitiva que el listado presentaria como
     * restaurable.</p>
     *
     * @param rutaRelativa ruta destino devuelta por {@link BackupStorage#ruta}
     * @return el tamano, checksum y duracion del dump almacenado
     * @throws BackupExecutionException si mysqldump falla, expira, o el almacenamiento rechaza
     *                                   la escritura
     */
    public ResultadoDump ejecutarYComprimir(String rutaRelativa) {
        long inicio = System.nanoTime();
        Path temporal = null;
        boolean publicado = false;
        // try-with-resources: el archivo de credenciales se borra siempre, incluso si mysqldump
        // revienta o se corta el proceso a mitad.
        try (MySqlClientCredentials credenciales = MySqlClientCredentials.crear(dataSourceProperties)) {
            temporal = Files.createTempFile("hotel-backup-", ".sql.gz.tmp");
            ResultadoDump resultado = ejecutar(construirComando(credenciales.argumento()), temporal);
            storage.confirmar(temporal, rutaRelativa);
            publicado = true;

            long duracionMs = (System.nanoTime() - inicio) / 1_000_000L;
            log.info("Backup generado en {} ({} bytes comprimidos)", rutaRelativa, resultado.tamanoBytes());
            return new ResultadoDump(resultado.tamanoBytes(), resultado.checksumSha256(), duracionMs);
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo escribir el archivo de copia de seguridad: " + e.getMessage(), e);
        } finally {
            if (temporal != null && !publicado) {
                borrarTemporal(temporal);
            }
        }
    }

    /**
     * Construye el comando de mysqldump a partir de la configuracion de la aplicacion.
     *
     * <p>Sin {@code --result-file}, mysqldump escribe el SQL en stdout, que es lo que este
     * servicio canaliza hacia el archivo comprimido.</p>
     *
     * @param argumentoCredenciales valor de {@code --defaults-extra-file}
     * @return los argumentos listos para {@link ProcessBuilder}
     * @throws BackupExecutionException si el JDBC url de la aplicacion no se puede interpretar
     */
    List<String> construirComando(String argumentoCredenciales) {
        MySqlTarget objetivo = leerObjetivo();

        List<String> comando = new ArrayList<>();
        comando.add(properties.getMysqldumpPath());
        // Debe ir primero: MySQL lee este archivo antes de aplicar cualquier otra opcion.
        comando.add(argumentoCredenciales);
        comando.add("--host=" + objetivo.host());
        comando.add("--port=" + objetivo.port());
        comando.addAll(dividirArgumentos(properties.getDumpExtraArgs()));
        comando.add(objetivo.baseDeDatos());
        return comando;
    }

    /**
     * Lanza mysqldump, canaliza stdout al archivo comprimido y espera con timeout.
     */
    private ResultadoDump ejecutar(List<String> comando, Path temporal) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new BackupExecutionException("SHA-256 no disponible en esta JVM", e);
        }

        long bytes;
        try (OutputStream salida = abrirComprimidoYHasheado(temporal, digest)) {
            MysqlProcessRunner.Resultado proceso = MysqlProcessRunner.conSalida(
                    comando, salida, properties.getTimeoutSeconds(), "el volcado de mysqldump");

            if (!proceso.diagnostico().isBlank()) {
                log.warn("mysqldump emitio diagnostico: {}", proceso.diagnostico());
            }
            if (proceso.codigoSalida() != 0) {
                throw new BackupExecutionException(
                        "mysqldump termino con codigo " + proceso.codigoSalida() + ": " + proceso.diagnostico());
            }
            bytes = Files.size(temporal);
        }

        return new ResultadoDump(bytes, HexFormat.of().formatHex(digest.digest()), 0L);
    }

    /**
     * Abre el archivo destino comprimido con gzip y digerido con SHA-256.
     *
     * <p>El orden de los envoltorios es lo importante: {@link DigestOutputStream} envuelve al
     * archivo y {@link GZIPOutputStream} envuelve al digest, de modo que pasan por el hash los
     * bytes exactos que quedan en disco (cabecera, datos deflateados y trailer). Invertirlo
     * calcularia el hash del SQL sin comprimir y el checksum guardado no serviria para
     * verificar el archivo descargado.</p>
     *
     * @param destino archivo a escribir
     * @param digest  se consume mientras se escribe; el valor final se lee con
     *                {@link MessageDigest#digest()}
     * @return el stream comprimido y digerido; cerrarlo cierra tambien el archivo
     */
    static OutputStream abrirComprimidoYHasheado(Path destino, MessageDigest digest) throws IOException {
        OutputStream archivo = Files.newOutputStream(destino);
        return new GZIPOutputStream(new DigestOutputStream(archivo, digest), 8192);
    }

    /**
     * Divide la cadena de argumentos extra respetando comillas dobles, para permitir
     * valores con espacios como {@code --default-character-set="valor con espacio"}.
     */
    private List<String> dividirArgumentos(String argumentos) {
        List<String> resultado = new ArrayList<>();
        if (argumentos == null || argumentos.isBlank()) {
            return resultado;
        }
        for (String parte : argumentos.trim().split("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            if (!parte.isBlank()) {
                resultado.add(parte.replace("\"", ""));
            }
        }
        return resultado;
    }

    private MySqlTarget leerObjetivo() {
        return MySqlTarget.parse(dataSourceProperties.determineUrl());
    }

    private void borrarTemporal(Path temporal) {
        try {
            Files.deleteIfExists(temporal);
        } catch (IOException e) {
            log.warn("No se pudo borrar el temporal {}: {}", temporal, e.getMessage());
        }
    }
}
