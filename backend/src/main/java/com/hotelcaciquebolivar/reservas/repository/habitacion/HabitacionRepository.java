package com.hotelcaciquebolivar.reservas.repository.habitacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;

import jakarta.persistence.LockModeType;

/**
 * Repositorio para la gestión de habitaciones del hotel.
 * Permite buscar habitaciones por número, estado, tipo y disponibilidad en rangos de fechas.
 * Incluye consultas con bloqueo pesimista para garantizar consistencia en reservas concurrentes.
 */
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    /**
     * Busca una habitación por su número.
     *
     * @param numero número de la habitación
     * @return Optional con la habitación encontrada o vacío si no existe
     */
    Optional<Habitacion> findByNumero(String numero);

    /**
     * Cuenta el número de habitaciones en un estado específico.
     *
     * @param estado estado de la habitación (DISPONIBLE, OCUPADA, MANTENIMIENTO, etc.)
     * @return cantidad de habitaciones en el estado especificado
     */
    long countByEstado(EstadoHabitacion estado);

    /**
     * Obtiene todas las habitaciones disponibles para un rango de fechas específico.
     * Excluye habitaciones en mantenimiento, inhabilitadas y aquellas con reservas activas
     * que se superpongan con el rango de fechas indicado.
     *
     * @param checkIn fecha de check-in deseada
     * @param checkOut fecha de check-out deseada
     * @return lista de habitaciones disponibles
     */
    @Query("""
            SELECT h FROM Habitacion h JOIN FETCH h.tipo WHERE h.estado NOT IN ('MANTENIMIENTO', 'INHABILITADA')
            AND h.id NOT IN (
                SELECT dr.habitacion.id FROM DetalleReserva dr
                JOIN dr.reserva r
                WHERE r.estado NOT IN ('CANCELADA', 'NO_SHOW', 'FINALIZADA')
                AND dr.estado NOT IN ('CANCELADA', 'LIBERADA')
                AND r.fechaCheckIn < :checkOut AND (r.fechaCheckOut > :checkIn OR (r.fechaCheckIn = r.fechaCheckOut AND r.fechaCheckIn >= :checkIn AND r.fechaCheckIn < :checkOut))
            )
            """)
    List<Habitacion> findAllDisponibles(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Busca una habitación disponible por ID con bloqueo pesimista para escritura.
     * Utilizado para garantizar consistencia en reservas concurrentes.
     *
     * @param id identificador de la habitación
     * @param checkIn fecha de check-in deseada
     * @param checkOut fecha de check-out deseada
     * @return Optional con la habitación disponible bloqueada o vacío si no está disponible
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT h FROM Habitacion h JOIN FETCH h.tipo WHERE h.id = :id AND h.estado NOT IN ('MANTENIMIENTO', 'INHABILITADA')
            AND h.id NOT IN (
                SELECT dr.habitacion.id FROM DetalleReserva dr
                JOIN dr.reserva r
                WHERE r.estado NOT IN ('CANCELADA', 'NO_SHOW', 'FINALIZADA')
                AND dr.estado NOT IN ('CANCELADA', 'LIBERADA')
                AND r.fechaCheckIn < :checkOut AND (r.fechaCheckOut > :checkIn OR (r.fechaCheckIn = r.fechaCheckOut AND r.fechaCheckIn >= :checkIn AND r.fechaCheckIn < :checkOut))
            )
            """)
    Optional<Habitacion> findDisponiblePorIdConLock(
            @Param("id") Long id,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Obtiene las habitaciones disponibles de un tipo específico para un rango de fechas.
     * Excluye habitaciones en mantenimiento, inhabilitadas y aquellas con reservas activas
     * que se superpongan con el rango de fechas indicado.
     *
     * @param tipoId identificador del tipo de habitación
     * @param checkIn fecha de check-in deseada
     * @param checkOut fecha de check-out deseada
     * @return lista de habitaciones disponibles del tipo especificado
     */
    @Query("""
            SELECT h FROM Habitacion h JOIN FETCH h.tipo
            WHERE h.tipo.id = :tipoId
            AND h.estado NOT IN ('MANTENIMIENTO', 'INHABILITADA')
            AND h.id NOT IN (
                SELECT dr.habitacion.id FROM DetalleReserva dr
                JOIN dr.reserva r
                WHERE r.estado NOT IN ('CANCELADA', 'NO_SHOW', 'FINALIZADA')
                AND dr.estado NOT IN ('CANCELADA', 'LIBERADA')
                AND r.fechaCheckIn < :checkOut AND (r.fechaCheckOut > :checkIn OR (r.fechaCheckIn = r.fechaCheckOut AND r.fechaCheckIn >= :checkIn AND r.fechaCheckIn < :checkOut))
            )
            """)
    List<Habitacion> findDisponiblesPorTipo(
            @Param("tipoId") Long tipoId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Cuenta el número de habitaciones activas (no en mantenimiento ni inhabilitadas) de un tipo específico.
     *
     * @param tipoId identificador del tipo de habitación
     * @return cantidad de habitaciones activas del tipo especificado
     */
    @Query("SELECT COUNT(h) FROM Habitacion h WHERE h.tipo.id = :tipoId AND h.estado NOT IN ('MANTENIMIENTO', 'INHABILITADA')")
    long countActivasPorTipo(@Param("tipoId") Long tipoId);
}
