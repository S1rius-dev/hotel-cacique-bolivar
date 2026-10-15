package com.hotelcaciquebolivar.reservas.repository.pago;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoDesglose;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;

/**
 * Repositorio para las líneas de desglose de los pagos.
 * Permite consultar el reparto de un pago y calcular lo ya cobrado por origen
 * en una reserva, que es la base para no reasignar dinero en pagos sucesivos.
 */
public interface PagoDesgloseRepository extends JpaRepository<PagoDesglose, Long> {

    /**
     * Obtiene las líneas de desglose de un pago, ordenadas por origen ascendente.
     *
     * @param pagoId identificador del pago
     * @return lista de líneas del pago
     */
    List<PagoDesglose> findByPagoIdOrderByOrigenAsc(Long pagoId);

    /**
     * Calcula el monto total que una reserva ya tiene asignado a una caja de origen.
     * Solo cuenta pagos completados: un pago reembolsado o cancelado no libera caja,
     * porque su movimiento de caja sigue registrado como tal.
     *
     * @param reservaId identificador de la reserva
     * @param origen caja de origen a consultar
     * @return suma de montos asignados a ese origen, o 0 si no hay líneas
     */
    @Query("""
            SELECT COALESCE(SUM(d.monto), 0) FROM PagoDesglose d
            WHERE d.pago.reserva.id = :reservaId
              AND d.origen = :origen
              AND d.pago.estado = :estado
            """)
    BigDecimal sumByReservaIdAndOrigenAndPagoEstado(
            @Param("reservaId") Long reservaId,
            @Param("origen") OrigenCobro origen,
            @Param("estado") EstadoPago estado);

    /**
     * Calcula el monto ya atribuido a un consumo concreto.
     * Permite repartir el cobro entre consumos sin reasignar uno ya pagado,
     * cuando la reserva se cobra en varias cuotas.
     *
     * @param consumoId identificador del consumo
     * @param estado estado del pago que cuenta como cobrado
     * @return suma de montos atribuidos al consumo, o 0 si no hay atribución
     */
    @Query("""
            SELECT COALESCE(SUM(d.monto), 0) FROM PagoDesglose d
            WHERE d.consumo.id = :consumoId
              AND d.pago.estado = :estado
            """)
    BigDecimal sumByConsumoIdAndPagoEstado(@Param("consumoId") Long consumoId, @Param("estado") EstadoPago estado);
}
