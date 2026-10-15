package com.hotelcaciquebolivar.reservas.service.backup.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;

/**
 * Tests de {@link LocalFilesystemStorage} contra un directorio temporal real.
 *
 * <p>Esta clase no lleva mocks a proposito: el comportamiento que importa es el del sistema
 * de archivos, incluida la creacion de directorios intermedios y la contencion de rutas
 * fuera del directorio raiz.</p>
 */
class LocalFilesystemStorageTest {

    @TempDir
    Path raiz;

    private LocalFilesystemStorage storage;

    @BeforeEach
    void setUp() {
        BackupProperties properties = new BackupProperties();
        properties.getStorage().setDir(raiz.toString());
        storage = new LocalFilesystemStorage(properties);
    }

    @Test
    void proveedor_debeReportarLocal() {
        assertEquals(ProveedorStorage.LOCAL, storage.proveedor());
    }

    @Test
    void ruta_debeSepararAutomaticosYManualesEnSubdirectoriosDistintos() {
        String automatico = storage.ruta(BackupTipo.AUTOMATICO, "backup-1.sql.gz");
        String manual = storage.ruta(BackupTipo.MANUAL, "backup-manual-1.sql.gz");

        assertTrue(automatico.startsWith("automatic/"), automatico);
        assertTrue(manual.startsWith("manual/"), manual);
    }

    @Test
    void confirmarYAbrirLectura_debePreservarElContenido() throws IOException {
        String ruta = storage.ruta(BackupTipo.MANUAL, "backup.sql.gz");
        Path temporal = Files.createTempFile("test-", ".tmp");
        Files.writeString(temporal, "contenido-sql");

        storage.confirmar(temporal, ruta);

        assertTrue(storage.existe(ruta));
        try (InputStream entrada = storage.abrirLectura(ruta)) {
            assertEquals("contenido-sql", new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void abrirEscritura_debeCrearLosDirectoriosIntermedios() throws IOException {
        String ruta = storage.ruta(BackupTipo.AUTOMATICO, "anidado/backup.sql.gz");

        try (OutputStream salida = storage.abrirEscritura(ruta)) {
            salida.write("datos".getBytes(StandardCharsets.UTF_8));
        }

        assertTrue(Files.isRegularFile(raiz.resolve("automatic").resolve("anidado").resolve("backup.sql.gz")));
    }

    @Test
    void confirmar_debePublicarEnLaRutaDefinitivaYEliminarElTemporal() throws IOException {
        String ruta = storage.ruta(BackupTipo.MANUAL, "backup.sql.gz");
        Path temporal = Files.createTempFile("test-", ".tmp");
        Files.writeString(temporal, "sql");

        storage.confirmar(temporal, ruta);

        assertFalse(Files.exists(temporal), "el temporal debe consumirse al publicar");
        assertTrue(storage.existe(ruta));
    }

    @Test
    void eliminar_debeBorrarElArchivoYNoFallarSiYaNoExiste() throws IOException {
        String ruta = storage.ruta(BackupTipo.MANUAL, "backup.sql.gz");
        Path temporal = Files.createTempFile("test-", ".tmp");
        Files.writeString(temporal, "sql");
        storage.confirmar(temporal, ruta);

        storage.eliminar(ruta);
        assertFalse(storage.existe(ruta));

        assertDoesNotThrow(() -> storage.eliminar(ruta), "eliminar un archivo ausente no debe fallar");
    }

    @Test
    void existe_debeDevolverFalseParaRutasNoRegistradas() {
        assertFalse(storage.existe("manual/no-existe.sql.gz"));
    }

    @Test
    void rutaQueEscapaDelDirectorioRaiz_debeSerRechazada() throws IOException {
        String ruta = "../fuera-del-directorio.sql.gz";

        assertThrows(IllegalArgumentException.class, () -> storage.existe(ruta));
        assertThrows(IllegalArgumentException.class, () -> storage.eliminar(ruta));
    }

    @Test
    void abrirLectura_debePermitirDescomprimirElArchivoGzip() throws IOException {
        String ruta = storage.ruta(BackupTipo.MANUAL, "backup.sql.gz");
        Path temporal = Files.createTempFile("test-", ".tmp");
        try (GZIPOutputStream salida = new GZIPOutputStream(Files.newOutputStream(temporal))) {
            salida.write("-- dump sql\nCREATE TABLE t (id INT);".getBytes(StandardCharsets.UTF_8));
        }
        storage.confirmar(temporal, ruta);

        try (InputStream entrada = new GZIPInputStream(storage.abrirLectura(ruta))) {
            String sql = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(sql.contains("CREATE TABLE t"), "el contenido debe seguir siendo SQL valido tras comprimir");
        }
    }
}
