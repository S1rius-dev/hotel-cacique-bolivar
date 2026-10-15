package com.hotelcaciquebolivar.reservas.service.producto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.producto.CategoriaDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.MovimientoInventarioDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoCreateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoResponseDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoUpdateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.TiendaMetricasDTO;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.producto.Categoria;
import com.hotelcaciquebolivar.reservas.entity.producto.MovimientoInventario;
import com.hotelcaciquebolivar.reservas.entity.producto.Producto;
import com.hotelcaciquebolivar.reservas.entity.producto.TipoMovimiento;
import com.hotelcaciquebolivar.reservas.exception.producto.SkuDuplicadoException;
import com.hotelcaciquebolivar.reservas.exception.producto.StockInsuficienteException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.mapper.ProductoMapper;
import com.hotelcaciquebolivar.reservas.repository.producto.CategoriaRepository;
import com.hotelcaciquebolivar.reservas.repository.producto.MovimientoInventarioRepository;
import com.hotelcaciquebolivar.reservas.repository.producto.ProductoRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.caja.CajaService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de gestión de productos y tienda.
 * Administra el catálogo de productos con validación de unicidad de SKU,
 * movimientos de inventario con control de stock, integración con la caja
 * de la tienda para ventas y cálculo de métricas.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoMapper mapper;
    private final UserRepository userRepository;
    private final CajaService cajaService;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listar(String search, Long categoriaId, String stock, Pageable pageable) {
        return productoRepository.buscarFiltrado(emptyToNull(search), categoriaId, stock != null ? stock.toUpperCase() : null, pageable)
                .map(mapper::toResponseDTO);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        return mapper.toResponseDTO(p);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorCodigo(String codigo) {
        Producto p = productoRepository.findBySkuBarcodeAndActivoTrue(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", codigo));
        return mapper.toResponseDTO(p);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {
        String sku = emptyToNull(dto.skuBarcode());
        if (sku != null && productoRepository.existsBySkuBarcodeIgnoreCase(sku)) {
            throw new SkuDuplicadoException(sku);
        }
        Categoria cat = null;
        if (dto.categoriaId() != null) {
            cat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", dto.categoriaId()));
        }
        Producto p = mapper.toEntity(dto);
        p.setCategoria(cat);
        if (dto.costo() != null) p.setCosto(dto.costo());
        if (dto.stockActual() != null) p.setStockActual(dto.stockActual());
        if (dto.stockMinimo() != null) p.setStockMinimo(dto.stockMinimo());
        p.setSkuBarcode(sku);
        Producto saved = productoRepository.save(p);
        return mapper.toResponseDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        String skuNuevo = emptyToNull(dto.skuBarcode());
        String skuActual = emptyToNull(p.getSkuBarcode());
        if (skuNuevo != null && !skuNuevo.equalsIgnoreCase(skuActual != null ? skuActual : "")) {
            if (productoRepository.existsBySkuBarcodeIgnoreCaseAndIdNot(skuNuevo, id)) {
                throw new SkuDuplicadoException(skuNuevo);
            }
            p.setSkuBarcode(skuNuevo);
        } else if (dto.skuBarcode() != null && skuNuevo == null) {
            // permite limpiar el código (envío de "" o "  " -> null)
            p.setSkuBarcode(null);
        }
        if (dto.nombre() != null) p.setNombre(dto.nombre());
        if (dto.precioVenta() != null) p.setPrecioVenta(dto.precioVenta());
        if (dto.costo() != null) p.setCosto(dto.costo());
        if (dto.stockMinimo() != null) p.setStockMinimo(dto.stockMinimo());
        if (dto.activo() != null) p.setActivo(dto.activo());
        if (dto.categoriaId() != null) {
            Categoria cat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", dto.categoriaId()));
            p.setCategoria(cat);
        }
        return mapper.toResponseDTO(productoRepository.save(p));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        p.setActivo(false);
        productoRepository.save(p);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Utiliza bloqueo pesimista ({@code findByIdWithLock}) para evitar condition racing
     * al actualizar el stock. Para ventas, registra el movimiento en la caja de la tienda
     * sin bloquear la operación principal (manejo de excepciones silencioso).
     */
    @Override
    @Transactional
    public MovimientoInventarioDTO registrarMovimiento(Long productoId, MovimientoInventarioDTO.MovimientoRequest req, String emailUsuario) {
        Producto p = productoRepository.findByIdWithLock(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productoId));

        int delta;
        switch (req.tipo()) {
            case ENTRADA -> delta = req.cantidad();
            case SALIDA, VENTA -> delta = -req.cantidad();
            case AJUSTE -> {
                delta = req.cantidad();
            }
            default -> delta = -req.cantidad();
        }

        int stockResultante = (p.getStockActual() != null ? p.getStockActual() : 0) + delta;
        if (stockResultante < 0) {
            throw new StockInsuficienteException(productoId, p.getStockActual(), req.cantidad());
        }
        p.setStockActual(stockResultante);
        productoRepository.save(p);

        var user = emailUsuario != null ? userRepository.findByEmail(emailUsuario).orElse(null) : null;

        MovimientoInventario mov = MovimientoInventario.builder()
                .producto(p)
                .tipo(req.tipo())
                .cantidad(req.cantidad())
                .stockResultante(stockResultante)
                .motivo(req.motivo())
                .usuario(user)
                .referenciaId(req.referenciaId())
                .build();
        MovimientoInventario saved = movimientoRepository.save(mov);

        // Integración con caja TIENDA solo para VENTA (no bloquea el movimiento de inventario)
        if (req.tipo() == TipoMovimiento.VENTA) {
            try {
                BigDecimal precio = p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO;
                BigDecimal montoTotal = precio.multiply(BigDecimal.valueOf(req.cantidad()));
                MetodoPago metodo = req.metodoPago() != null ? req.metodoPago() : MetodoPago.EFECTIVO;
                cajaService.registrarMovimientoPorVenta(productoId, p.getNombre(), req.cantidad(), montoTotal, metodo, emailUsuario, "TIENDA");
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(ProductoServiceImpl.class).warn("Venta producto {} guardada pero caja TIENDA pendiente: {}", p.getNombre(), e.getMessage());
            }
        }

        return mapper.toMovimientoDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientos(Long productoId) {
        productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productoId));
        return movimientoRepository.findByProductoIdOrderByCreatedAtDesc(productoId)
                .stream().map(mapper::toMovimientoDTO).toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Calcula el valor del inventario como la sumatoria de (precioVenta × stockActual)
     * para productos activos. El stock bajo se filtra por productos activos con stock > 0
     * y stock ≤ stockMínimo.
     */
    @Override
    @Transactional(readOnly = true)
    public TiendaMetricasDTO metricas() {
        long totalProductos = productoRepository.count();
        BigDecimal valorInventario = productoRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(p -> p.getPrecioVenta() != null && p.getStockActual() != null
                        ? p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getStockActual())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long stockBajo = productoRepository.findByStockActualLessThanEqualStockMinimo().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) && p.getStockActual() != null && p.getStockActual() > 0)
                .count();
        LocalDate hoy = LocalDate.now();
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.atTime(LocalTime.MAX);
        long vendidosHoyCount = movimientoRepository.countByTipoAndCreatedAtBetween(TipoMovimiento.VENTA, desde, hasta);
        BigDecimal vendidosHoyMonto = movimientoRepository.sumMontoVendidoBetween(TipoMovimiento.VENTA, desde, hasta);
        if (vendidosHoyMonto == null) vendidosHoyMonto = BigDecimal.ZERO;
        return new TiendaMetricasDTO(totalProductos, valorInventario, stockBajo, vendidosHoyCount, vendidosHoyMonto);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(mapper::toCategoriaDTO).toList();
    }

    /**
     * Convierte una cadena vacía o en blanco a null, o retorna la cadena recortada.
     *
     * @param s cadena a procesar
     * @return null si la cadena es null, vacía o en blanco; la cadena recortada en caso contrario
     */
    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
