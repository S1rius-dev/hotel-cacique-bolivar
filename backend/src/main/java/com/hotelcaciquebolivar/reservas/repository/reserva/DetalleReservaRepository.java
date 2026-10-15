package com.hotelcaciquebolivar.reservas.repository.reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;

/**
 * Repositorio para la gestión de detalles de reserva.
 * Permite consultar detalles por habitación, fecha y estado, incluyendo información
 * de huéspedes, reserva asociada y detección de conflictos de fechas.
 */
public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Long> {

    /**
     * Busca un detalle de reserva por ID con sus huéspedes cargados (LEFT JOIN FETCH).
     *
     * @param id identificador del detalle de reserva
     * @return Optional con el detalle de reserva y sus huéspedes o vacío si no existe
     */
    @Query("SELECT d FROM DetalleReserva d LEFT JOIN FETCH d.huespedes WHERE d.id = :id")
    Optional<DetalleReserva> findByIdWithHuespedes(@Param("id") Long id);

    /**
     * Obtiene los detalles de reserva activos de una habitación en una fecha específica.
     * Excluye reservas canceladas y no show.
     *
     * @param habitacionId identificador de la habitación
     * @param fecha fecha a consultar
     * @return lista de detalles de reserva activos en la habitación y fecha
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            LEFT JOIN FETCH dr.huespedes h
            LEFT JOIN FETCH h.cliente c
            WHERE dr.habitacion.id = :habitacionId
            AND r.estado NOT IN ('CANCELADA', 'NO_SHOW')
            AND dr.estado NOT IN ('CANCELADA')
            AND r.fechaCheckIn <= :fecha
            AND r.fechaCheckOut >= :fecha
            ORDER BY r.fechaCheckIn ASC
            """)
    List<DetalleReserva> findActivoPorHabitacionYFecha(
            @Param("habitacionId") Long habitacionId,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Obtiene todos los detalles de reserva activos en una fecha específica.
     * Excluye reservas canceladas y no show, ordenados por habitación y fecha de check-in.
     *
     * @param fecha fecha a consultar
     * @return lista de todos los detalles de reserva activos en la fecha
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            LEFT JOIN FETCH dr.huespedes h
            LEFT JOIN FETCH h.cliente c
            WHERE r.estado NOT IN ('CANCELADA', 'NO_SHOW')
            AND dr.estado NOT IN ('CANCELADA')
            AND r.fechaCheckIn <= :fecha
            AND r.fechaCheckOut >= :fecha
            ORDER BY dr.habitacion.id, r.fechaCheckIn ASC
            """)
    List<DetalleReserva> findAllActivosEnFecha(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene los detalles de reserva activos de una habitación que se superponen con un rango de fechas.
     * Excluye reservas canceladas y no show.
     *
     * @param habitacionId identificador de la habitación
     * @param checkIn fecha de check-in del rango
     * @param checkOut fecha de check-out del rango
     * @return lista de detalles de reserva que se superponen con el rango
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            LEFT JOIN FETCH dr.huespedes h
            LEFT JOIN FETCH h.cliente c
            WHERE dr.habitacion.id = :habitacionId
            AND r.estado NOT IN ('CANCELADA', 'NO_SHOW')
            AND dr.estado NOT IN ('CANCELADA')
            AND r.fechaCheckIn < :checkOut AND r.fechaCheckOut > :checkIn
            """)
    List<DetalleReserva> findActivoPorHabitacionEntreFechas(
            @Param("habitacionId") Long habitacionId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Obtiene los detalles de reserva no liberados de una habitación que se superponen con un rango de fechas.
     * Solo incluye detalles en estado RESERVADA u OCUPADA.
     *
     * @param habitacionId identificador de la habitación
     * @param checkIn fecha de check-in del rango
     * @param checkOut fecha de check-out del rango
     * @return lista de detalles de reserva no liberados que se superponen con el rango
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            LEFT JOIN FETCH dr.huespedes h
            LEFT JOIN FETCH h.cliente c
            WHERE dr.habitacion.id = :habitacionId
            AND r.estado NOT IN ('CANCELADA', 'NO_SHOW')
            AND dr.estado IN ('RESERVADA', 'OCUPADA')
            AND r.fechaCheckIn < :checkOut AND r.fechaCheckOut > :checkIn
            """)
    List<DetalleReserva> findActivoNoLiberadoPorHabitacionEntreFechas(
            @Param("habitacionId") Long habitacionId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Busca conflictos de extensión de reserva para una habitación específica.
     * Verifica si existe otra reserva activa que se superponga con una extensión de check-out.
     *
     * @param habitacionId identificador de la habitación
     * @param excluirDetalleId identificador del detalle a excluir de la búsqueda
     * @param checkOutActual fecha de check-out actual
     * @param nuevoCheckOut nueva fecha de check-out propuesta
     * @return Optional con el detalle de reserva en conflicto o vacío si no hay conflicto
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            WHERE dr.habitacion.id = :habitacionId
            AND dr.id != :excluirDetalleId
            AND r.estado NOT IN ('CANCELADA', 'NO_SHOW', 'FINALIZADA')
            AND dr.estado NOT IN ('CANCELADA', 'LIBERADA')
            AND r.fechaCheckIn < :nuevoCheckOut AND r.fechaCheckOut >= :checkOutActual
            """)
    Optional<DetalleReserva> findConflictoExtension(
            @Param("habitacionId") Long habitacionId,
            @Param("excluirDetalleId") Long excluirDetalleId,
            @Param("checkOutActual") LocalDate checkOutActual,
            @Param("nuevoCheckOut") LocalDate nuevoCheckOut
    );

    /**
     * Obtiene los detalles de reserva vencidos que están en estado RESERVADA pero cuya fecha de check-out ya pasó.
     *
     * @param fecha fecha de corte para considerar reservas vencidas
     * @return lista de detalles de reserva vencidos
     */
    @Query("""
            SELECT dr FROM DetalleReserva dr
            JOIN FETCH dr.reserva r
            WHERE dr.estado = 'RESERVADA'
            AND r.estado IN ('PENDIENTE', 'CONFIRMADA')
            AND r.fechaCheckOut < :fecha
            """)
    List<DetalleReserva> findReservadasVencidas(@Param("fecha") LocalDate fecha);

    /**
     * Cuenta el número de habitaciones ocupadas en una fecha específica.
     * Incluye habitaciones que han hecho check-in pero no han hecho check-out,
     * así como las que hacen check-out el mismo día y están ocupadas.
     *
     * @param fecha fecha a consultar
     * @return cantidad de habitaciones ocupadas
     */
    @Query("SELECT COUNT(DISTINCT dr.habitacion.id) FROM DetalleReserva dr JOIN dr.reserva r WHERE dr.estado IN ('RESERVADA', 'OCUPADA') AND r.estado NOT IN ('CANCELADA', 'NO_SHOW') AND r.fechaCheckIn <= :fecha AND (r.fechaCheckOut > :fecha OR (r.fechaCheckOut = :fecha AND dr.estado = 'OCUPADA'))")
    long countOcupadasEnFecha(@Param("fecha") LocalDate fecha);

    /**
     * Cuenta el número de habitaciones en estado OCUPADA en una fecha específica.
     * Solo cuenta habitaciones con estado OCUPADA y reserva no finalizada.
     *
     * @param fecha fecha a consultar
     * @return cantidad de habitaciones en estado OCUPADA
     */
    @Query("SELECT COUNT(DISTINCT dr.habitacion.id) FROM DetalleReserva dr JOIN dr.reserva r WHERE dr.estado = 'OCUPADA' AND r.estado NOT IN ('CANCELADA', 'NO_SHOW', 'FINALIZADA') AND r.fechaCheckIn <= :fecha AND r.fechaCheckOut >= :fecha")
    long countHabitacionesOcupadasEnFecha(@Param("fecha") LocalDate fecha);

    /**
     * Cuenta el número de habitaciones con reservas no canceladas en una fecha específica.
     * Incluye todas las reservas que no han sido canceladas ni marcadas como no show.
     *
     * @param fecha fecha a consultar
     * @return cantidad de habitaciones reservadas
     */
    @Query("SELECT COUNT(DISTINCT dr.habitacion.id) FROM DetalleReserva dr JOIN dr.reserva r WHERE dr.estado NOT IN ('CANCELADA') AND r.estado NOT IN ('CANCELADA', 'NO_SHOW') AND r.fechaCheckIn <= :fecha AND r.fechaCheckOut > :fecha")
    long countOcupadasHistoricasEnFecha(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene la fecha de última reserva para cada habitación en una lista de IDs.
     * Retorna un array de objetos donde cada posición contiene: [0] habitacion.id, [1] fecha de última reserva.
     *
     * @param ids lista de identificadores de habitaciones
     * @return lista de arrays con el último uso por habitación
     */
    @Query("""
            SELECT dr.habitacion.id, MAX(dr.reserva.fechaCreacion)
            FROM DetalleReserva dr
            WHERE dr.habitacion.id IN :ids
            GROUP BY dr.habitacion.id
            """)
    List<Object[]> findUltimoUsoPorHabitaciones(@Param("ids") List<Long> ids);
}
