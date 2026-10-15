package com.hotelcaciquebolivar.reservas.service.consumo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoRequest;
import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;
import com.hotelcaciquebolivar.reservas.entity.consumo.EstadoConsumo;
import com.hotelcaciquebolivar.reservas.entity.producto.MovimientoInventario;
import com.hotelcaciquebolivar.reservas.entity.producto.Producto;
import com.hotelcaciquebolivar.reservas.entity.producto.TipoMovimiento;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.producto.StockInsuficienteException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ConsumoMapper;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.repository.producto.MovimientoInventarioRepository;
import com.hotelcaciquebolivar.reservas.repository.producto.ProductoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.payload.AjustePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de gestión de consumos del sistema hotelero.
 * Maneja el registro de productos o servicios adicionales consumidos por los huéspedes
 * durante su estancia, incluyendo la validación de stock, actualización de inventario,
 * cálculo de totales de reserva y registro de eventos de auditoría.
 * 
 * <p>Los consumos pueden asociarse a una reserva completa o a un detalle específico
 * de habitación dentro de la reserva.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumoServiceImpl implements ConsumoService {

    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleReservaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ConsumoRepository consumoRepository;
    private final UserRepository userRepository;
    private final ConsumoMapper consumoMapper;
    private final ReservaPricingService pricingService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Agrega un nuevo consumo a una reserva específica.
     * Valida que la reserva esté en estado editable, verifica el stock del producto,
     * descuenta el stock, crea un movimiento de inventario, registra el consumo,
     * recalcula el total de la reserva y publica un evento SSE.
     *
     * @param reservaId identificador único de la reserva a la que se agregará el consumo
     * @param dto objeto que contiene los datos del consumo a registrar
     * @param email email del usuario que registra el consumo
     * @return el consumo registrado con los datos completos
     * @throws ResourceNotFoundException si la reserva, producto o detalle no existen
     * @throws ValidationException si la reserva no es editable, el producto está inactivo o no tiene precio válido
     * @throws StockInsuficienteException si no hay stock suficiente del producto
     */
    @Override
    @Transactional
    public ConsumoResponse agregarConsumo(Long reservaId, ConsumoRequest dto, String email) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));
        validarReservaEditable(reserva);

        DetalleReserva detalle = null;
        if (dto.detalleReservaId() != null) {
            detalle = detalleReservaRepository.findById(dto.detalleReservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", dto.detalleReservaId()));
            if (!detalle.getReserva().getId().equals(reservaId)) {
                throw new ValidationException("detalleReservaId", "el detalle no pertenece a la reserva indicada");
            }
        }

        Producto producto = productoRepository.findByIdWithLock(dto.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", dto.productoId()));
        if (Boolean.FALSE.equals(producto.getActivo())) {
            throw new ValidationException("productoId", "el producto está inactivo");
        }

        int cantidad = dto.cantidad();
        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        if (stockActual < cantidad) {
            throw new StockInsuficienteException(producto.getId(), stockActual, cantidad);
        }

        BigDecimal precioUnitario = producto.getPrecioVenta() != null ? producto.getPrecioVenta() : BigDecimal.ZERO;
        if (precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("precioVenta", "el producto no tiene precio de venta válido");
        }
        BigDecimal montoTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        // Descontar stock
        int stockResultante = stockActual - cantidad;
        producto.setStockActual(stockResultante);
        productoRepository.save(producto);

        User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;

        MovimientoInventario mov = MovimientoInventario.builder()
                .producto(producto)
                .tipo(TipoMovimiento.VENTA)
                .cantidad(cantidad)
                .stockResultante(stockResultante)
                .motivo("Consumo reserva " + reserva.getCodigoReserva() + (detalle != null ? " hab " + detalle.getHabitacion().getNumero() : ""))
                .usuario(user)
                .referenciaId(reservaId)
                .build();
        MovimientoInventario savedMov = movimientoRepository.save(mov);

        Consumo consumo = Consumo.builder()
                .reserva(reserva)
                .detalleReserva(detalle)
                .producto(producto)
                .movimientoInventario(savedMov)
                .cantidad(cantidad)
                .precioUnitario(precioUnitario)
                .montoTotal(montoTotal)
                .estado(EstadoConsumo.PENDIENTE)
                .usuario(user)
                .observaciones(dto.observaciones())
                .build();
        Consumo saved = consumoRepository.save(consumo);

        // No se registra movimiento en caja TIENDA aqui: vender a una reserva no es
        // cobrar. El consumo queda PENDIENTE y su monto entra a la caja TIENDA
        // cuando el huésped paga, a traves del desglose del pago.

        // Recalcular total reserva incluyendo consumos
        var desglose = pricingService.calcularDesglose(reserva);
        reserva.setTotal(desglose.totalFinal());
        reservaRepository.save(reserva);

        auditService.log(com.hotelcaciquebolivar.reservas.entity.audit.AuditAction.UPDATE, "Reserva", reservaId,
                String.format("Consumo agregado — %s\nProducto: %s x%d = $%s\nReserva: %s%s",
                        saved.getId(), producto.getNombre(), cantidad, montoTotal, reserva.getCodigoReserva(),
                        detalle != null ? " Detalle#" + detalle.getId() + " Hab " + detalle.getHabitacion().getNumero() : ""));

        // SSE reutiliza AJUSTE_AGREGADO para desglose
        try {
            eventPublisher.publishEvent(new SseDomainEvent(
                    com.hotelcaciquebolivar.reservas.sse.SseEventTypes.AJUSTE_AGREGADO,
                    new AjustePayload(saved.getId(), reservaId, "CONSUMO", producto.getNombre() + " x" + cantidad, montoTotal)));
        } catch (Exception ignored) {}

        log.info("Consumo agregado reserva={}, producto={}, cantidad={}, total={}, detalle={}", reservaId, producto.getNombre(), cantidad, montoTotal, detalle != null ? detalle.getId() : null);
        return consumoMapper.toResponse(saved);
    }

    /**
     * Lista todos los consumos asociados a una reserva específica, ordenados por fecha de creación descendente.
     *
     * @param reservaId identificador único de la reserva
     * @return lista de consumos registrados para la reserva
     * @throws ResourceNotFoundException si la reserva no existe
     */
    @Override
    @Transactional(readOnly = true)
    public List<ConsumoResponse> listarPorReserva(Long reservaId) {
        reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));
        return consumoRepository.findByReservaIdOrderByCreatedAtDesc(reservaId)
                .stream().map(consumoMapper::toResponse).toList();
    }

    /**
     * Anula un consumo específico de una reserva.
     * Valida que el consumo pertenezca a la reserva, que no esté anulado ni facturado,
     * repone el stock del producto, crea un movimiento de inventario de tipo ENTRADA,
     * marca el consumo como ANULADO y recalcula el total de la reserva.
     *
     * @param reservaId identificador único de la reserva
     * @param consumoId identificador único del consumo a anular
     * @param email email del usuario que anula el consumo
     * @throws ResourceNotFoundException si la reserva o el consumo no existen
     * @throws ValidationException si el consumo no pertenece a la reserva, ya está anulado o está facturado
     */
    @Override
    @Transactional
    public void anularConsumo(Long reservaId, Long consumoId, String email) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));
        Consumo consumo = consumoRepository.findById(consumoId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumo", consumoId));
        if (!consumo.getReserva().getId().equals(reservaId)) {
            throw new ValidationException("consumo", "el consumo no pertenece a la reserva indicada");
        }
        if (consumo.getEstado() == EstadoConsumo.ANULADO) {
            throw new ValidationException("estado", "el consumo ya está anulado");
        }
        if (consumo.getEstado() == EstadoConsumo.FACTURADO) {
            throw new ValidationException("estado", "no se puede anular un consumo ya facturado");
        }

        // Reponer stock
        Producto producto = productoRepository.findByIdWithLock(consumo.getProducto().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", consumo.getProducto().getId()));
        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        int stockResultante = stockActual + consumo.getCantidad();
        producto.setStockActual(stockResultante);
        productoRepository.save(producto);

        User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        MovimientoInventario ajuste = MovimientoInventario.builder()
                .producto(producto)
                .tipo(TipoMovimiento.ENTRADA)
                .cantidad(consumo.getCantidad())
                .stockResultante(stockResultante)
                .motivo("Anulación consumo #" + consumo.getId() + " reserva " + reserva.getCodigoReserva())
                .usuario(user)
                .referenciaId(reservaId)
                .build();
        movimientoRepository.save(ajuste);

        consumo.setEstado(EstadoConsumo.ANULADO);
        consumoRepository.save(consumo);

        var desglose = pricingService.calcularDesglose(reserva);
        reserva.setTotal(desglose.totalFinal());
        reservaRepository.save(reserva);

        auditService.log(com.hotelcaciquebolivar.reservas.entity.audit.AuditAction.UPDATE, "Reserva", reservaId,
                String.format("Consumo anulado — %s\nProducto: %s x%d", consumo.getId(), producto.getNombre(), consumo.getCantidad()));
        log.info("Consumo anulado id={}, reserva={}, repuesto stock {}", consumoId, reservaId, consumo.getCantidad());
    }

    /**
     * Valida que una reserva esté en un estado que permita agregar consumos.
     * No permite agregar consumos a reservas FINALIZADA, CANCELADA o NO_SHOW.
     *
     * @param reserva reserva a validar
     * @throws ValidationException si la reserva no está en un estado editable
     */
    private void validarReservaEditable(Reserva reserva) {
        EstadoReserva est = reserva.getEstado();
        if (est == EstadoReserva.FINALIZADA || est == EstadoReserva.CANCELADA || est == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado", "no se pueden agregar consumos a una reserva " + est);
        }
    }
}
