package com.hotelcaciquebolivar.reservas.service.backup.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.service.backup.BackupService;

import lombok.RequiredArgsConstructor;

/**
 * Tareas programadas del sistema de copias de seguridad.
 *
 * <p>Cada metodo es idempotente y protegido contra excepcion: una tarea programada que
 * propaga un error no detiene a las demas del pool, y aqui no se propaga ninguno.
 * {@link BackupService#crearAutomatico()} ya absorbe el fallo del dump y lo deja registrado
 * como FALLIDO, por lo que en este nivel solo se cubre el fallo de la propia infraestructura.</p>
 *
 * <p>El bean exige que esten activas tanto {@code app.backup.enabled} como
 * {@code app.backup.schedule.enabled}: declarar el cron pero dejar la programacion apagada
 * es un error de operacion frecuente, y sin esta condicion las tareas se registrarian igual
 * porque {@code @Scheduled} se procesa en el bean, no en el metodo.</p>
 *
 * <p>El cron y la zona horaria se leen de configuracion. Fijar la zona es obligatorio: el
 * servidor de produccion puede operar en UTC mientras el hotel opera en America/Bogota,
 * y un backup de las 2 AM Bogota disparado a las 9 PM seria una sorpresa desagradable.</p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.backup", name = { "enabled", "schedule.enabled" }, havingValue = "true")
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final BackupService backupService;
    private final BackupProperties properties;

    /**
     * Genera la copia de seguridad automatica segun el cron configurado.
     */
    @Scheduled(cron = "${app.backup.schedule.cron}", zone = "${app.backup.schedule.zone}")
    public void ejecutarBackupAutomatico() {
        if (!properties.getSchedule().isEnabled()) {
            log.debug("Backup automático omitido: app.backup.schedule.enabled=false");
            return;
        }
        log.info("=== INICIO BACKUP AUTOMATICO ===");
        try {
            var resultado = backupService.crearAutomatico();
            if (resultado != null) {
                log.info("=== FIN BACKUP AUTOMATICO: {} ({} bytes) ===",
                        resultado.archivoNombre(), resultado.tamanoBytes());
            } else {
                log.info("=== FIN BACKUP AUTOMATICO: omitido ===");
            }
        } catch (RuntimeException e) {
            log.error("Fallo la tarea de backup automático: {}", e.getMessage(), e);
        }
    }

    /**
     * Elimina las copias que exceden la politica de retencion.
     *
     * <p>Se ejecuta en una ventana distinta a la del backup, tipicamente de madrugada, para
     * que la purga compita con el dump por disco.</p>
     */
    @Scheduled(cron = "${app.backup.schedule.cleanup-cron}", zone = "${app.backup.schedule.zone}")
    public void purgarRetenidos() {
        if (!properties.getSchedule().isCleanupEnabled()) {
            log.debug("Purga de retenidos omitida: app.backup.schedule.cleanup-enabled=false");
            return;
        }
        try {
            int purgados = backupService.purgarAntiguos();
            log.info("Purga de retenidos completada: {} copias eliminadas "
                    + "(automáticos > {} días, manuales > {} días)",
                    purgados,
                    properties.getRetention().getAutomaticDays(),
                    formatDias(properties.getRetention().getManualDays()));
        } catch (RuntimeException e) {
            log.error("Fallo la purga de copias de seguridad: {}", e.getMessage(), e);
        }
    }

    private String formatDias(int dias) {
        return dias < 0 ? "nunca" : dias + " días";
    }
}
