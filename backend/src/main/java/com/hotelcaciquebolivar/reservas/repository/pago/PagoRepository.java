package com.hotelcaciquebolivar.reservas.repository.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;

/**
 * Repositorio para la gestión de pagos del sistema de reservas.
 * Permite consultar pagos por reserva, estado y rango de fechas,
 * así como calcular sumas de montos agrupados por hora y día.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Obtiene los pagos de una reserva específica, ordenados por fecha de pago descendente.
     *
     * @param reservaId identificador de la reserva
     * @return lista de pagos de la reserva
     */
    List<Pago> findByReservaIdOrderByFechaPagoDesc(Long reservaId);

    /**
     * Obtiene los pagos de una reserva específica y estado, ordenados por fecha de pago descendente.
     *
     * @param reservaId identificador de la reserva
     * @param estado estado del pago (PENDIENTE, COMPLETADO, FALLIDO, etc.)
     * @return lista de pagos que coinciden con la reserva y estado
     */
    List<Pago> findByReservaIdAndEstadoOrderByFechaPagoDesc(Long reservaId, EstadoPago estado);

    /**
     * Calcula la suma de montos de pagos completados en un rango de fechas específico.
     *
     * @param inicio fecha de inicio del rango
     * @param fin fecha de fin del rango
     * @return suma de montos, o 0 si no hay pagos completados
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'COMPLETADO' AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumByFechaPagoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Calcula la suma de montos de pagos completados en efectivo en un rango de fechas específico.
     *
     * @param inicio fecha de inicio del rango
     * @param fin fecha de fin del rango
     * @return suma de montos en efectivo, o 0 si no hay pagos
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'COMPLETADO' AND p.metodoPago = 'EFECTIVO' AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumEfectivoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Calcula la suma de montos de pagos completados agrupados por hora del día.
     * Retorna un array de objetos donde cada posición contiene: [0] hora (0-23), [1] suma de montos.
     *
     * @param inicio fecha y hora de inicio del rango
     * @param fin fecha y hora de fin del rango
     * @return lista de arrays con los totales agrupados por hora
     */
    @Query("SELECT FUNCTION('HOUR', p.fechaPago), COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'COMPLETADO' AND p.fechaPago >= :inicio AND p.fechaPago < :fin GROUP BY FUNCTION('HOUR', p.fechaPago) ORDER BY FUNCTION('HOUR', p.fechaPago)")
    List<Object[]> sumGroupedByHour(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Calcula la suma de montos de pagos completados agrupados por día.
     * Retorna un array de objetos donde cada posición contiene: [0] fecha del día, [1] suma de montos.
     *
     * @param inicio fecha y hora de inicio del rango
     * @param fin fecha y hora de fin del rango
     * @return lista de arrays con los totales agrupados por día
     */
    @Query("SELECT CAST(p.fechaPago AS date), COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'COMPLETADO' AND p.fechaPago >= :inicio AND p.fechaPago < :fin GROUP BY CAST(p.fechaPago AS date) ORDER BY CAST(p.fechaPago AS date)")
    List<Object[]> sumGroupedByDay(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
