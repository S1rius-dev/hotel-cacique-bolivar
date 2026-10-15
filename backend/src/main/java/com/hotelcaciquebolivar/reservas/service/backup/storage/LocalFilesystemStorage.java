package com.hotelcaciquebolivar.reservas.service.backup.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;

import lombok.RequiredArgsConstructor;

/**
 * Implementacion de {@link BackupStorage} sobre el sistema de archivos local.
 *
 * <p>Es la implementacion por defecto (controlada por {@code app.backup.storage.provider=local}).
 * Conserva los backups en {@code <dir>/automatic/} y {@code <dir>/manual/}.</p>
 *
 * <p>Los archivos se escriben primero en un temporal y se publican con {@link #confirmar},
 * para que un mysqldump interrumpido no deje un .sql.gz truncado en su ruta definitiva,
 * que de otro modo el listado presentaria como si fuera restaurable.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.backup.storage", name = "provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalFilesystemStorage implements BackupStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesystemStorage.class);

    private final BackupProperties properties;

    @Override
    public ProveedorStorage proveedor() {
        return ProveedorStorage.LOCAL;
    }

    @Override
    public String ruta(BackupTipo tipo, String nombre) {
        String carpeta = tipo == BackupTipo.MANUAL
                ? properties.getStorage().getFolderManual()
                : properties.getStorage().getFolderAutomatic();
        return carpeta + "/" + nombre;
    }

    @Override
    public OutputStream abrirEscritura(String rutaRelativa) throws IOException {
        Path destino = resolver(rutaRelativa);
        Files.createDirectories(destino.getParent());
        return Files.newOutputStream(destino);
    }

    @Override
    public void confirmar(Path temporal, String rutaRelativa) throws IOException {
        Path destino = resolver(rutaRelativa);
        Files.createDirectories(destino.getParent());
        try {
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(temporal);
            throw e;
        }
        log.debug("Backup almacenado en {}", destino);
    }

    @Override
    public InputStream abrirLectura(String rutaRelativa) throws IOException {
        return Files.newInputStream(resolver(rutaRelativa));
    }

    @Override
    public boolean existe(String rutaRelativa) {
        return Files.isRegularFile(resolver(rutaRelativa));
    }

    @Override
    public void eliminar(String rutaRelativa) throws IOException {
        Files.deleteIfExists(resolver(rutaRelativa));
    }

    @Override
    public String ubicacion() {
        return raizAbsoluta().toString();
    }

    @Override
    public long tamano(String rutaRelativa) throws IOException {
        Path destino = resolver(rutaRelativa);
        return Files.isRegularFile(destino) ? Files.size(destino) : 0L;
    }

    /**
     * Resuelve una ruta relativa dentro del directorio raiz configurado.
     *
     * <p>Verifica que la ruta no escape del directorio raiz: un {@code storage_path}
     * corrupto o manipulado no debe permitir leer ni borrar archivos fuera del area de backups.</p>
     */
    private Path resolver(String rutaRelativa) {
        Path raiz = raizAbsoluta();
        Path destino = raiz.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException(
                    "Ruta de backup fuera del directorio configurado: " + rutaRelativa);
        }
        return destino;
    }

    private Path raizAbsoluta() {
        return Paths.get(properties.getStorage().getDir()).toAbsolutePath().normalize();
    }
}
