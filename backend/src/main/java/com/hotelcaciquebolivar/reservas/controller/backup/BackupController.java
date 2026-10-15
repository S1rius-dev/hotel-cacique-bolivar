package com.hotelcaciquebolivar.reservas.controller.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.hotelcaciquebolivar.reservas.dto.backup.BackupClonRequest;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupClonResponse;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupResponse;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupVerificacionResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.service.backup.BackupCloneService;
import com.hotelcaciquebolivar.reservas.service.backup.BackupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestion de copias de seguridad de la base de datos.
 *
 * <p>Expone creacion bajo demanda, consulta, descarga, verificacion de integridad y
 * eliminacion.</p>
 *
 * <p>La restauracion solo se ofrece como clon a una base nueva ({@code /clonar}). Restaurar
 * sobre la base que la aplicacion tiene abierta exige detenerla y se documenta en
 * {@code docs/backup-runbook.md}; hacerlo desde dentro dejaria a HikariCP y Hibernate
 * apuntando a un schema reemplazado por debajo.</p>
 *
 * <p>El acceso se restringe a ROLE_ADMIN en {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Tag(name = "Backups", description = "Copias de seguridad, verificacion de integridad y clon a base nueva")
public class BackupController {

    private final BackupService backupService;
    private final BackupCloneService backupCloneService;

    /**
     * Resumen del sistema de copias: ultimo backup exitoso, totales y proxima ejecucion.
     *
     * @return el dashboard de backups
     */
    @Operation(operationId = "obtenerDashboardBackups",
            summary = "Resumen del sistema de copias de seguridad",
            description = "Devuelve el ultimo backup exitoso, si hay una ejecucion en curso, "
                    + "los totales por origen y estado, el espacio ocupado y la proxima "
                    + "ejecucion programada segun el cron configurado.")
    @GetMapping("/dashboard")
    public ResponseEntity<BackupDashboardResponse> dashboard() {
        return ResponseEntity.ok(backupService.dashboard());
    }

    /**
     * Lista las copias de seguridad con paginacion y filtros opcionales.
     *
     * @param pageable parametros de paginacion (20 por defecto, por fecha descendente)
     * @param tipo     filtra por origen (AUTOMATICO o MANUAL), opcional
     * @param estado   filtra por estado (EN_PROGRESO, EXITOSO o FALLIDO), opcional
     * @return pagina de copias, de la mas reciente a la mas antigua
     */
    @Operation(operationId = "listarBackups",
            summary = "Listar copias de seguridad",
            description = "Devuelve las copias ordenadas de la mas reciente a la mas antigua. "
                    + "Los filtros por tipo y estado son opcionales y combinables.")
    @GetMapping
    public ResponseEntity<PageResponse<BackupResponse>> listar(
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) BackupTipo tipo,
            @RequestParam(required = false) BackupEstado estado) {

        return ResponseEntity.ok(backupService.listar(pageable, tipo, estado));
    }

    /**
     * Obtiene el detalle de una copia de seguridad.
     *
     * @param id identificador de la copia
     * @return la copia solicitada
     */
    @Operation(operationId = "obtenerBackup",
            summary = "Obtener el detalle de una copia de seguridad")
    @GetMapping("/{id}")
    public ResponseEntity<BackupResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(backupService.obtener(id));
    }

    /**
     * Crea una copia de seguridad bajo demanda.
     *
     * <p>La peticion es bloqueante: retorna cuando mysqldump termina. Si ya hay una copia en
     * curso responde 409, porque dos mysqldump simultaneos se pelearian el ancho de banda
     * de disco sin aportar nada.</p>
     *
     * @param authentication usuario autenticado que solicita la copia
     * @return la copia creada
     */
    @Operation(operationId = "crearBackup",
            summary = "Crear una copia de seguridad manual",
            description = "Ejecuta mysqldump de forma sincrona y registra el resultado en el "
                    + "historial. Responde 503 si el sistema esta deshabilitado por configuracion "
                    + "y 409 si ya hay una copia en ejecucion.")
    @PostMapping
    public ResponseEntity<BackupResponse> crear(Authentication authentication) {
        BackupResponse creada = backupService.crearManual(authentication.getName());
        return ResponseEntity.status(201).body(creada);
    }

    /**
     * Descarga el archivo comprimido de una copia de seguridad.
     *
     * <p>El archivo se transmite en streaming, no se carga completo en memoria: un dump de la
     * base del hotel puede pesar cientos de MB comprimidos.</p>
     *
     * @param id identificador de la copia
     * @return el archivo .sql.gz como adjunto
     */
    @Operation(operationId = "descargarBackup",
            summary = "Descargar el archivo de una copia de seguridad",
            description = "Devuelve el archivo .sql.gz en streaming. El contenido es un dump SQL "
                    + "de MySQL comprimido con gzip, listo para restaurar segun el runbook.")
    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> descargar(@PathVariable Long id) {
        BackupService.Descarga descarga = backupService.abrirDescarga(id);

        StreamingResponseBody cuerpo = (OutputStream salida) -> {
            try (InputStream origen = descarga.contenido()) {
                origen.transferTo(salida);
            } catch (IOException e) {
                // La conexion ya puede estar cerrada si el cliente cancelo la descarga;
                // no hay nada que reportar a un cliente que se fue.
                throw new IOException("No se pudo completar la descarga de " + descarga.nombreArchivo(), e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/gzip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + descarga.nombreArchivo() + "\"")
                .body(cuerpo);
    }

    /**
     * Elimina una copia de seguridad y su archivo asociado.
     *
     * @param id            identificador de la copia
     * @param authentication usuario autenticado que la elimina
     * @return nombre del archivo eliminado
     */
    @Operation(operationId = "eliminarBackup",
            summary = "Eliminar una copia de seguridad",
            description = "Borra el archivo almacenado y el registro del historial. Si el archivo "
                    + "ya no existe, el registro se elimina igualmente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        backupService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica la integridad de una copia recalculando su SHA-256.
     *
     * <p>No modifica nada. Responde 200 tanto si coincide como si no, porque un checksum
     * discrepante es un resultado valido que el operador debe ver con detalle, no un error.</p>
     *
     * @param id identificador de la copia
     * @return checksum esperado, recalculado y si la copia esta integra
     */
    @Operation(operationId = "verificarBackup",
            summary = "Verificar la integridad de una copia de seguridad",
            description = "Recalcula el SHA-256 del archivo almacenado y lo compara con el "
                    + "registrado al crear la copia. Es de solo lectura. Devuelve 200 con "
                    + "integro=false si no coinciden, para que la UI muestre el detalle.")
    @PostMapping("/{id}/verificar")
    public ResponseEntity<BackupVerificacionResponse> verificar(@PathVariable Long id) {
        return ResponseEntity.ok(backupCloneService.verificar(id, null));
    }

    /**
     * Restaura la copia en una base de datos nueva.
     *
     * <p>Nunca restaura sobre la base en uso: si el destino coincide con ella, el servicio
     * lo rechaza. Si MySQL falla a mitad, la base destino se elimina para no dejar un clon
     * incompleto que parezca restaurado.</p>
     *
     * @param id            identificador de la copia
     * @param request       nombre de la base a crear
     * @param authentication usuario autenticado que solicita el clon
     * @return la base creada
     */
    @Operation(operationId = "clonarBackup",
            summary = "Restaurar una copia en una base de datos nueva",
            description = "Verifica el checksum, crea la base destino e importa el .sql.gz. "
                    + "La base de la aplicacion no se toca nunca. Es sincrono: la peticion no "
                    + "termina hasta que mysql acaba la importacion.")
    @PostMapping("/{id}/clonar")
    public ResponseEntity<BackupClonResponse> clonar(
            @PathVariable Long id,
            @Valid @RequestBody BackupClonRequest request,
            Authentication authentication) {

        String base = backupCloneService.clonar(id, request.baseDestino(), authentication.getName());
        return ResponseEntity.status(201).body(new BackupClonResponse(id, base,
                "Copia restaurada en la base '" + base + "'. La base en uso no se modifico."));
    }
}
