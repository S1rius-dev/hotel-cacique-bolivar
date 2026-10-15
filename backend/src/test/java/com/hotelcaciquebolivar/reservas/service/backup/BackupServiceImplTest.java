package com.hotelcaciquebolivar.reservas.service.backup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupEnProgresoException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupNoDisponibleException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.mapper.BackupMapper;
import com.hotelcaciquebolivar.reservas.repository.backup.BackupHistoryRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;

/**
 * Tests de {@link BackupServiceImpl}.
 *
 * <p>Se cubre el ciclo de vida completo del historial (alta, exito, fallo, borrado, purga y
 * recuperacion de ejecuciones interrumpidas) mas los dos riesgos operativos reales: que el
 * guard de concurrencia impida dos mysqldump simultaneos, y que una falla de auditoria o de
 * notificacion no enmascare el resultado de la operacion.</p>
 */
@ExtendWith(MockitoExtension.class)
class BackupServiceImplTest {

    @Mock
    private BackupHistoryRepository repository;
    @Mock
    private BackupStorage storage;
    @Mock
    private MySqlDumpService dumpService;
    @Mock
    private BackupMapper backupMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private NotificacionMessageBuilder notificacionMessageBuilder;
    @Mock
    private org.springframework.boot.jdbc.autoconfigure.DataSourceProperties dataSourceProperties;

    private BackupProperties properties;
    private BackupServiceImpl backupService;

    @BeforeEach
    void setUp() {
        properties = new BackupProperties();
        properties.setEnabled(true);
        backupService = new BackupServiceImpl(repository, properties, storage, dumpService,
                backupMapper, auditService, notificacionService, notificacionMessageBuilder,
                dataSourceProperties);

        lenient().when(dataSourceProperties.determineUrl())
                .thenReturn("jdbc:mysql://localhost:3306/hotelcaciquebolivar?useSSL=false");

        lenient().when(notificacionMessageBuilder.obtenerActor())
                .thenReturn(new ActorInfo("admin@hotel.com", "Admin", "ADMIN", "ADMIN", "Admin"));
        lenient().when(storage.proveedor()).thenReturn(ProveedorStorage.LOCAL);
        lenient().when(repository.findByEstadoAndFechaBefore(eq(BackupEstado.EN_PROGRESO), any()))
                .thenReturn(List.of());
        lenient().when(repository.save(any(BackupHistory.class)))
                .thenAnswer(invocacion -> {
                    BackupHistory registro = invocacion.getArgument(0);
                    if (registro.getId() == null) {
                        registro.setId(1L);
                    }
                    return registro;
                });
    }

    // ============================================================
    // CREAR MANUAL
    // ============================================================

    @Test
    void crearManual_exito_debeRegistrarElBackupComoExitoso() {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenReturn(new ResultadoDump(2048L, "abc123", 900L));

        backupService.crearManual("admin@hotel.com");

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);
        verify(repository, atLeastOnce()).save(captor.capture());

        BackupHistory guardado = captor.getAllValues().getLast();
        assertEquals(BackupEstado.EXITOSO, guardado.getEstado());
        assertEquals(2048L, guardado.getTamanoBytes());
        assertEquals("abc123", guardado.getChecksumSha256());
        assertEquals(900L, guardado.getDuracionMs());
        assertEquals(ProveedorStorage.LOCAL, guardado.getProveedor());
        assertEquals("admin@hotel.com", guardado.getUsuario());
    }

    @Test
    void crearManual_exito_debeAuditarYNotificar() {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenReturn(new ResultadoDump(1024L, "hash", 500L));

        backupService.crearManual("admin@hotel.com");

        verify(auditService).log(eq(AuditAction.BACKUP_CREATED), eq("BackupHistory"), eq(1L),
                contains("manual"), eq("admin@hotel.com"));
        verify(notificacionService).notificarATodos(eq(TipoNotificacion.BACKUP_EXITOSO),
                anyString(), any(), any(), any(), eq("BACKUP"), eq(1L), any(), eq("admin@hotel.com"));
    }

    @Test
    void crearManual_falloDelDump_debeMarcarElRegistroComoFallidoYPropagar() {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenThrow(new BackupExecutionException("mysqldump termino con codigo 2"));

        assertThrows(BackupExecutionException.class, () -> backupService.crearManual("admin@hotel.com"));

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);
        verify(repository, atLeastOnce()).save(captor.capture());

        BackupHistory fallido = captor.getAllValues().getLast();
        assertEquals(BackupEstado.FALLIDO, fallido.getEstado());
        assertTrue(fallido.getMensajeError().contains("codigo 2"));
    }

    @Test
    void crearManual_sistemaDeshabilitado_debeResponderNoDisponible() {
        properties.setEnabled(false);

        assertThrows(BackupNoDisponibleException.class, () -> backupService.crearManual("admin@hotel.com"));

        verifyNoInteractions(dumpService);
    }

    @Test
    void crearManual_conDumpEnCurso_debeRechazarLaSegundaPeticion() throws Exception {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        // El primer dump se queda bloqueado: simula un mysqldump de varios minutos.
        when(dumpService.ejecutarYComprimir(anyString())).thenAnswer(invocacion -> {
            Thread.sleep(400);
            return new ResultadoDump(10L, "hash", 400L);
        });

        var primerHilo = new Thread(() -> backupService.crearManual("admin@hotel.com"));
        primerHilo.start();
        Thread.sleep(120);

        assertThrows(BackupEnProgresoException.class, () -> backupService.crearManual("otro@hotel.com"),
                "dos mysqldump simultaneos se pelearian el disco sin aportar nada");

        primerHilo.join();
    }

    @Test
    void crearManual_luegoDeFallar_debeLiberarElGuardDeConcurrencia() {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenThrow(new BackupExecutionException("fallo"));

        assertThrows(BackupExecutionException.class, () -> backupService.crearManual("admin@hotel.com"));

        // doReturn y no when(): un when() sobre un mock que ya lanza ejecutaria el throw
        // dentro de la propia llamada a when.
        doReturn(new ResultadoDump(5L, "hash", 1L)).when(dumpService).ejecutarYComprimir(anyString());

        assertDoesNotThrow(() -> backupService.crearManual("admin@hotel.com"),
                "tras un fallo el sistema no debe quedar bloqueado");
    }

    @Test
    void crearManual_falloDeAuditoria_noDebeEnmascararElExitoDelBackup() {
        when(storage.ruta(eq(BackupTipo.MANUAL), anyString())).thenReturn("manual/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenReturn(new ResultadoDump(1024L, "hash", 100L));
        doThrow(new RuntimeException("tabla audit_logs caida")).when(auditService)
                .log(any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> backupService.crearManual("admin@hotel.com"));
    }

    // ============================================================
    // CREAR AUTOMATICO
    // ============================================================

    @Test
    void crearAutomatico_sistemaDeshabilitado_debeOmitirseEnSilencio() {
        properties.setEnabled(false);

        assertNull(backupService.crearAutomatico());
        verifyNoInteractions(dumpService);
    }

    @Test
    void crearAutomatico_exito_debeUsarElTipoAutomatico() {
        when(storage.ruta(eq(BackupTipo.AUTOMATICO), anyString())).thenReturn("automatic/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenReturn(new ResultadoDump(4096L, "hash", 2000L));

        backupService.crearAutomatico();

        verify(storage).ruta(eq(BackupTipo.AUTOMATICO), startsWith("backup-"));
        verify(auditService).log(eq(AuditAction.BACKUP_CREATED), any(), eq(1L), contains("automática"), isNull());
    }

    @Test
    void crearAutomatico_fallo_delDump_noDebePropagarLaExcepcionAlScheduler() {
        when(storage.ruta(eq(BackupTipo.AUTOMATICO), anyString())).thenReturn("automatic/backup.sql.gz");
        when(dumpService.ejecutarYComprimir(anyString()))
                .thenThrow(new BackupExecutionException("mysqldump no encontrado"));

        assertNull(backupService.crearAutomatico(), "una tarea programada no debe propagar fallos");

        verify(notificacionService).notificarATodos(eq(TipoNotificacion.BACKUP_FALLIDO),
                contains("mysqldump no encontrado"), any(), any(), any(), any(), any(), any(), isNull());
    }

    // ============================================================
    // CONSULTA
    // ============================================================

    @Test
    void listar_sinFiltros_debeUsarLaConsultaGeneral() {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.AUTOMATICO);
        when(repository.findAllByOrderByFechaDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(registro)));
        when(backupMapper.toResponse(registro)).thenReturn(respuestaDe(registro));

        var resultado = backupService.listar(Pageable.unpaged(), null, null);

        assertEquals(1, resultado.totalElements());
        verify(repository).findAllByOrderByFechaDesc(any(Pageable.class));
    }

    @Test
    void listar_conFiltros_debeUsarLaConsultaCombinada() {
        when(repository.findByTipoAndEstadoOrderByFechaDesc(eq(BackupTipo.MANUAL), eq(BackupEstado.EXITOSO), any()))
                .thenReturn(new PageImpl<>(List.of()));

        backupService.listar(Pageable.unpaged(), BackupTipo.MANUAL, BackupEstado.EXITOSO);

        verify(repository).findByTipoAndEstadoOrderByFechaDesc(
                eq(BackupTipo.MANUAL), eq(BackupEstado.EXITOSO), any());
    }

    @Test
    void obtener_noEncontrado_debeLanzarResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> backupService.obtener(99L));
    }

    @Test
    void abrirDescarga_archivoAusenteEnDisco_debeReportarNoEncontrado() {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.MANUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(registro));
        when(storage.existe(registro.getStoragePath())).thenReturn(false);

        ResourceNotFoundException error =
                assertThrows(ResourceNotFoundException.class, () -> backupService.abrirDescarga(1L));

        assertTrue(error.getMessage().contains(registro.getArchivoNombre()),
                "el error debe nombrar el archivo que falta para que el operador sepa que buscar");
    }

    @Test
    void abrirDescarga_exitoso_debeDevolverElStreamDelArchivo() throws IOException {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.MANUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(registro));
        when(storage.existe(anyString())).thenReturn(true);
        when(storage.abrirLectura(anyString()))
                .thenReturn(new ByteArrayInputStream("sql".getBytes()));

        BackupService.Descarga descarga = backupService.abrirDescarga(1L);

        assertEquals(registro.getArchivoNombre(), descarga.nombreArchivo());
        assertNotNull(descarga.contenido());
    }

    // ============================================================
    // ELIMINAR Y PURGAR
    // ============================================================

    @Test
    void eliminar_debeBorrarArchivoYRegistroYAuditar() throws IOException {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.MANUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(registro));

        String nombre = backupService.eliminar(1L, "admin@hotel.com");

        assertEquals(registro.getArchivoNombre(), nombre);
        verify(storage).eliminar(registro.getStoragePath());
        verify(repository).delete(registro);
        verify(auditService).log(eq(AuditAction.BACKUP_DELETED), eq("BackupHistory"), eq(1L),
                anyString(), eq("admin@hotel.com"));
    }

    @Test
    void eliminar_falloAlBorrarElArchivo_noDebeBorrarElRegistro() throws IOException {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.MANUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(registro));
        doThrow(new IOException("archivo bloqueado")).when(storage).eliminar(anyString());

        assertThrows(BackupExecutionException.class, () -> backupService.eliminar(1L, "admin@hotel.com"));

        verify(repository, never()).delete(any(BackupHistory.class));
    }

    @Test
    void purgarAntiguos_retencionIndefinida_noDebePurgarManuales() {
        properties.getRetention().setManualDays(-1);
        when(repository.findByTipoAndEstadoOrderByFechaDesc(eq(BackupTipo.AUTOMATICO), eq(BackupEstado.EXITOSO), any()))
                .thenReturn(new PageImpl<>(List.of()));

        int purgados = backupService.purgarAntiguos();

        assertEquals(0, purgados, "-1 significa que los backups manuales nunca se borran");
        verify(repository, never()).findByTipoAndEstadoOrderByFechaDesc(
                eq(BackupTipo.MANUAL), any(BackupEstado.class), any(Pageable.class));
        verify(repository, never()).delete(any(BackupHistory.class));
    }

    @Test
    void purgarAntiguos_debeEliminarSoloLosFueraDeLaVentana() {
        properties.getRetention().setAutomaticDays(30);
        properties.getRetention().setManualDays(-1);
        BackupHistory viejo = registroAntiguo();
        when(repository.findByTipoAndEstadoOrderByFechaDesc(eq(BackupTipo.AUTOMATICO), eq(BackupEstado.EXITOSO), any()))
                .thenReturn(new PageImpl<>(List.of(viejo, registroExistente(BackupEstado.EXITOSO, BackupTipo.AUTOMATICO))));
        when(repository.findById(viejo.getId())).thenReturn(Optional.of(viejo));

        int purgados = backupService.purgarAntiguos();

        assertEquals(1, purgados);
        verify(repository).delete(viejo);
    }

    // ============================================================
    // EJECUCIONES INTERRUMPIDAS Y DASHBOARD
    // ============================================================

    @Test
    void limpiarEjecucionesPendientes_debeMarcarComoFallidasLasInterrumpidas() {
        BackupHistory interrumpido = registroExistente(BackupEstado.EN_PROGRESO, BackupTipo.AUTOMATICO);
        when(repository.findByEstadoAndFechaBefore(eq(BackupEstado.EN_PROGRESO), any()))
                .thenReturn(List.of(interrumpido));

        int corregidos = backupService.limpiarEjecucionesPendientes();

        assertEquals(1, corregidos);
        assertEquals(BackupEstado.FALLIDO, interrumpido.getEstado());
        assertNotNull(interrumpido.getMensajeError());
    }

    @Test
    void dashboard_debeConsolidarTotalesPorOrigen() {
        BackupHistory ultimo = registroExistente(BackupEstado.EXITOSO, BackupTipo.AUTOMATICO);
        ultimo.setFecha(LocalDateTime.now().minusHours(2));
        when(repository.findFirstByEstadoOrderByFechaDesc(BackupEstado.EXITOSO)).thenReturn(Optional.of(ultimo));
        when(repository.findFirstByEstadoOrderByFechaDesc(BackupEstado.EN_PROGRESO)).thenReturn(Optional.empty());
        when(repository.countByTipoAndEstado(BackupTipo.AUTOMATICO, BackupEstado.EXITOSO)).thenReturn(28L);
        when(repository.countByTipoAndEstado(BackupTipo.MANUAL, BackupEstado.EXITOSO)).thenReturn(5L);
        when(repository.countByEstado(BackupEstado.FALLIDO)).thenReturn(1L);
        when(repository.sumTamanoBytes(BackupTipo.AUTOMATICO, BackupEstado.EXITOSO)).thenReturn(3_000L);
        when(repository.sumTamanoBytes(BackupTipo.MANUAL, BackupEstado.EXITOSO)).thenReturn(500L);
        properties.getSchedule().setEnabled(false);

        var dashboard = backupService.dashboard();

        assertEquals(33, dashboard.totalCopias());
        assertEquals(28, dashboard.totalAutomaticos());
        assertEquals(5, dashboard.totalManuales());
        assertEquals(1, dashboard.totalFallidas());
        assertEquals(3500, dashboard.tamanoTotalBytes());
        assertEquals(ultimo.getArchivoNombre(), dashboard.ultimoExitosoArchivo());
        assertFalse(dashboard.ejecucionEnCurso());
        assertNull(dashboard.proximaEjecucion(), "sin schedule no hay próxima ejecución");
    }

    @Test
    void dashboard_conScheduleActivo_debeCalcularLaProximaEjecucion() {
        when(repository.findFirstByEstadoOrderByFechaDesc(any(BackupEstado.class))).thenReturn(Optional.empty());
        when(repository.countByTipoAndEstado(any(), any())).thenReturn(0L);
        when(repository.countByEstado(any())).thenReturn(0L);
        when(repository.sumTamanoBytes(any(), any())).thenReturn(0L);
        properties.getSchedule().setEnabled(true);
        properties.getSchedule().setCron("0 0 2 * * *");
        properties.getSchedule().setZone("America/Bogota");

        var dashboard = backupService.dashboard();

        assertNotNull(dashboard.proximaEjecucion());
        assertNull(dashboard.ultimoExitoso());
    }

    // ============================================================
    // NOMBRES DE ARCHIVO
    // ============================================================

    @Test
    void generarNombreArchivo_debeDiferenciarAutomaticosYManuales() {
        String automatico = backupService.generarNombreArchivo(BackupTipo.AUTOMATICO);
        String manual = backupService.generarNombreArchivo(BackupTipo.MANUAL);

        assertTrue(automatico.startsWith("backup-2"), automatico);
        assertTrue(manual.startsWith("backup-manual-2"), manual);
        assertTrue(automatico.endsWith(".sql.gz"));
        assertTrue(manual.endsWith(".sql.gz"));
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private BackupHistory registroExistente(BackupEstado estado, BackupTipo tipo) {
        BackupHistory registro = new BackupHistory();
        registro.setId(1L);
        registro.setFecha(LocalDateTime.now());
        registro.setEstado(estado);
        registro.setTipo(tipo);
        registro.setArchivoNombre(tipo == BackupTipo.MANUAL ? "backup-manual.sql.gz" : "backup.sql.gz");
        registro.setStoragePath((tipo == BackupTipo.MANUAL ? "manual/" : "automatic/") + registro.getArchivoNombre());
        registro.setProveedor(ProveedorStorage.LOCAL);
        registro.setTamanoBytes(1024L);
        return registro;
    }

    private BackupHistory registroAntiguo() {
        BackupHistory registro = registroExistente(BackupEstado.EXITOSO, BackupTipo.AUTOMATICO);
        registro.setId(77L);
        registro.setFecha(LocalDateTime.now().minusDays(400));
        return registro;
    }

    private BackupResponse respuestaDe(BackupHistory registro) {
        return new BackupResponse(registro.getId(), registro.getFecha(), registro.getFechaFin(),
                registro.getTipo(), registro.getEstado(), registro.getArchivoNombre(),
                registro.getTamanoBytes(), registro.getDuracionMs(), registro.getUsuario(),
                registro.getChecksumSha256(), registro.getProveedor());
    }
}
