package com.hotelcaciquebolivar.reservas.repository.backup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.backup.BackupEstado;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupTipo;

/**
 * Repositorio de acceso al historial de copias de seguridad.
 * Las consultas son JPQL o derivadas, sin SQL nativo, siguiendo la convencion del proyecto.
 */
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {

    /** Lista todas las copias paginadas, de la mas reciente a la mas antigua. */
    Page<BackupHistory> findAllByOrderByFechaDesc(Pageable pageable);

    /** Lista copias filtrando opcionalmente por tipo y estado. */
    Page<BackupHistory> findByTipoAndEstadoOrderByFechaDesc(BackupTipo tipo, BackupEstado estado, Pageable pageable);

    /** Lista copias filtrando solo por tipo. */
    Page<BackupHistory> findByTipoOrderByFechaDesc(BackupTipo tipo, Pageable pageable);

    /** Lista copias filtrando solo por estado. */
    Page<BackupHistory> findByEstadoOrderByFechaDesc(BackupEstado estado, Pageable pageable);

    /** Ultima copia finalizada exitosamente. */
    Optional<BackupHistory> findFirstByEstadoOrderByFechaDesc(BackupEstado estado);

    /** Ultima copia del tipo indicado, sin importar su estado. */
    Optional<BackupHistory> findFirstByTipoOrderByFechaDesc(BackupTipo tipo);

    /** Ejecuciones que quedaron EN_PROGRESO antes del corte, para marcarlas como fallidas. */
    List<BackupHistory> findByEstadoAndFechaBefore(BackupEstado estado, LocalDateTime corte);

    /** Cuenta las copias finalizadas exitosamente. */
    long countByEstado(BackupEstado estado);

    /** Cuenta las copias de un origen que llegaron al estado indicado. */
    long countByTipoAndEstado(BackupTipo tipo, BackupEstado estado);

    /** Suma los bytes de los backups exitosos del tipo indicado, para el dashboard. */
    @Query("SELECT COALESCE(SUM(b.tamanoBytes), 0) FROM BackupHistory b WHERE b.tipo = :tipo AND b.estado = :estado")
    Long sumTamanoBytes(@Param("tipo") BackupTipo tipo, @Param("estado") BackupEstado estado);
}
