package com.hotelcaciquebolivar.reservas.repository.caja;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.caja.MovimientoCaja;
import com.hotelcaciquebolivar.reservas.entity.caja.TipoMovimientoCaja;

/**
 * Repositorio para la gestión de movimientos de caja.
 * Permite consultar movimientos por turno, tipo, método de pago y rango de fechas,
 * así como calcular sumas de montos agrupadas.
 */
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    /**
     * Obtiene los movimientos de caja de un turno específico, ordenados por fecha ascendente.
     *
     * @param turnoId identificador del turno
     * @return lista de movimientos del turno ordenados cronológicamente
     */
    List<MovimientoCaja> findByTurnoIdOrderByFechaAsc(Long turnoId);

    /**
     * Obtiene los movimientos de caja de un turno específico y tipo, ordenados por fecha ascendente.
     *
     * @param turnoId identificador del turno
     * @param tipo tipo de movimiento (INGRESO o EGRESO)
     * @return lista de movimientos del turno del tipo especificado
     */
    List<MovimientoCaja> findByTurnoIdAndTipoOrderByFechaAsc(Long turnoId, TipoMovimientoCaja tipo);

    /**
     * Obtiene los movimientos de varios turnos en una sola consulta (batch).
     *
     * @param turnoIds lista de identificadores de turnos
     * @return lista de movimientos ordenados por fecha ascendente
     */
    List<MovimientoCaja> findByTurnoIdInOrderByFechaAsc(List<Long> turnoIds);

    /**
     * Calcula la suma de montos de movimientos de caja de un turno específico por tipo.
     *
     * @param turnoId identificador del turno
     * @param tipo tipo de movimiento (INGRESO o EGRESO)
     * @return suma de montos, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.turno.id = :turnoId AND m.tipo = :tipo")
    BigDecimal sumByTurnoIdAndTipo(@Param("turnoId") Long turnoId, @Param("tipo") TipoMovimientoCaja tipo);

    /**
     * Calcula la suma de montos de movimientos de caja agrupados por turno y tipo.
     * Retorna un array de objetos donde cada posición contiene:
     * [0] turno.id, [1] tipo de movimiento, [2] suma de montos.
     *
     * @param turnoIds lista de identificadores de turnos
     * @return lista de arrays con los totales agrupados por turno y tipo
     */
    @Query("""
            SELECT m.turno.id, m.tipo, COALESCE(SUM(m.monto), 0)
            FROM MovimientoCaja m
            WHERE m.turno.id IN :turnoIds
            GROUP BY m.turno.id, m.tipo
            """)
    List<Object[]> sumByTurnoIdsGrouped(@Param("turnoIds") List<Long> turnoIds);

    /**
     * Calcula la suma de montos de movimientos de caja de un turno específico por tipo y método de pago.
     *
     * @param turnoId identificador del turno
     * @param tipo tipo de movimiento (INGRESO o EGRESO)
     * @param metodo método de pago utilizado
     * @return suma de montos, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.turno.id = :turnoId AND m.tipo = :tipo AND m.metodoPago = :metodo")
    BigDecimal sumByTurnoIdAndTipoAndMetodo(@Param("turnoId") Long turnoId, @Param("tipo") TipoMovimientoCaja tipo, @Param("metodo") com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago metodo);

    /**
     * Calcula la suma de montos de movimientos de caja de una caja específica por tipo y rango de fechas.
     *
     * @param cajaId identificador de la caja
     * @param tipo tipo de movimiento (INGRESO o EGRESO)
     * @param inicio fecha de inicio del rango
     * @param fin fecha de fin del rango
     * @return suma de montos en el rango de fechas, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.turno.caja.id = :cajaId AND m.tipo = :tipo AND m.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumByCajaIdAndTipoAndFechaBetween(@Param("cajaId") Long cajaId, @Param("tipo") TipoMovimientoCaja tipo, @Param("inicio") java.time.LocalDateTime inicio, @Param("fin") java.time.LocalDateTime fin);

    /**
     * Verifica si existe un movimiento de caja asociado a un pago específico.
     *
     * @param pagoId identificador del pago
     * @return true si existe un movimiento asociado al pago, false en caso contrario
     */
    boolean existsByPagoId(Long pagoId);

    /**
     * Calcula la suma de montos de movimientos sin turno asociado, por caja y tipo.
     *
     * @param cajaId identificador de la caja
     * @param tipo tipo de movimiento (INGRESO o GASTO)
     * @return suma de montos, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.turno IS NULL AND m.caja.id = :cajaId AND m.tipo = :tipo")
    BigDecimal sumSinTurnoByCajaAndTipo(@Param("cajaId") Long cajaId, @Param("tipo") TipoMovimientoCaja tipo);

    /**
     * Calcula la suma total de montos de movimientos sin turno asociado para una caja.
     *
     * @param cajaId identificador de la caja
     * @return suma total de montos, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.turno IS NULL AND m.caja.id = :cajaId")
    BigDecimal sumSinTurnoByCaja(@Param("cajaId") Long cajaId);

    /**
     * Cuenta la cantidad de movimientos sin turno asociado para una caja.
     *
     * @param cajaId identificador de la caja
     * @return cantidad de movimientos sin turno
     */
    @Query("SELECT COUNT(m) FROM MovimientoCaja m WHERE m.turno IS NULL AND m.caja.id = :cajaId")
    long countSinTurnoByCaja(@Param("cajaId") Long cajaId);

    /**
     * Obtiene movimientos sin turno asociado para una caja, ordenados por fecha descendente.
     *
     * @param cajaId identificador de la caja
     * @return lista de movimientos sin turno
     */
    List<MovimientoCaja> findByTurnoIsNullAndCajaIdOrderByFechaDesc(Long cajaId);

    /**
     * Obtiene todos los movimientos de una caja, ordenados por fecha descendente.
     * Incluye movimientos con turno y sin turno.
     *
     * @param cajaId identificador de la caja
     * @return lista de todos los movimientos de la caja
     */
    List<MovimientoCaja> findByCajaIdOrderByFechaDesc(Long cajaId);

    /**
     * Agrupa los movimientos de varias cajas por caja y tipo dentro de un período.
     *
     * <p>Filtra por {@code caja} y no por {@code turno.caja} a propósito: los
     * movimientos registrados sin turno activo igual tienen caja asignada, y
     * excluirlos dejaría el efectivo fuera de la conciliación.</p>
     *
     * <p>Retorna un array por grupo con: [0] nombre de la caja, [1] tipo de
     * movimiento, [2] suma de montos.</p>
     *
     * @param nombresCajas nombres de las cajas a incluir
     * @param inicio fecha de inicio del rango
     * @param fin fecha de fin del rango
     * @return lista de arrays con los totales agrupados por caja y tipo
     */
    @Query("""
            SELECT m.caja.nombre, m.tipo, COALESCE(SUM(m.monto), 0)
            FROM MovimientoCaja m
            WHERE m.caja.nombre IN :nombresCajas
              AND m.fecha BETWEEN :inicio AND :fin
            GROUP BY m.caja.nombre, m.tipo
            """)
    List<Object[]> sumAgrupadoPorCajaYTipo(
            @Param("nombresCajas") List<String> nombresCajas,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fin") java.time.LocalDateTime fin);
}
