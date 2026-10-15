package com.hotelcaciquebolivar.reservas.repository.habitacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.habitacion.PrecioTemporal;

/**
 * Repositorio para la gestión de precios temporales de habitaciones.
 * Permite consultar precios por tipo de habitación, fecha y rango de fechas,
 * así como verificar solapamientos entre períodos de precios.
 */
public interface PrecioTemporalRepository extends JpaRepository<PrecioTemporal, Long> {

    /**
     * Obtiene los precios temporales de un tipo de habitación específico, ordenados por fecha de inicio.
     *
     * @param tipoId identificador del tipo de habitación
     * @return lista de precios temporales del tipo de habitación
     */
    List<PrecioTemporal> findByTipoHabitacionIdOrderByFechaInicio(Long tipoId);

    /**
     * Obtiene los precios temporales activos para una fecha específica de un tipo de habitación.
     *
     * @param tipoId identificador del tipo de habitación
     * @param fecha fecha a consultar
     * @return lista de precios activos en la fecha indicada
     */
    @Query("""
            SELECT pt FROM PrecioTemporal pt
            WHERE pt.tipoHabitacion.id = :tipoId
              AND pt.fechaInicio <= :fecha
              AND pt.fechaFin >= :fecha
            """)
    List<PrecioTemporal> findActivosEnFecha(
            @Param("tipoId") Long tipoId,
            @Param("fecha") LocalDate fecha);

    /**
     * Obtiene los precios temporales activos para un rango de fechas de un tipo de habitación.
     * Retorna los precios ordenados por fecha de inicio.
     *
     * @param tipoId identificador del tipo de habitación
     * @param inicio fecha de inicio del rango
     * @param fin fecha de fin del rango
     * @return lista de precios activos en el rango de fechas
     */
    @Query("""
            SELECT pt FROM PrecioTemporal pt
            WHERE pt.tipoHabitacion.id = :tipoId
              AND pt.fechaInicio <= :fin
              AND pt.fechaFin >= :inicio
            ORDER BY pt.fechaInicio
            """)
    List<PrecioTemporal> findActivosEnRango(
            @Param("tipoId") Long tipoId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    /**
     * Busca el precio temporal más reciente de un tipo de habitación que se superponga con un rango de fechas.
     * Retorna el registro con el ID más alto que cumpla con la condición de superposición.
     *
     * @param tipoId identificador del tipo de habitación
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @return Optional con el precio temporal encontrado o vacío si no existe
     */
    Optional<PrecioTemporal> findTopByTipoHabitacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByIdDesc(
            Long tipoId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Verifica si existe un solapamiento de precios temporales para un tipo de habitación en un rango de fechas.
     * Excluye el registro con el ID especificado de la verificación.
     *
     * @param id identificador del precio temporal a excluir (para actualizaciones)
     * @param tipoId identificador del tipo de habitación
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @return true si existe un solapamiento, false en caso contrario
     */
    @Query("""
            SELECT COUNT(pt) > 0 FROM PrecioTemporal pt
            WHERE pt.id <> :id
              AND pt.tipoHabitacion.id = :tipoId
              AND pt.fechaInicio <= :fechaFin
              AND pt.fechaFin >= :fechaInicio
            """)
    boolean existeSolapamiento(
            @Param("id") Long id,
            @Param("tipoId") Long tipoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
