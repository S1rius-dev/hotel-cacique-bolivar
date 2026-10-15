package com.hotelcaciquebolivar.reservas.service.backup;

import java.io.InputStream;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.backup.BackupDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;
import com.hotelcaciquebolivar.reservas.entity.backup.ProveedorStorage;

/**
 * Servicio de gestion de copias de seguridad de la base de datos.
 *
 * <p>No expone restore a proposito: restaurar implica reemplazar la base de datos que la
 * propia aplicacion tiene abierta, con riesgo de bloqueos en metadata locks y estado JPA
 * incoherente. El restore es un procedimiento fuera de banda documentado en
 * {@code docs/backup-runbook.md} que exige la aplicacion detenida.</p>
 */
public interface BackupService {

    /**
     * Crea una copia de seguridad solicitada por un administrador.
     *
     * <p>Bloqueante: retorna cuando mysqldump termina. Inscribe el registro
     * EN_PROGRESO antes de invocar el proceso y lo actualiza al finalizar, de modo que
     * un fallo siempre queda registrado.</p>
     *
     * @param emailUsuario email del administrador que solicita la copia
     * @return el registro de la copia creada
     * @throws com.hotelcaciquebolivar.reservas.exception.backup.BackupNoDisponibleException si el sistema esta deshabilitado
     * @throws com.hotelcaciquebolivar.reservas.exception.backup.BackupEnProgresoException si ya hay una copia en curso
     * @throws com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException si mysqldump falla
     */
    BackupResponse crearManual(String emailUsuario);

    /**
     * Crea una copia de seguridad desde la tarea programada.
     *
     * <p>No propaga la excepcion: un fallo programado no debe tumbar el proceso ni
     * ensuciar los logs de arranque, queda registrado como FALLIDO y notificado a los administradores.</p>
     *
     * @return la copia creada, o {@code null} si ya habia una en curso
     */
    BackupResponse crearAutomatico();

    /**
     * Lista las copias con paginacion y filtros opcionales.
     *
     * @param pageable parametros de paginación
     * @param tipo     filtra por origen, o {@code null} para todos
     * @param estado   filtra por estado, o {@code null} para todos
     * @return pagina de copias, de la mas reciente a la mas antigua
     */
    PageResponse<BackupResponse> listar(Pageable pageable, BackupTipo tipo, BackupEstado estado);

    /**
     * Obtiene una copia por su identificador.
     *
     * @param id identificador de la copia
     * @return la copia solicitada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no existe
     */
    BackupResponse obtener(Long id);

    /**
     * Abre el archivo comprimido de una copia para descargarlo.
     *
     * <p>El llamador es responsable de cerrar el stream devuelto.</p>
     *
     * @param id identificador de la copia
     * @return el nombre del archivo y su contenido
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si el registro o el archivo no existen
     */
    Descarga abrirDescarga(Long id);

    /**
     * Elimina una copia: borra el archivo almacenado y el registro del historial.
     *
     * <p>Si el archivo ya no existe, el registro se elimina igual, para que un backup
     * cuyo archivo se perdio no ocupe la lista de forma permanente.</p>
     *
     * @param id          identificador de la copia
     * @param emailUsuario email del administrador que la elimina, o {@code null} si es la purga automatica
     * @return el nombre del archivo que fue eliminado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si el registro no existe
     */
    String eliminar(Long id, String emailUsuario);

    /**
     * Resumen del estado del sistema de copias de seguridad.
     *
     * @return el dashboard con ultimo backup, totales y proxima ejecucion
     */
    BackupDashboardResponse dashboard();

    /**
     * Elimina las copias que exceden la politica de retencion.
     *
     * <p>Un valor de retencion negativo significa que ese origen nunca se purga.
     * Solo se purgan copias ya finalizadas, nunca una que este EN_PROGRESO.</p>
     *
     * @return cantidad de copias eliminadas
     */
    int purgarAntiguos();

    /**
     * Marca como fallidas las ejecuciones que quedaron EN_PROGRESO mas alla del timeout.
     *
     * <p>Recupera el historial si la aplicacion se detuvo a mitad de un dump: sin este
     * barrido esos registros quedarían EN_PROGRESO para siempre y el panel los
     * presentaria como ejecuciones que nunca terminan.</p>
     *
     * @return cantidad de registros corregidos
     */
    int limpiarEjecucionesPendientes();

    /**
     * Nombre del archivo comprimido con marca de tiempo, para una copia del tipo indicado.
     *
     * @param tipo origen de la copia
     * @return el nombre del archivo, por ejemplo {@code backup-manual-2026-09-25T14-30-00.sql.gz}
     */
    String generarNombreArchivo(BackupTipo tipo);

    /**
     * Contenido de una copia para descarga.
     *
     * @param nombreArchivo nombre con el que se descarga, ya con extension
     * @param contenido     stream del archivo comprimido, que el llamador debe cerrar
     */
    record Descarga(String nombreArchivo, InputStream contenido) {
    }
}
