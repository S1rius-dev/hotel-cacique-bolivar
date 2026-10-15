package com.hotelcaciquebolivar.reservas.repository.reserva;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

import jakarta.persistence.LockModeType;

/**
 * Repositorio para la gestión de reservas del hotel.
 * Permite consultar reservas por estado, titular, fechas y rangos de fechas,
 * así como buscar reservas vencidas, expiradas y activas con bloqueo pesimista.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    /**
     * Cuenta el número de reservas en un estado específico.
     *
     * @param estado estado de la reserva (PENDIENTE, CONFIRMADA, EN_CURSO, etc.)
     * @return cantidad de reservas en el estado especificado
     */
    long countByEstado(EstadoReserva estado);

    /**
     * Cuenta el número de reservas en una lista de estados específicos.
     *
     * @param estados lista de estados de reserva a contar
     * @return cantidad de reservas que coinciden con los estados
     */
    long countByEstadoIn(List<EstadoReserva> estados);

    /**
     * Obtiene las reservas más recientes con el titular cargado, ordenadas por fecha de creación descendente.
     *
     * @param pageable paginación de resultados
     * @return lista de reservas más recientes
     */
    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.titularReserva
            ORDER BY r.fechaCreacion DESC
            """)
    List<Reserva> findTopNByOrderByFechaCreacionDesc(Pageable pageable);

    /**
     * Busca una reserva por ID con bloqueo pesimista para escritura.
     * Utilizado para operaciones que requieren consistencia concurrente.
     *
     * @param id identificador de la reserva
     * @return Optional con la reserva bloqueada o vacío si no existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r JOIN FETCH r.titularReserva WHERE r.id = :id")
    Optional<Reserva> findByIdWithLock(@Param("id") Long id);

    /**
     * Obtiene las reservas en estado PENDIENTE o CONFIRMADA cuya fecha de check-in ya venció.
     * Utilizada para tareas programadas de actualización de estados.
     *
     * @param fecha fecha de corte para considerar check-in vencido
     * @return lista de reservas con check-in vencido
     */
    @Query("""
            SELECT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion
            WHERE r.estado IN ('PENDIENTE', 'CONFIRMADA')
            AND r.fechaCheckIn < :fecha
            ORDER BY r.fechaCheckIn ASC
            """)
    List<Reserva> findPendienteConfirmadaConCheckInVencido(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene las reservas en estado EN_CURSO cuya fecha de check-out ya venció.
     * Utilizada para tareas programadas de finalización automática.
     *
     * @param fecha fecha de corte para considerar check-out vencido
     * @return lista de reservas con check-out vencido
     */
    @Query("""
            SELECT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion
            WHERE r.estado = 'EN_CURSO'
            AND r.fechaCheckOut < :fecha
            ORDER BY r.fechaCheckOut ASC
            """)
    List<Reserva> findEnCursoConCheckOutVencido(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene las reservas web en estado PENDIENTE que han expirado según un umbral de tiempo.
     * Utilizada para cancelar automáticamente reservas no confirmadas.
     *
     * @param threshold fecha y hora límite para considerar la reserva expirada
     * @return lista de reservas web pendientes expiradas
     */
    @Query("""
            SELECT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion
            WHERE r.estado = 'PENDIENTE'
            AND r.origen = 'WEB'
            AND r.fechaCreacion < :threshold
            """)
    List<Reserva> findPendientesWebExpiradas(@Param("threshold") java.time.LocalDateTime threshold);

    /**
     * Obtiene las reservas en estado PENDIENTE_PAGO cuya fecha de expiración de pago ya venció.
     * Utilizada para cancelar automáticamente reservas con pago expirado.
     *
     * @param now fecha y hora actual para comparar con la expiración
     * @return lista de reservas con pago expirado
     */
    @Query("""
            SELECT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion
            WHERE r.estado = 'PENDIENTE_PAGO'
            AND r.fechaExpiracionPago IS NOT NULL
            AND r.fechaExpiracionPago <= :now
            """)
    List<Reserva> findPendientePagoExpiradas(@Param("now") java.time.LocalDateTime now);

    /**
     * Obtiene las reservas activas de un titular específico con paginación.
     * Solo retorna reservas en estados activos (PENDIENTE, PENDIENTE_PAGO, CONFIRMADA, EN_CURSO)
     * cuya fecha de check-out es igual o posterior a la fecha actual.
     *
     * @param titularId identificador del titular de la reserva
     * @param estados colección de estados de reserva a filtrar
     * @param today fecha actual para filtrar reservas vigentes
     * @return lista de reservas activas del titular
     */
    // === Mis Reservas: solo activas/presentes (PENDIENTE/PENDIENTE_PAGO/CONFIRMADA/EN_CURSO + checkOut >= today) ===
    @Query("""
            SELECT DISTINCT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion h
            LEFT JOIN FETCH h.tipo
            WHERE r.titularReserva.id = :titularId
            AND r.estado IN :estados
            AND r.fechaCheckOut >= :today
            ORDER BY r.fechaCheckIn ASC
            """)
    List<Reserva> findActivasByTitular(
            @Param("titularId") Long titularId,
            @Param("estados") Collection<EstadoReserva> estados,
            @Param("today") LocalDate today);

    /**
     * Obtiene las reservas activas de un titular específico con paginación.
     * Solo retorna reservas en estados activos (PENDIENTE, PENDIENTE_PAGO, CONFIRMADA, EN_CURSO)
     * cuya fecha de check-out es igual o posterior a la fecha actual.
     *
     * @param titularId identificador del titular de la reserva
     * @param estados colección de estados de reserva a filtrar
     * @param today fecha actual para filtrar reservas vigentes
     * @param pageable paginación de resultados
     * @return página de reservas activas del titular
     */
    @Query("""
            SELECT DISTINCT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion h
            LEFT JOIN FETCH h.tipo
            WHERE r.titularReserva.id = :titularId
            AND r.estado IN :estados
            AND r.fechaCheckOut >= :today
            ORDER BY r.fechaCheckIn ASC
            """)
    Page<Reserva> findActivasByTitular(
            @Param("titularId") Long titularId,
            @Param("estados") Collection<EstadoReserva> estados,
            @Param("today") LocalDate today,
            Pageable pageable);

    /**
     * Busca una reserva por ID y titular de reserva específico.
     * Permite verificar que una reserva pertenece a un titular determinado.
     *
     * @param id identificador de la reserva
     * @param titularId identificador del titular de la reserva
     * @return Optional con la reserva encontrada o vacío si no existe o no pertenece al titular
     */
    @Query("""
            SELECT r FROM Reserva r
            LEFT JOIN FETCH r.detalles d
            LEFT JOIN FETCH d.habitacion h
            LEFT JOIN FETCH h.tipo
            WHERE r.id = :id AND r.titularReserva.id = :titularId
            """)
    Optional<Reserva> findByIdAndTitularReservaId(
            @Param("id") Long id,
            @Param("titularId") Long titularId);
}
