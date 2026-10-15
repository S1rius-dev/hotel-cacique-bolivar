package com.hotelcaciquebolivar.reservas.service.producto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.producto.CategoriaDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.MovimientoInventarioDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoCreateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoResponseDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoUpdateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.TiendaMetricasDTO;

/**
 * Interfaz del servicio de gestión de productos y tienda del hotel.
 * Administra el catálogo de productos, movimientos de inventario (entradas, salidas, ventas, ajustes),
 * métricas de la tienda y categorías de productos.
 *
 * @author Hotel Cacique Bolívar
 */
public interface ProductoService {

    /**
     * Lista productos de forma paginada con filtros opcionales por búsqueda, categoría y estado de stock.
     *
     * @param search      texto de búsqueda (nombre o código); puede ser null
     * @param categoriaId identificador de la categoría para filtrar; puede ser null
     * @param stock       filtro por estado de stock ("BAJO", "OK"); puede ser null
     * @param pageable    configuración de paginación
     * @return página con los productos encontrados
     */
    Page<ProductoResponseDTO> listar(String search, Long categoriaId, String stock, Pageable pageable);

    /**
     * Obtiene un producto por su identificador único.
     *
     * @param id identificador del producto
     * @return el producto encontrado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    ProductoResponseDTO obtenerPorId(Long id);

    /**
     * Obtiene un producto activo por su código SKU/Barras.
     *
     * @param codigo código SKU o de barras del producto
     * @return el producto encontrado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    ProductoResponseDTO obtenerPorCodigo(String codigo);

    /**
     * Crea un nuevo producto en el catálogo.
     * Valida que el SKU/Barras no esté duplicado si se proporciona.
     *
     * @param dto datos del producto a crear
     * @return el producto creado
     * @throws com.hotelcaciquebolivar.reservas.exception.producto.SkuDuplicadoException       si el SKU ya existe
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si la categoría no existe
     */
    ProductoResponseDTO crear(ProductoCreateDTO dto);

    /**
     * Actualiza un producto existente.
     * Permite limpiar el SKU/Barras estableciéndolo en cadena vacía o nula.
     * Valida unicidad del SKU si se modifica.
     *
     * @param id  identificador del producto a actualizar
     * @param dto datos actualizados
     * @return el producto actualizado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     * @throws com.hotelcaciquebolivar.reservas.exception.producto.SkuDuplicadoException     si el nuevo SKU ya existe
     */
    ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto);

    /**
     * Desactiva un producto (soft delete) estableciendo su flag activo en false.
     *
     * @param id identificador del producto a desactivar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    void eliminar(Long id);

    /**
     * Registra un movimiento de inventario para un producto.
     * Calcula el stock resultante según el tipo de movimiento (ENTRADA, SALIDA, VENTA, AJUSTE).
     * Para movimientos de tipo VENTA, registra también un movimiento en la caja de la tienda.
     *
     * @param productoId   identificador del producto
     * @param req          datos del movimiento a registrar
     * @param emailUsuario email del usuario que realiza el movimiento
     * @return el movimiento de inventario registrado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException   si el producto no existe
     * @throws com.hotelcaciquebolivar.reservas.exception.producto.StockInsuficienteException si el stock resultante es negativo
     */
    MovimientoInventarioDTO registrarMovimiento(Long productoId, MovimientoInventarioDTO.MovimientoRequest req, String emailUsuario);

    /**
     * Lista el historial de movimientos de inventario de un producto, ordenados por fecha de creación descendente.
     *
     * @param productoId identificador del producto
     * @return lista de movimientos de inventario
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si el producto no existe
     */
    List<MovimientoInventarioDTO> listarMovimientos(Long productoId);

    /**
     * Calcula las métricas generales de la tienda: total de productos, valor del inventario,
     * productos con stock bajo y monto vendido en el día actual.
     *
     * @return objeto con las métricas de la tienda
     */
    TiendaMetricasDTO metricas();

    /**
     * Lista todas las categorías de productos disponibles.
     *
     * @return lista de categorías
     */
    List<CategoriaDTO> listarCategorias();
}
