package com.hotelcaciquebolivar.reservas.repository.producto;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.producto.Producto;

import jakarta.persistence.LockModeType;

/**
 * Repositorio para la gestión de productos del hotel.
 * Permite buscar productos por código de barras, SKU, categoría y nivel de stock.
 * Incluye consultas con bloqueo pesimista y filtrado avanzado.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    /**
     * Busca un producto por su código de barras.
     *
     * @param codigoBarra código de barras del producto
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> findByCodigoBarra(String codigoBarra);

    /**
     * Busca un producto activo por su SKU/código de barras.
     *
     * @param skuBarcode SKU o código de barras del producto
     * @return Optional con el producto activo encontrado o vacío si no existe
     */
    Optional<Producto> findBySkuBarcodeAndActivoTrue(String skuBarcode);

    /**
     * Busca un producto por su SKU/código de barras sin filtro de actividad.
     *
     * @param skuBarcode SKU o código de barras del producto
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> findBySkuBarcode(String skuBarcode);

    /**
     * Busca un producto por ID con bloqueo pesimista para escritura.
     * Utilizado para operaciones de actualización de stock concurrentes.
     *
     * @param id identificador del producto
     * @return Optional con el producto bloqueado o vacío si no existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdWithLock(@Param("id") Long id);

    /**
     * Obtiene todos los productos activos cuyo stock actual es igual o menor al stock mínimo.
     * Utilizado para generar alertas de reposición.
     *
     * @return lista de productos con stock bajo o agotado
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    java.util.List<Producto> findByStockActualLessThanEqualStockMinimo();

    /**
     * Busca productos activos con filtros combinados de texto, categoría y nivel de stock.
     * Soporta búsqueda por nombre o SKU, filtrado por categoría y estado de stock
     * (AGOTADO, BAJO, OK).
     *
     * @param search término de búsqueda para nombre o SKU
     * @param categoriaId identificador de la categoría (null para todas)
     * @param stock nivel de stock a filtrar: AGOTADO, BAJO, OK (null para todos)
     * @param pageable paginación de resultados
     * @return página de productos que coinciden con los filtros
     */
    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.categoria c
            WHERE p.activo = true
            AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.skuBarcode) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:categoriaId IS NULL OR c.id = :categoriaId)
            AND (
                :stock IS NULL
                OR (:stock = 'AGOTADO' AND p.stockActual = 0)
                OR (:stock = 'BAJO' AND p.stockActual > 0 AND p.stockActual <= p.stockMinimo)
                OR (:stock = 'OK' AND p.stockActual > p.stockMinimo)
            )
            """)
    Page<Producto> buscarFiltrado(
            @Param("search") String search,
            @Param("categoriaId") Long categoriaId,
            @Param("stock") String stock,
            Pageable pageable);

    /**
     * Verifica si existe un producto con el SKU/código de barras especificado (case-insensitive).
     *
     * @param skuBarcode SKU o código de barras a verificar
     * @return true si existe un producto con ese SKU, false en caso contrario
     */
    boolean existsBySkuBarcodeIgnoreCase(String skuBarcode);
    /**
     * Verifica si existe un producto con el SKU/código de barras especificado (case-insensitive),
     * excluyendo un producto específico por ID.
     *
     * @param skuBarcode SKU o código de barras a verificar
     * @param id identificador del producto a excluir
     * @return true si existe otro producto con ese SKU, false en caso contrario
     */
    boolean existsBySkuBarcodeIgnoreCaseAndIdNot(String skuBarcode, Long id);
}
