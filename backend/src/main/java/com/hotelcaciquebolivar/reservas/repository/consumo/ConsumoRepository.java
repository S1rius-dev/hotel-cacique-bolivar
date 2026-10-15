package com.hotelcaciquebolivar.reservas.repository.consumo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;
import com.hotelcaciquebolivar.reservas.entity.consumo.EstadoConsumo;

/**
 * Repositorio para la gestión de consumos de clientes.
 * Permite consultar consumos por reserva, estado y calcular totales de montos.
 */
public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    /**
     * Obtiene los consumos de una reserva específica, ordenados por fecha de creación descendente.
     *
     * @param reservaId identificador de la reserva
     * @return lista de consumos de la reserva
     */
    List<Consumo> findByReservaIdOrderByCreatedAtDesc(Long reservaId);

    /**
     * Obtiene los consumos de una reserva específica filtrados por una lista de estados.
     *
     * @param reservaId identificador de la reserva
     * @param estados lista de estados de consumo a filtrar
     * @return lista de consumos que coinciden con los estados indicados
     */
    List<Consumo> findByReservaIdAndEstadoIn(Long reservaId, List<EstadoConsumo> estados);

    /**
     * Obtiene los consumos de una reserva en orden cronológico ascendente.
     * El orden estable por id es necesario para atribuir el cobro a los consumos
     * de forma determinista cuando la reserva se paga en varias cuotas.
     *
     * @param reservaId identificador de la reserva
     * @param estados lista de estados de consumo a filtrar
     * @return lista de consumos de la reserva ordenados por id ascendente
     */
    List<Consumo> findByReservaIdAndEstadoInOrderByIdAsc(Long reservaId, List<EstadoConsumo> estados);

    /**
     * Calcula la suma de montos totales de consumos de una reserva específica por estados.
     *
     * @param reservaId identificador de la reserva
     * @param estados lista de estados de consumo a incluir en la suma
     * @return suma de montos totales, o 0 si no hay consumos
     */
    @Query("SELECT COALESCE(SUM(c.montoTotal), 0) FROM Consumo c WHERE c.reserva.id = :reservaId AND c.estado IN :estados")
    BigDecimal sumByReservaIdAndEstadoIn(@Param("reservaId") Long reservaId, @Param("estados") List<EstadoConsumo> estados);

    /**
     * Calcula la suma de montos totales de consumos de una reserva específica por un estado.
     *
     * @param reservaId identificador de la reserva
     * @param estado estado de consumo a filtrar
     * @return suma de montos totales, o 0 si no hay consumos
     */
    @Query("SELECT COALESCE(SUM(c.montoTotal), 0) FROM Consumo c WHERE c.reserva.id = :reservaId AND c.estado = :estado")
    BigDecimal sumByReservaIdAndEstado(@Param("reservaId") Long reservaId, @Param("estado") EstadoConsumo estado);

    /**
     * Verifica si existe un consumo asociado a un movimiento de inventario específico.
     *
     * @param movimientoId identificador del movimiento de inventario
     * @return true si existe un consumo asociado, false en caso contrario
     */
    boolean existsByMovimientoInventarioId(Long movimientoId);
}
