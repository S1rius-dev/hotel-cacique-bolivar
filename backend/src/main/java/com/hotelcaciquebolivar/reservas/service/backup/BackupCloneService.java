package com.hotelcaciquebolivar.reservas.service.backup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupVerificacionResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupDestinoInvalidoException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.repository.backup.BackupHistoryRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlClientCredentials;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlTarget;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MysqlProcessRunner;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;

import lombok.RequiredArgsConstructor;

/**
 * Verifica la integridad de una copia y la restaura en una base de datos nueva.
 *
 * <h2>Por que clonar y no restaurar encima</h2>
 * <p>Restaurar sobre la base que la aplicacion tiene abierta significa reemplazar el
 * schema y los datos bajo las conexiones vivas de HikariCP y Hibernate, con bloqueos en
 * metadata locks y un cache de segundo nivel desalineado. El resultado tipico es un
 * arranque caido. Clonar a una base aparte elimina ese riesgo: la base viva no se toca
 * y el operador puede inspeccionar la copia antes de decidir que hacer con ella.</p>
 * <p>Por esta razon {@link #validarNombreDestino} rechaza de forma dura cualquier destino
 * igual a la base configurada de la aplicacion. No es una advertencia: es un error.</p>
 *
 * <h2>Seguridad de la importacion</h2>
 * <ul>
 *   <li>Se recalcula el SHA-256 del .sql.gz y se compara con el registrado al crear la copia.
 *       Importar un archivo corrupto seria peor que no importar.</li>
 *   <li>El .sql.gz se descomprime en memoria (Java) y se canaliza por stdin a
 *       {@code mysql}, sin descomprimir a disco ni construir un .sql temporal.</li>
 *   <li>El destino se crea con una sentencia propia y el dump se importa sin
 *       {@code CREATE DATABASE} ni {@code USE}, de modo que las tablas caen en la base
 *       indicada por {@code --database}.</li>
 *   <li>Si MySQL falla a mitad, la base destino se elimina: una base clonada a medias es
 *       peor que ninguna, porque parece restaurada sin serlo.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class BackupCloneService {

    private static final Logger log = LoggerFactory.getLogger(BackupCloneService.class);

    /** Nombres de base admitidos: letras, digitos y guion bajo, hasta 64 caracteres. */
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    private static final String ENTIDAD_AUDIT = "BackupHistory";

    private final BackupHistoryRepository repository;
    private final BackupStorage storage;
    private final BackupProperties properties;
    private final DataSourceProperties dataSourceProperties;
    private final AuditService auditService;

    /**
     * Recalcula el SHA-256 de la copia y lo compara con el registrado al crearla.
     *
     * <p>Solo lee el archivo, no modifica nada, por lo que es seguro ejecutarlo sobre
     * cualquier copia. El nombre de la base de datos que aparece en el dump se compara
     * en mayusculas porque MySQL no distingue mayusculas en los nombres de schema.</p>
     *
     * @param id          identificador de la copia
     * @param emailUsuario email del administrador, o {@code null} si es la purga automatica
     * @return el resultado de la verificacion, con el checksum esperado y el recalculado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si la copia no existe
     */
    @Transactional(readOnly = true)
    public BackupVerificacionResponse verificar(Long id, String emailUsuario) {
        BackupHistory copia = obtener(id);

        if (!storage.existe(copia.getStoragePath())) {
            throw new BackupExecutionException(
                    "El archivo de la copia no existe en el almacenamiento: " + copia.getStoragePath());
        }

        String esperado = copia.getChecksumSha256();
        String calculado = sha256(copia.getStoragePath());
        boolean integro = esperado != null && esperado.equalsIgnoreCase(calculado);

        String detalle = String.format(Locale.ROOT, "copia=%d esperado=%s calculado=%s integro=%s",
                id, esperado, calculado, integro);
        registrarAuditoria(integro ? AuditAction.BACKUP_VERIFIED : null, id, detalle, emailUsuario);

        if (!integro) {
            log.warn("Checksum discrepante en la copia {}: esperado={} calculado={}", id, esperado, calculado);
        }
        return new BackupVerificacionResponse(id, integro, esperado, calculado, copia.getStoragePath());
    }

    /**
     * Restaura la copia en una base de datos nueva y devuelve el nombre creado.
     *
     * <p>Bloqueante: retorna cuando mysql termina de importar. Ante cualquier fallo la base
     * destino se elimina para no dejar un clon incompleto.</p>
     *
     * @param id           identificador de la copia
     * @param baseDestino  nombre de la base a crear, entre 1 y 64 caracteres alfanumericos
     * @param emailUsuario email del administrador que solicita el clon
     * @return el nombre de la base creada
     * @throws BackupExecutionException si el nombre es invalido, si el destino es la base
     *                                  de la aplicacion, si el checksum no coincide o si
     *                                  MySQL falla
     */
    public String clonar(Long id, String baseDestino, String emailUsuario) {
        BackupHistory copia = obtener(id);
        MySqlTarget origen = leerObjetivo();

        String destino = validarNombreDestino(baseDestino, origen);
        verificarArchivoYChecksum(copia, destino);

        long inicio = System.nanoTime();
        // Si algo falla hay que deshacer: una base a medias parece restaurada sin serlo.
        boolean creada = false;
        try (MySqlClientCredentials credenciales = MySqlClientCredentials.crear(dataSourceProperties)) {
            crearBase(origen, destino, credenciales.argumento());
            creada = true;
            importar(copia, origen, destino, credenciales.argumento());
        } catch (RuntimeException e) {
            if (creada) {
                eliminarBase(origen, destino, e);
            }
            throw e;
        }

        long duracionMs = (System.nanoTime() - inicio) / 1_000_000L;
        String detalle = String.format(Locale.ROOT, "copia=%d baseDestino=%s duracionMs=%d", id, destino, duracionMs);
        registrarAuditoria(AuditAction.BACKUP_CLONED, id, detalle, emailUsuario);
        log.info("Copia {} clonada en la base '{}' en {} ms", id, destino, duracionMs);
        return destino;
    }

    /**
     * Crea la base de datos destino con utf8mb4, para que los clientes con tildes y
     * emojis que ya estan en el dump no se trunquen al reimportar.
     */
    private void crearBase(MySqlTarget origen, String destino, String argumentoCredenciales) {
        MysqlProcessRunner.Resultado resultado = MysqlProcessRunner.ejecutar(
                comandoMySql(argumentoCredenciales, origen, List.of(
                        "-e", "CREATE DATABASE `" + destino + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")),
                properties.getClon().getTimeoutSeconds(),
                "la creacion de la base " + destino);

        if (resultado.codigoSalida() != 0) {
            throw new BackupExecutionException(
                    "No se pudo crear la base '" + destino + "': " + resultado.diagnostico());
        }
    }

    /**
     * Canaliza el .sql.gz descomprimido a mysql por stdin.
     *
     * <p>El archivo se abre una vez y el mismo stream alimenta al runner, que ya se
     * encarga de copiar en un hilo aparte y de esperar con timeout. No hace falta
     * concatenar ni comprimir a mano: GZIPInputStream termina en el Runnable y se cierra
     * con el try-with-resources.</p>
     */
    private void importar(BackupHistory copia, MySqlTarget origen, String destino, String argumentoCredenciales) {
        List<String> comando = comandoMySql(argumentoCredenciales, origen,
                List.of("--database=" + destino, "--default-character-set=utf8mb4"));

        try (InputStream archivo = storage.abrirLectura(copia.getStoragePath());
                InputStream gzip = new GZIPInputStream(new BufferedInputStream(archivo), 65536)) {

            MysqlProcessRunner.Resultado resultado = MysqlProcessRunner.conEntrada(
                    comando, stdin -> copiar(gzip, stdin), properties.getClon().getTimeoutSeconds(),
                    "la importacion en '" + destino + "'");

            if (resultado.codigoSalida() != 0) {
                throw new BackupExecutionException(
                        "MySQL termino con codigo " + resultado.codigoSalida()
                        + " al importar en '" + destino + "': " + resultado.diagnostico());
            }
            if (!resultado.diagnostico().isBlank()) {
                log.warn("mysql emitio diagnostico al importar en {}: {}", destino, resultado.diagnostico());
            }
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo leer el archivo comprimido de la copia " + copia.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Copia el origen al destino por bloques. El buffer importa: el dump descomprimido
     * puede pesar cientos de MB y nunca debe cargarse completo en memoria.
     */
    private static void copiar(InputStream origen, OutputStream destino) throws IOException {
        byte[] buffer = new byte[65536];
        int leidos;
        while ((leidos = origen.read(buffer)) != -1) {
            destino.write(buffer, 0, leidos);
        }
    }

    /**
     * Construye la invocacion de mysql con el archivo de credenciales como primer argumento.
     */
    private List<String> comandoMySql(String argumentoCredenciales, MySqlTarget origen, List<String> extra) {
        List<String> comando = new ArrayList<>();
        comando.add(properties.getMysqlPath());
        // Debe ir primero: MySQL lee este archivo antes de aplicar cualquier otra opcion.
        comando.add(argumentoCredenciales);
        comando.add("--host=" + origen.host());
        comando.add("--port=" + origen.port());
        comando.addAll(extra);
        return comando;
    }

    /**
     * Intenta deshacer un clon fallido. Un fallo aqui se registra pero no reemplaza el
     * error original, que es el que explica por que fallo el clon.
     */
    private void eliminarBase(MySqlTarget origen, String destino, RuntimeException causa) {
        try (MySqlClientCredentials credenciales = MySqlClientCredentials.crear(dataSourceProperties)) {
            MysqlProcessRunner.ejecutar(
                    comandoMySql(credenciales.argumento(), origen, List.of("-e", "DROP DATABASE `" + destino + "`")),
                    properties.getClon().getTimeoutSeconds(),
                    "la limpieza de la base " + destino);
            log.warn("Base '{}' eliminada tras un clon fallido", destino);
        } catch (RuntimeException e) {
            log.error("No se pudo eliminar la base parcial '{}': {}", destino, e.getMessage());
            causa.addSuppressed(e);
        }
    }

    /**
     * Rechaza destinos imposibles y, sobre todo, la base que la aplicacion tiene abierta.
     */
    String validarNombreDestino(String baseDestino, MySqlTarget origen) {
        String destino = baseDestino == null ? "" : baseDestino.trim();
        if (!NOMBRE_VALIDO.matcher(destino).matches()) {
            throw new BackupDestinoInvalidoException(
                    "Nombre de base de datos invalido: '" + baseDestino + "'. "
                    + "Solo se admiten letras, digitos y guion bajo (1 a 64 caracteres).");
        }
        // Comparacion insensible a mayusculas porque MySQL no distingue en los nombres de schema.
        if (destino.equalsIgnoreCase(origen.baseDeDatos())) {
            throw new BackupDestinoInvalidoException(
                    "No se puede restaurar sobre '" + origen.baseDeDatos() + "', que es la base en uso. "
                    + "Elija otra base: la copia se restaura siempre en una base nueva.");
        }
        return destino;
    }

    /**
     * Exige que el archivo exista y que su checksum coincida antes de importar.
     *
     * <p>Importar un .sql.gz corrupto produce una base incompleta que parece restaurada;
     * es preferible fallar antes de tocar el servidor.</p>
     */
    private void verificarArchivoYChecksum(BackupHistory copia, String destino) {
        if (!storage.existe(copia.getStoragePath())) {
            throw new BackupExecutionException(
                    "El archivo de la copia no existe en el almacenamiento: " + copia.getStoragePath());
        }
        String esperado = copia.getChecksumSha256();
        if (esperado == null || esperado.isBlank()) {
            throw new BackupExecutionException(
                    "La copia " + copia.getId() + " no tiene checksum registrado; no se puede verificar su integridad.");
        }
        String calculado = sha256(copia.getStoragePath());
        if (!esperado.equalsIgnoreCase(calculado)) {
            throw new BackupExecutionException(
                    "La copia " + copia.getId() + " esta danada (SHA-256 esperado " + esperado
                    + ", calculado " + calculado + "). No se importo en '" + destino + "'.");
        }
    }

    /**
     * Recalcula el SHA-256 del archivo almacenado.
     *
     * <p>Lee en streaming porque el archivo puede pesar cientos de MB y no debe caber
     * en memoria solo para comprobar que no cambio.</p>
     */
    private String sha256(String rutaRelativa) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream origen = storage.abrirLectura(rutaRelativa);
                    DigestInputStream entrada = new DigestInputStream(new BufferedInputStream(origen, 65536), digest)) {
                byte[] buffer = new byte[65536];
                while (entrada.read(buffer) != -1) {
                    // El digest se actualiza en cada read; no hay que acumular nada.
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new BackupExecutionException("SHA-256 no disponible en esta JVM", e);
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo leer la copia para verificar su integridad: " + e.getMessage(), e);
        }
    }

    private BackupHistory obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Copia de seguridad", id));
    }

    private MySqlTarget leerObjetivo() {
        return MySqlTarget.parse(dataSourceProperties.determineUrl());
    }

    private void registrarAuditoria(AuditAction accion, Long id, String detalle, String emailUsuario) {
        if (accion == null) {
            return;
        }
        try {
            auditService.log(accion, ENTIDAD_AUDIT, id, detalle, emailUsuario);
        } catch (Exception e) {
            // La auditoria nunca debe enmascarar el resultado real de una operacion critica.
            log.error("Fallo la auditoria del clon del backup {}: {}", id, e.getMessage());
        }
    }
}
