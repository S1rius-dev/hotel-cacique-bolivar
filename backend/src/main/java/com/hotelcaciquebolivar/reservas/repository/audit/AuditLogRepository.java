package com.hotelcaciquebolivar.reservas.repository.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditLog;

/**
 * Repositorio para la gestión de registros de auditoría del sistema.
 * Permite consultar y filtrar logs de auditoría por entidad, usuario, rango de fechas y tipo de acción.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Obtiene registros de auditoría de una entidad específica, ordenados por fecha descendente.
     *
     * @param entity nombre de la entidad a consultar
     * @param pageable paginación de resultados
     * @return página de registros de auditoría de la entidad
     */
    Page<AuditLog> findByEntityOrderByFechaDesc(String entity, Pageable pageable);

    /**
     * Obtiene registros de auditoría de una entidad y ID específico, ordenados por fecha descendente.
     *
     * @param entity nombre de la entidad a consultar
     * @param entityId identificador de la entidad
     * @param pageable paginación de resultados
     * @return página de registros de auditoría de la entidad con el ID especificado
     */
    Page<AuditLog> findByEntityAndEntityIdOrderByFechaDesc(String entity, Long entityId, Pageable pageable);

    /**
     * Obtiene registros de auditoría de un usuario específico por su email, ordenados por fecha descendente.
     *
     * @param usuarioEmail email del usuario a consultar
     * @param pageable paginación de resultados
     * @return página de registros de auditoría del usuario
     */
    Page<AuditLog> findByUsuarioEmailOrderByFechaDesc(String usuarioEmail, Pageable pageable);

    /**
     * Obtiene registros de auditoría dentro de un rango de fechas específico, ordenados por fecha descendente.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @param pageable paginación de resultados
     * @return página de registros de auditoría en el rango de fechas
     */
    Page<AuditLog> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    /**
     * Obtiene los 50 registros de auditoría más recientes, ordenados por fecha descendente.
     *
     * @return lista de los 50 registros de auditoría más recientes
     */
    List<AuditLog> findTop50ByOrderByFechaDesc();

    /**
     * Obtiene registros de auditoría por tipo de acción y entidad, ordenados por fecha descendente.
     *
     * @param action tipo de acción de auditoría (CREAR, ACTUALIZAR, ELIMINAR, etc.)
     * @param entity nombre de la entidad a consultar
     * @param pageable paginación de resultados
     * @return lista de registros de auditoría que coinciden con la acción y entidad
     */
    List<AuditLog> findByActionAndEntityOrderByFechaDesc(AuditAction action, String entity, Pageable pageable);
}
