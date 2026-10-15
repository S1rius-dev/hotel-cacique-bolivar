package com.hotelcaciquebolivar.reservas.repository.producto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.producto.MovimientoInventario;
import com.hotelcaciquebolivar.reservas.entity.producto.TipoMovimiento;

/**
 * Repositorio para la gestión de movimientos de inventario de productos.
 * Permite consultar movimientos por producto, tipo y rango de fechas,
 * así como calcular sumas de cantidades y montos vendidos.
 */
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Obtiene los movimientos de inventario de un producto específico, ordenados por fecha de creación descendente.
     *
     * @param productoId identificador del producto
     * @return lista de movimientos del producto
     */
    List<MovimientoInventario> findByProductoIdOrderByCreatedAtDesc(Long productoId);

    /**
     * Cuenta el número de movimientos de un tipo específico en un rango de fechas.
     *
     * @param tipo tipo de movimiento (ENTRADA, SALIDA, AJUSTE, etc.)
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return cantidad de movimientos del tipo especificado en el rango de fechas
     */
    long countByTipoAndCreatedAtBetween(TipoMovimiento tipo, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Calcula la suma de cantidades de movimientos de un tipo específico en un rango de fechas.
     *
     * @param tipo tipo de movimiento (ENTRADA, SALIDA, AJUSTE, etc.)
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return suma de cantidades, o null si no hay movimientos
     */
    @Query("SELECT SUM(m.cantidad) FROM MovimientoInventario m WHERE m.tipo = :tipo AND m.createdAt BETWEEN :desde AND :hasta")
    Long sumCantidadByTipoBetween(@Param("tipo") TipoMovimiento tipo, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /**
     * Calcula el monto total vendido de movimientos de un tipo específico en un rango de fechas.
     * Multiplica la cantidad de cada movimiento por el precio de venta del producto.
     *
     * @param tipo tipo de movimiento (generalmente SALIDA para ventas)
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return monto total vendido, o 0 si no hay movimientos
     */
    @Query("SELECT COALESCE(SUM(m.cantidad * p.precioVenta), 0) FROM MovimientoInventario m JOIN m.producto p WHERE m.tipo = :tipo AND m.createdAt BETWEEN :desde AND :hasta")
    java.math.BigDecimal sumMontoVendidoBetween(@Param("tipo") TipoMovimiento tipo, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
