package com.hotelcaciquebolivar.reservas.controller.producto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.producto.CategoriaDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.MovimientoInventarioDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoCreateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoResponseDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoUpdateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.TiendaMetricasDTO;
import com.hotelcaciquebolivar.reservas.service.producto.ProductoService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de productos y tienda del hotel.
 * Permite CRUD de productos, búsqueda por código, gestión de inventario
 * (movimientos de entrada/salida), métricas de tienda y listado de categorías.
 */
@RestController
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista productos con filtros opcionales y paginación.
     *
     * @param search      término de búsqueda para filtrar productos (opcional)
     * @param categoriaId identificador de categoría para filtrar (opcional)
     * @param stock       filtro por estado de stock (opcional)
     * @param pageable    parámetros de paginación
     * @return página de productos que coinciden con los filtros
     */
    @Operation(operationId = "listarProductos")
    @GetMapping("/api/productos")
    public ResponseEntity<Page<ProductoResponseDTO>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String stock,
            Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(search, categoriaId, stock, pageable));
    }

    /**
     * Obtiene un producto específico por su identificador.
     *
     * @param id identificador del producto
     * @return el producto encontrado
     */
    @Operation(operationId = "obtenerProductoPorId")
    @GetMapping("/api/productos/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    /**
     * Busca un producto por su código de barras o código interno.
     *
     * @param codigo código del producto a buscar
     * @return el producto encontrado
     */
    @Operation(operationId = "buscarProductoPorCodigo")
    @GetMapping("/api/productos/buscar-por-codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.obtenerPorCodigo(codigo));
    }

    /**
     * Crea un nuevo producto en el sistema.
     *
     * @param dto datos del producto a crear, validados con @Valid
     * @return ResponseEntity con el producto creado, código 201 Created
     */
    @Operation(operationId = "crearProducto")
    @PostMapping("/api/productos")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param id  identificador del producto a actualizar
     * @param dto datos actualizados del producto, validados con @Valid
     * @return el producto actualizado
     */
    @Operation(operationId = "actualizarProducto")
    @PutMapping("/api/productos/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoUpdateDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    /**
     * Elimina un producto del sistema.
     *
     * @param id identificador del producto a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarProducto")
    @DeleteMapping("/api/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registra un movimiento de inventario (entrada o salida) para un producto.
     *
     * @param id             identificador del producto
     * @param req            datos del movimiento de inventario, validados con @Valid
     * @param authentication objeto de autenticación del usuario actual (puede ser null)
     * @return ResponseEntity con el movimiento registrado, código 201 Created
     */
    @Operation(operationId = "registrarMovimientoProducto")
    @PostMapping("/api/productos/{id}/movimiento")
    public ResponseEntity<MovimientoInventarioDTO> registrarMovimiento(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoInventarioDTO.MovimientoRequest req,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.registrarMovimiento(id, req, email));
    }

    /**
     * Lista todos los movimientos de inventario de un producto específico.
     *
     * @param id identificador del producto
     * @return lista de movimientos de inventario del producto
     */
    @Operation(operationId = "listarMovimientosProducto")
    @GetMapping("/api/productos/{id}/movimientos")
    public ResponseEntity<List<MovimientoInventarioDTO>> listarMovimientos(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.listarMovimientos(id));
    }

    /**
     * Obtiene métricas generales de la tienda.
     *
     * @return métricas de la tienda
     */
    @Operation(operationId = "metricasTienda")
    @GetMapping("/api/tienda/metricas")
    public ResponseEntity<TiendaMetricasDTO> metricas() {
        return ResponseEntity.ok(productoService.metricas());
    }

    /**
     * Lista todas las categorías de productos disponibles.
     *
     * @return lista de categorías
     */
    @Operation(operationId = "listarCategorias")
    @GetMapping("/api/categorias")
    public ResponseEntity<List<CategoriaDTO>> categorias() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }
}
