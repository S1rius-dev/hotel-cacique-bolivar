package com.hotelcaciquebolivar.reservas.service.backup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupEnProgresoException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupNoDisponibleException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.mapper.BackupMapper;
import com.hotelcaciquebolivar.reservas.repository.backup.BackupHistoryRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlTarget;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;

import lombok.RequiredArgsConstructor;

/**
 * Implementacion de {@link BackupService}.
 *
 * <h2>Transacciones</h2>
 * <p>Ningun metodo que dispare mysqldump esta anotado como transaccional, y a proposito:
 * el dump es un proceso externo que puede tardar minutos, y mantener abierta una transaccion
 * de base de datos durante ese tiempo retendria bloqueos sobre las tablas con las que el
 * hotel opera. Cada llamada a {@code repository} abre su propia transaccion, de modo que un
 * registro EN_PROGRESO sobrevive a un rollback ajeno y el fallo siempre queda documentado.</p>
 *
 * <h2>Concurrencia</h2>
 * <p>Un unico {@link AtomicBoolean} serializa todas las ejecuciones. Sin el, un backup manual
 * que coincidiera con el de las 2 AM produciria dos procesos mysqldump compitiendo por el
 * ancho de banda de disco y dos registros EN_PROGRESO para el mismo instante. El guard se
 * libera siempre en un finally, incluso si mysqldump falla.</p>
 */
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    private static final String ENTIDAD_AUDIT = "BackupHistory";
    /**
     * Codigo de referencia para las notificaciones. El frontend lo usa como clave para
     * resolver la ruta del recurso enlazado, asi que debe seguir la convencion de
     * "PAGO", "PQRS" y "CLIENTE", no el nombre de la entidad Java.
     */
    private static final String REFERENCIA_TIPO = "BACKUP";
    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
    private static final int MAX_ERROR_CHARS = 2000;

    private final BackupHistoryRepository repository;
    private final BackupProperties properties;
    private final BackupStorage storage;
    private final MySqlDumpService dumpService;
    private final BackupMapper backupMapper;
    private final AuditService auditService;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;
    private final org.springframework.boot.jdbc.autoconfigure.DataSourceProperties dataSourceProperties;

    private final AtomicBoolean ejecucionEnCurso = new AtomicBoolean(false);

    @Override
    public BackupResponse crearManual(String emailUsuario) {
        verificarHabilitado();
        return ejecutar(BackupTipo.MANUAL, emailUsuario);
    }

    @Override
    public BackupResponse crearAutomatico() {
        if (!properties.isEnabled()) {
            log.debug("Backup automático omitido: app.backup.enabled=false");
            return null;
        }
        if (!ejecucionEnCurso.compareAndSet(false, true)) {
            log.warn("Backup automático omitido: ya hay una copia de seguridad en ejecución");
            return null;
        }
        try {
            return ejecutarInterno(BackupTipo.AUTOMATICO, null);
        } catch (RuntimeException e) {
            // Un fallo programado no debe propagarse al scheduler: ya quedó registrado
            // como FALLIDO y notificado a los administradores.
            log.error("Falló el backup automático programado: {}", e.getMessage());
            return null;
        } finally {
            ejecucionEnCurso.set(false);
        }
    }

    @Override
    public PageResponse<BackupResponse> listar(Pageable pageable, BackupTipo tipo, BackupEstado estado) {
        Page<BackupHistory> pagina = (tipo != null && estado != null)
                ? repository.findByTipoAndEstadoOrderByFechaDesc(tipo, estado, pageable)
                : (tipo != null)
                        ? repository.findByTipoOrderByFechaDesc(tipo, pageable)
                        : (estado != null)
                                ? repository.findByEstadoOrderByFechaDesc(estado, pageable)
                                : repository.findAllByOrderByFechaDesc(pageable);

        return new PageResponse<>(
                pagina.getContent().stream().map(backupMapper::toResponse).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }

    @Override
    public BackupResponse obtener(Long id) {
        return backupMapper.toResponse(buscar(id));
    }

    @Override
    public Descarga abrirDescarga(Long id) {
        BackupHistory registro = buscar(id);
        if (!storage.existe(registro.getStoragePath())) {
            throw new ResourceNotFoundException("Archivo de backup", registro.getArchivoNombre());
        }
        try {
            return new Descarga(registro.getArchivoNombre(), storage.abrirLectura(registro.getStoragePath()));
        } catch (IOException e) {
            throw new BackupExecutionException("No se pudo leer el archivo del backup: " + e.getMessage(), e);
        }
    }

    @Override
    public String eliminar(Long id, String emailUsuario) {
        BackupHistory registro = buscar(id);
        String nombreArchivo = registro.getArchivoNombre();
        try {
            storage.eliminar(registro.getStoragePath());
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo eliminar el archivo " + nombreArchivo + ": " + e.getMessage(), e);
        }
        repository.delete(registro);
        registrarAuditoria(AuditAction.BACKUP_DELETED, registro.getId(),
                "Copia de seguridad eliminada: " + nombreArchivo, emailUsuario);
        log.info("Backup eliminado: {} (id={})", nombreArchivo, id);
        return nombreArchivo;
    }

    @Override
    public BackupDashboardResponse dashboard() {
        BackupHistory ultimoExitoso = repository
                .findFirstByEstadoOrderByFechaDesc(BackupEstado.EXITOSO)
                .orElse(null);
        boolean enCurso = repository
                .findFirstByEstadoOrderByFechaDesc(BackupEstado.EN_PROGRESO)
                .isPresent();

        long automaticos = repository.countByTipoAndEstado(BackupTipo.AUTOMATICO, BackupEstado.EXITOSO);
        long manuales = repository.countByTipoAndEstado(BackupTipo.MANUAL, BackupEstado.EXITOSO);
        long totalBytes = repository.sumTamanoBytes(BackupTipo.AUTOMATICO, BackupEstado.EXITOSO)
                + repository.sumTamanoBytes(BackupTipo.MANUAL, BackupEstado.EXITOSO);

        return new BackupDashboardResponse(
                ultimoExitoso != null ? ultimoExitoso.getFecha() : null,
                ultimoExitoso != null ? ultimoExitoso.getArchivoNombre() : null,
                enCurso,
                automaticos + manuales,
                repository.countByEstado(BackupEstado.FALLIDO),
                totalBytes,
                automaticos,
                manuales,
                storage.proveedor().name(),
                calcularProximaEjecucion(),
                storage.ubicacion(),
                // La UI necesita saber que base no puede usar como destino del clon.
                MySqlTarget.parse(dataSourceProperties.determineUrl()).baseDeDatos());
    }

    @Override
    public int purgarAntiguos() {
        int eliminados = purgar(BackupTipo.AUTOMATICO, properties.getRetention().getAutomaticDays())
                + purgar(BackupTipo.MANUAL, properties.getRetention().getManualDays());
        if (eliminados > 0) {
            log.info("Purga de retención: {} copias eliminadas", eliminados);
        }
        return eliminados;
    }

    @Override
    public int limpiarEjecucionesPendientes() {
        // Margen extra sobre el timeout: un proceso destruido a la fuerza puede tardar
        // un poco más en dejar de escribir, y no queremos marcar como fallido un dump vivo.
        LocalDateTime corte = LocalDateTime.now()
                .minusSeconds(properties.getTimeoutSeconds())
                .minusMinutes(5);

        List<BackupHistory> pendientes = repository.findByEstadoAndFechaBefore(BackupEstado.EN_PROGRESO, corte);
        for (BackupHistory registro : pendientes) {
            try {
                registro.setEstado(BackupEstado.FALLIDO);
                registro.setMensajeError("La ejecución quedó interrumpida: no registró fin dentro del tiempo esperado.");
                repository.save(registro);
                log.warn("Backup {} marcado como fallido por interrupción previa", registro.getArchivoNombre());
            } catch (DataAccessException e) {
                log.error("No se pudo corregir el backup {}: {}", registro.getArchivoNombre(), e.getMessage());
            }
        }
        return pendientes.size();
    }

    @Override
    public String generarNombreArchivo(BackupTipo tipo) {
        String prefijo = tipo == BackupTipo.MANUAL ? "backup-manual-" : "backup-";
        return prefijo + FORMATO_ARCHIVO.format(LocalDateTime.now()) + ".sql.gz";
    }

    /**
     * Ejecuta una copia de seguridad tomando el guard de concurrencia.
     */
    private BackupResponse ejecutar(BackupTipo tipo, String emailUsuario) {
        if (!ejecucionEnCurso.compareAndSet(false, true)) {
            throw new BackupEnProgresoException();
        }
        try {
            return ejecutarInterno(tipo, emailUsuario);
        } finally {
            ejecucionEnCurso.set(false);
        }
    }

    /**
     * Cuerpo comun de la creacion. El guard de concurrencia ya esta tomado por el llamador.
     */
    private BackupResponse ejecutarInterno(BackupTipo tipo, String emailUsuario) {
        limpiarEjecucionesPendientes();

        String nombreArchivo = generarNombreArchivo(tipo);
        String rutaRelativa = storage.ruta(tipo, nombreArchivo);

        BackupHistory registro = new BackupHistory();
        registro.setTipo(tipo);
        registro.setEstado(BackupEstado.EN_PROGRESO);
        registro.setArchivoNombre(nombreArchivo);
        registro.setStoragePath(rutaRelativa);
        registro.setProveedor(storage.proveedor());
        registro.setUsuario(emailUsuario);
        registro = repository.save(registro);

        try {
            ResultadoDump resultado = dumpService.ejecutarYComprimir(rutaRelativa);

            registro.setEstado(BackupEstado.EXITOSO);
            registro.setTamanoBytes(resultado.tamanoBytes());
            registro.setDuracionMs(resultado.duracionMs());
            registro.setChecksumSha256(resultado.checksumSha256());
            registro = repository.save(registro);

            String detalle = "Copia " + etiquetaTipo(tipo) + " creada: " + nombreArchivo
                    + " (" + resultado.tamanoBytes() + " bytes)";
            registrarAuditoria(AuditAction.BACKUP_CREATED, registro.getId(), detalle, emailUsuario);
            notificar(TipoNotificacion.BACKUP_EXITOSO, registro, detalle, emailUsuario);
            return backupMapper.toResponse(registro);

        } catch (RuntimeException e) {
            marcarFallido(registro, e);
            String detalle = "Copia " + etiquetaTipo(tipo) + " fallida: " + nombreArchivo
                    + " — " + e.getMessage();
            registrarAuditoria(AuditAction.BACKUP_FAILED, registro.getId(), detalle, emailUsuario);
            notificar(TipoNotificacion.BACKUP_FALLIDO, registro,
                    "Falló la copia de seguridad: " + e.getMessage(), emailUsuario);
            throw e;
        }
    }

    /**
     * Persiste el estado fallido. Si el registro no llego a tener id, la escritura falla y
     * el error real del dump es lo que se propaga al llamador.
     */
    private void marcarFallido(BackupHistory registro, RuntimeException causa) {
        try {
            registro.setEstado(BackupEstado.FALLIDO);
            registro.setMensajeError(truncar(causa.getMessage()));
            repository.save(registro);
        } catch (DataAccessException e) {
            log.error("No se pudo registrar el fallo del backup {}: {}",
                    registro.getArchivoNombre(), e.getMessage());
        }
    }

    /**
     * Elimina las copias de un tipo que exceden su ventana de retencion.
     *
     * <p>Solo actua sobre registros EXITOSO ya finalizados: una copia EN_PROGRESO nunca se
     * purga, ni siquiera si su fecha es vieja, porque el archivo podria estar escribiendose.</p>
     */
    private int purgar(BackupTipo tipo, int dias) {
        if (dias < 0) {
            log.debug("Retención de backups {}: indefinida, no se purga", tipo);
            return 0;
        }
        LocalDateTime corte = LocalDateTime.now().minusDays(dias);
        Page<BackupHistory> pagina = repository
                .findByTipoAndEstadoOrderByFechaDesc(tipo, BackupEstado.EXITOSO, Pageable.unpaged());
        if (pagina == null) {
            return 0;
        }

        int purgados = 0;
        for (BackupHistory registro : pagina.getContent()) {
            if (!registro.getFecha().isBefore(corte)) {
                continue;
            }
            try {
                eliminar(registro.getId(), null);
                purgados++;
            } catch (RuntimeException e) {
                log.error("No se pudo purgar el backup {}: {}", registro.getArchivoNombre(), e.getMessage());
            }
        }
        return purgados;
    }

    private BackupHistory buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Backup", id));
    }

    private void verificarHabilitado() {
        if (!properties.isEnabled()) {
            throw new BackupNoDisponibleException(
                    "El sistema de copias de seguridad está deshabilitado (app.backup.enabled=false)");
        }
    }

    private void registrarAuditoria(AuditAction accion, Long id, String detalle, String emailUsuario) {
        try {
            auditService.log(accion, ENTIDAD_AUDIT, id, detalle, emailUsuario);
        } catch (RuntimeException e) {
            // La auditoría nunca debe enmascarar el resultado real de una operación crítica.
            log.error("Falló la auditoría del backup {}: {}", id, e.getMessage());
        }
    }

    private void notificar(TipoNotificacion tipo, BackupHistory registro, String mensaje, String emailExcluido) {
        try {
            NotificacionMessageBuilder.ActorInfo actor = notificacionMessageBuilder.obtenerActor();
            Map<String, Object> detalles = new HashMap<>();
            detalles.put("archivo", registro.getArchivoNombre());
            detalles.put("tipo", registro.getTipo().name());
            detalles.put("estado", registro.getEstado().name());
            detalles.put("tamanoBytes", registro.getTamanoBytes());

            notificacionService.notificarATodos(tipo, mensaje,
                    actor.email(), actor.nombre(), detalles,
                    REFERENCIA_TIPO, registro.getId(),
                    List.of(Rol.ADMIN), emailExcluido);
        } catch (RuntimeException e) {
            log.error("Falló la notificación del backup {}: {}", registro.getId(), e.getMessage());
        }
    }

    /**
     * Proxima ejecucion del cron de backup automatico, o {@code null} si no esta programado.
     */
    private String calcularProximaEjecucion() {
        if (!properties.getSchedule().isEnabled()) {
            return null;
        }
        try {
            ZoneId zona = ZoneId.of(properties.getSchedule().getZone());
            return CronExpression.parse(properties.getSchedule().getCron())
                    .next(LocalDateTime.now(zona))
                    .toString();
        } catch (RuntimeException e) {
            log.warn("No se pudo calcular la próxima ejecución del backup: {}", e.getMessage());
            return null;
        }
    }

    private String etiquetaTipo(BackupTipo tipo) {
        return tipo == BackupTipo.MANUAL ? "manual" : "automática";
    }

    private String truncar(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "Error desconocido";
        }
        return mensaje.length() > MAX_ERROR_CHARS ? mensaje.substring(0, MAX_ERROR_CHARS) : mensaje;
    }
}
