package com.hotelcaciquebolivar.reservas.service.backup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;

/**
 * Tests de la construccion del comando de mysqldump.
 *
 * <p>El foco es el contrato con el proceso externo: el orden de los argumentos (MySQL exige
 * que {@code --defaults-extra-file} vaya primero) y el origen de las credenciales. No se
 * invoca mysqldump real, porque en el perfil de test la base es H2.</p>
 */
@ExtendWith(MockitoExtension.class)
class MySqlDumpServiceTest {

    @Mock
    private BackupStorage storage;

    @Mock
    private DataSourceProperties dataSourceProperties;

    private BackupProperties properties;
    private MySqlDumpService dumpService;

    @BeforeEach
    void setUp() {
        properties = new BackupProperties();
        properties.setMysqldumpPath("mysqldump");
        properties.setDumpExtraArgs("--single-transaction --routines --triggers");
        dumpService = new MySqlDumpService(properties, dataSourceProperties, storage);
    }

    @Test
    void construirComando_debeColocarDefaultsExtraFilePrimero() {
        List<String> comando = comandoPara(
                "jdbc:mysql://localhost:3306/hotelcaciquebolivar?useSSL=false");

        assertEquals("mysqldump", comando.get(0));
        assertTrue(comando.get(1).startsWith("--defaults-extra-file="),
                "--defaults-extra-file debe ser la primera opcion: MySQL lo lee antes que las demas");
    }

    @Test
    void construirComando_noDebeIncluirPasswordEnLaLineaDeComandos() {
        List<String> comando = comandoPara("jdbc:mysql://localhost:3306/hotelcaciquebolivar");

        assertTrue(comando.stream().noneMatch(arg -> arg.startsWith("-p")),
                "no se debe usar -p<clave> porque queda visible en la lista de procesos del sistema");
        assertEquals(1, comando.stream().filter(arg -> arg.startsWith("--defaults-extra-file=")).count(),
                "la clave debe viajar solo dentro del archivo de credenciales, nunca como argumento");
    }

    @Test
    void construirComando_debeExtraerHostPuertoYBaseDelUrlJdbc() {
        List<String> comando = comandoPara(
                "jdbc:mysql://db.hotel.local:3307/hotelcaciquebolivar?serverTimezone=America/Bogota");

        assertTrue(comando.contains("--host=db.hotel.local"));
        assertTrue(comando.contains("--port=3307"));
        assertTrue(comando.contains("hotelcaciquebolivar"));
    }

    @Test
    void construirComando_debeUsarPuerto3306SiElUrlNoLoEspecifica() {
        List<String> comando = comandoPara("jdbc:mysql://localhost/hotelcaciquebolivar");

        assertTrue(comando.contains("--port=3306"));
    }

    @Test
    void construirComando_debeIncluirLosArgumentosExtraConfigurados() {
        properties.setDumpExtraArgs("--single-transaction --default-character-set=utf8mb4");

        List<String> comando = comandoPara("jdbc:mysql://localhost:3306/hotelcaciquebolivar");

        assertTrue(comando.contains("--single-transaction"));
        assertTrue(comando.contains("--default-character-set=utf8mb4"));
    }

    @Test
    void construirComando_debeTerminarEnElNombreDeLaBase() {
        List<String> comando = comandoPara("jdbc:mysql://localhost:3306/hotelcaciquebolivar");

        assertEquals("hotelcaciquebolivar", comando.get(comando.size() - 1));
    }

    @Test
    void construirComando_urlNoMySql_debeLanzarExcepcionExplicita() {
        when(dataSourceProperties.determineUrl()).thenReturn("jdbc:h2:mem:testdb");

        BackupExecutionException error = assertThrows(BackupExecutionException.class,
                () -> dumpService.construirComando("--defaults-extra-file=dummy.cnf"));

        assertTrue(error.getMessage().contains("JDBC url"));
    }

    @Test
    void ejecutarYComprimir_mysqldumpInexistente_debeReportarQueRevisarLaRuta() {
        properties.setMysqldumpPath("ruta/inexistente/mysqldump.exe");
        when(dataSourceProperties.determineUrl()).thenReturn("jdbc:mysql://localhost:3306/hotelcaciquebolivar");
        when(dataSourceProperties.determineUsername()).thenReturn("root");
        when(dataSourceProperties.determinePassword()).thenReturn("clave");

        BackupExecutionException error = assertThrows(BackupExecutionException.class,
                () -> dumpService.ejecutarYComprimir("manual/test.sql.gz"));

        assertTrue(error.getMessage().contains("ruta/inexistente/mysqldump.exe"),
                "el mensaje debe senalar la ruta configurada a corregir");
        assertTrue(error.getMessage().contains("ruta configurada"),
                "el mensaje debe indicar que hay que revisar la ruta del ejecutable");
    }

    /**
     * Regresion del orden de los envoltorios: el digest debe envolver al gzip, no al revés.
     *
     * <p>Con el digest por dentro se calculaba el hash del SQL sin comprimir, asi que el
     * checksum guardado en {@code backup_history.checksum} no coincidia jamas con el
     * {@code .sql.gz} descargado y la verificacion de integridad era inejecutable. Los tests
     * con mysqldump simulado no lo detectaban porque nunca se exercise la composicion de
     * streams, por eso se prueba el metodo directamente.</p>
     */
    @Test
    void comprimeYHashea_elChecksumDebeCorresponderAlArchivoPublicado() throws Exception {
        Path destino = Files.createTempFile("prueba-checksum-", ".sql.gz");
        String sql = "CREATE TABLE huespedes (id INT);\nCREATE TABLE reservas (id INT);\n";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (OutputStream salida = MySqlDumpService.abrirComprimidoYHasheado(destino, digest)) {
            salida.write(sql.getBytes(StandardCharsets.UTF_8));
        }
        String checksum = HexFormat.of().formatHex(digest.digest());

        assertEquals(
                HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(destino))),
                checksum,
                "el checksum registrado debe verificar el .sql.gz, no el SQL sin comprimir");

        // Y el archivo debe seguir siendo un gzip legible con el SQL original.
        try (GZIPInputStream in = new GZIPInputStream(Files.newInputStream(destino))) {
            assertEquals(sql, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }

        Files.deleteIfExists(destino);
    }

    /**
     * El checksum del .sql.gz no debe coincidir con el del SQL crudo; si coincidieran, el test
     * anterior pasaria aunque el envoltorio estuviera invertido.
     */
    @Test
    void comprimeYHashea_elChecksumNoDebeSerElDelSqlSinComprimir() throws Exception {
        Path destino = Files.createTempFile("prueba-diferencia-", ".sql.gz");
        String sql = "CREATE TABLE huespedes (id INT);\n";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (OutputStream salida = MySqlDumpService.abrirComprimidoYHasheado(destino, digest)) {
            salida.write(sql.getBytes(StandardCharsets.UTF_8));
        }
        String checksumComprimido = HexFormat.of().formatHex(digest.digest());

        String checksumCrudo = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(sql.getBytes(StandardCharsets.UTF_8)));

        assertNotEquals(checksumCrudo, checksumComprimido,
                "si coincidieran, el digest estaria envolviendo al SQL en vez de al archivo publicado");

        Files.deleteIfExists(destino);
    }

    /**
     * construirComando solo resuelve el destino (host, puerto, base) a partir del JDBC url; el
     * usuario y la clave viajan por el archivo de credenciales, asi que no se stubbean aqui.
     */
    private List<String> comandoPara(String url) {
        when(dataSourceProperties.determineUrl()).thenReturn(url);
        return dumpService.construirComando("--defaults-extra-file=/tmp/credenciales.cnf");
    }
}
