package com.hotelcaciquebolivar.reservas.service.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.ActualizarDetalleRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleCheckInRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRapidaRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.reserva.ajustes.ReservaAjusteService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.ReservaPayload;
import com.hotelcaciquebolivar.reservas.service.reserva.checkin.CheckInOutService;
import com.hotelcaciquebolivar.reservas.service.reserva.crud.ReservaCrudService;
import com.hotelcaciquebolivar.reservas.service.reserva.detalles.ReservaDetalleService;
import com.hotelcaciquebolivar.reservas.service.reserva.disponibilidad.DisponibilidadService;
import com.hotelcaciquebolivar.reservas.service.reserva.extension.ReservaExtensionService;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;
import com.hotelcaciquebolivar.reservas.util.DineroUtil;
import com.hotelcaciquebolivar.reservas.service.reserva.rapida.ReservaRapidaService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio principal de reservas.
 * Actúa como fachada (Facade) que delega las operaciones a servicios especializados
 * según la responsabilidad: CRUD, check-in/check-out, detalles, disponibilidad,
 * precios, extensiones, ajustes y reserva rápida.
 *
 * <p>También gestiona el procesamiento automático de detalles vencidos y la
 * sincronización de estados de reserva cuando los detalles cambian.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaCrudService reservaCrudService;
    private final CheckInOutService checkInOutService;
    private final ReservaDetalleService reservaDetalleService;
    private final ReservaRapidaService reservaRapidaService;
    private final ReservaExtensionService reservaExtensionService;
    private final DisponibilidadService disponibilidadService;
    private final ReservaPricingService reservaPricingService;
    private final ReservaAjusteService reservaAjusteService;
    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleReservaRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     * Delega al servicio de disponibilidad la consulta de habitaciones disponibles.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> consultarDisponibilidad(LocalDate checkIn, LocalDate checkOut) {
        return disponibilidadService.consultarDisponibilidad(checkIn, checkOut);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio CRUD la creación de la reserva.
     */
    @Override
    public ReservaResponse crear(ReservaRequest dto) {
        return reservaCrudService.crear(dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio CRUD la búsqueda de la reserva por ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {
        return reservaCrudService.buscarPorId(id);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio CRUD el listado paginado de reservas con filtros.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> listarTodos(
            String codigo, String cliente, EstadoReserva estado,
            OrigenReserva origen,
            LocalDate checkInDesde, LocalDate checkInHasta,
            LocalDate checkOutDesde, LocalDate checkOutHasta,
            Pageable pageable) {
        return reservaCrudService.listarTodos(
                codigo, cliente, estado, origen,
                checkInDesde, checkInHasta,
                checkOutDesde, checkOutHasta, pageable);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio CRUD la actualización de la reserva.
     */
    @Override
    public ReservaResponse actualizar(Long id, ReservaUpdateRequest dto) {
        return reservaCrudService.actualizar(id, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio CRUD la eliminación de la reserva.
     */
    @Override
    public void eliminar(Long id) {
        reservaCrudService.eliminar(id);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de check-in/check-out la operación de check-in completo.
     */
    @Override
    public ReservaResponse realizarCheckIn(Long reservaId) {
        return checkInOutService.realizarCheckIn(reservaId);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de check-in/check-out la operación de check-out completo.
     */
    @Override
    public ReservaResponse realizarCheckOut(Long reservaId) {
        return checkInOutService.realizarCheckOut(reservaId);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de check-in/check-out el check-in de un detalle específico.
     */
    @Override
    public ReservaResponse checkInPorDetalle(Long reservaId, Long detalleId, DetalleCheckInRequest dto) {
        return checkInOutService.checkInPorDetalle(reservaId, detalleId, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de check-in/check-out el check-out de un detalle específico.
     */
    @Override
    public ReservaResponse checkOutPorDetalle(Long reservaId, Long detalleId) {
        return checkInOutService.checkOutPorDetalle(reservaId, detalleId);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de reserva rápida la creación con asignación automática.
     */
    @Override
    public ReservaResponse crearReservaRapida(ReservaRapidaRequest dto) {
        return reservaRapidaService.crearReservaRapida(dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de extensiones la extensión de la estadía.
     */
    @Override
    public ReservaResponse extenderEstadia(Long reservaId, LocalDate nuevaFechaCheckOut) {
        return reservaExtensionService.extenderEstadia(reservaId, nuevaFechaCheckOut);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de detalles la adición de huéspedes al detalle.
     */
    @Override
    public ReservaResponse agregarHuespedes(Long reservaId, Long detalleId, List<Long> huespedesIds) {
        return reservaDetalleService.agregarHuespedes(reservaId, detalleId, huespedesIds);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de detalles la remoción de un huésped.
     */
    @Override
    public ReservaResponse removerHuesped(Long reservaId, Long detalleId, Long detalleHuespedId) {
        return reservaDetalleService.removerHuesped(reservaId, detalleId, detalleHuespedId);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de detalles la remoción de un detalle de reserva.
     */
    @Override
    public ReservaResponse removerDetalle(Long reservaId, Long detalleId) {
        return reservaDetalleService.removerDetalle(reservaId, detalleId);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de detalles la adición de un nuevo detalle.
     */
    @Override
    public ReservaResponse agregarDetalle(Long reservaId, DetalleReservaRequest dto) {
        return reservaDetalleService.agregarDetalle(reservaId, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de detalles la actualización de un detalle.
     */
    @Override
    public ReservaResponse actualizarDetalle(Long reservaId, Long detalleId, ActualizarDetalleRequest dto) {
        return reservaDetalleService.actualizarDetalle(reservaId, detalleId, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de ajustes la adición de un nuevo ajuste.
     */
    @Override
    public AjusteResponse agregarAjuste(Long reservaId, AjusteRequest dto) {
        return reservaAjusteService.agregarAjuste(reservaId, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de ajustes la edición de un ajuste existente.
     */
    @Override
    public AjusteResponse editarAjuste(Long reservaId, Long ajusteId, AjusteRequest dto) {
        return reservaAjusteService.editarAjuste(reservaId, ajusteId, dto);
    }

    /**
     * {@inheritDoc}
     * Delega al servicio de ajustes la eliminación de un ajuste.
     */
    @Override
    public void eliminarAjuste(Long reservaId, Long ajusteId) {
        reservaAjusteService.eliminarAjuste(reservaId, ajusteId);
    }

    /**
     * {@inheritDoc}
     * Busca detalles de reserva cuya fecha de check-out ya pasó y que están en estado RESERVADA.
     * Cancela automáticamente estos detalles, libera las habitaciones asociadas,
     * recalcula el total de la reserva, registra auditoría y publica un evento SSE.
     * Sincroniza el estado de la reserva según el resultado del procesamiento.
     */
    @Override
    @Transactional
    public void procesarDetallesVencidos() {
        LocalDate hoy = LocalDate.now();
        List<DetalleReserva> vencidos = detalleReservaRepository.findReservadasVencidas(hoy);
        if (vencidos.isEmpty()) {
            return;
        }

        Set<Reserva> reservasAfectadas = new HashSet<>();

        for (DetalleReserva detalle : vencidos) {
            detalle.cancelar();
            Habitacion habitacion = detalle.getHabitacion();
            if (habitacion.getEstado() == EstadoHabitacion.RESERVADA) {
                habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
            }
            reservasAfectadas.add(detalle.getReserva());
        }

        detalleReservaRepository.saveAll(vencidos);

        for (Reserva reserva : reservasAfectadas) {
            sincronizarEstadoReservaPorVencimiento(reserva);
            BigDecimal fullTotal = reservaPricingService.calcularTotalReserva(reserva);
            reserva.setTotal(fullTotal);
            reservaRepository.save(reserva);
            auditService.log(AuditAction.UPDATE, "Reserva", reserva.getId(),
                    String.format("Procesamiento automático — %s: detalles vencidos cancelados",
                            reserva.getCodigoReserva()));

            eventPublisher.publishEvent(new SseDomainEvent(
                    SseEventTypes.RESERVA_VENCIDA,
                    new ReservaPayload(
                            reserva.getId(),
                            reserva.getCodigoReserva(),
                            reserva.getTitularReserva() != null
                                    ? reserva.getTitularReserva().getNombreCompleto()
                                    + " " + reserva.getTitularReserva().getApellidoCompleto()
                                    : "N/A",
                            reserva.getEstado().name(),
                            reserva.getFechaCheckIn().toString(),
                            reserva.getFechaCheckOut().toString(),
                            reserva.getTotal())));
        }
    }

    /**
     * Sincroniza el estado de una reserva después del procesamiento de detalles vencidos.
     * Evalúa la proporción de detalles en cada estado para determinar el nuevo estado
     * de la reserva:
     * <ul>
     *   <li>Si hay detalles ocupados, no cambia el estado</li>
     *   <li>Si todos los detalles están cancelados: PENDIENTE → CANCELADA, CONFIRMADA → NO_SHOW</li>
     *   <li>Si hay detalles liberados y cancelados: → FINALIZADA</li>
     * </ul>
     *
     * @param reserva la reserva cuyo estado se va a sincronizar
     */
    private void sincronizarEstadoReservaPorVencimiento(Reserva reserva) {
        List<DetalleReserva> detalles = reserva.getDetalles();
        long total = detalles.size();
        long canceladas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.CANCELADA).count();
        long liberadas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.LIBERADA).count();
        long ocupadas = detalles.stream()
                .filter(d -> d.getEstado() == EstadoDetalleReserva.OCUPADA).count();

        if (ocupadas > 0) {
            return;
        }

        if (canceladas == total) {
            EstadoReserva original = reserva.getEstado();
            if (original == EstadoReserva.PENDIENTE) {
                reserva.setEstado(EstadoReserva.CANCELADA);
            } else if (original == EstadoReserva.CONFIRMADA) {
                reserva.setEstado(EstadoReserva.NO_SHOW);
            }
            detalles.forEach(d -> {
                Habitacion h = d.getHabitacion();
                if (h.getEstado() == EstadoHabitacion.RESERVADA) {
                    h.setEstado(EstadoHabitacion.DISPONIBLE);
                }
            });
        } else if (liberadas > 0 && canceladas > 0) {
            reserva.setEstado(EstadoReserva.FINALIZADA);
        }
    }

    @Override
    @Transactional
    public ReservaResponse togglePrecioModo(Long reservaId, Long detalleId, boolean incluyeIva) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException("Reserva", reservaId));
        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> detalleId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException("DetalleReserva", detalleId));

        // Permitir cambio por el momento (no bloquear por pagos). Registrar en auditoría.
        EstadoReserva estado = reserva.getEstado();
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.FINALIZADA || estado == EstadoReserva.NO_SHOW) {
            throw new com.hotelcaciquebolivar.reservas.exception.validation.ValidationException("estado",
                    "El estado de la reserva no permite cambiar el modo de IVA (estado terminal)");
        }

        if (detalle.getPrecioUnitarioBase() == null || detalle.getPrecioUnitarioConIva() == null) {
            throw new com.hotelcaciquebolivar.reservas.exception.validation.ValidationException("precioModo",
                    "Precio base y precio con IVA son obligatorios para la tarifa/tipo vigente");
        }

        BigDecimal base = detalle.getPrecioUnitarioBase();
        BigDecimal conIva = detalle.getPrecioUnitarioConIva();
        BigDecimal aplicado;
        BigDecimal subtotal;
        BigDecimal ivaTotal;
        BigDecimal total;

        long noches = reserva.calcularNoches();
        BigDecimal extrasTotal = calcularExtrasTotalDetalle(detalle, noches);
        BigDecimal baseNoches = base.multiply(BigDecimal.valueOf(noches));

        if (incluyeIva) {
            aplicado = conIva;
            // total = conIva * noches + extras: el precio ya incluye IVA.
            BigDecimal ivaNoches = conIva.multiply(BigDecimal.valueOf(noches)).subtract(baseNoches);
            if (ivaNoches.compareTo(BigDecimal.ZERO) < 0) {
                ivaNoches = BigDecimal.ZERO;
            }
            // COP sin decimales: redondear subtotal e IVA por separado y sumar,
            // para que el desglose persistido siempre cuadre (subtotal + IVA = total).
            subtotal = DineroUtil.redondear(baseNoches.add(extrasTotal));
            ivaTotal = DineroUtil.redondear(ivaNoches);
            total = subtotal.add(ivaTotal);
        } else {
            aplicado = base;
            // Modo sin IVA: se cobra el precio base. Antes se sumaba el
            // diferencial conIva-base y el total quedaba en el valor con IVA.
            subtotal = DineroUtil.redondear(baseNoches.add(extrasTotal));
            ivaTotal = BigDecimal.ZERO;
            total = subtotal;
        }

        detalle.setPrecioUsadoIncluyeIva(incluyeIva);
        detalle.setPrecioUnitarioAplicado(aplicado);
        detalle.setSubtotalDetalle(subtotal);
        detalle.setIvaTotalDetalle(ivaTotal);
        detalle.setTotalDetalle(total);
        detalle.setPrecioPorNoche(aplicado);

        detalleReservaRepository.save(detalle);
        // El desglose ya respeta el modo de IVA del detalle (calcularTotalDetalle):
        // persistir el total para que saldoPendiente y el cobro usen el precio correcto.
        BigDecimal totalReserva = reservaPricingService.calcularTotalReserva(reserva);
        reserva.setTotal(totalReserva);
        reservaRepository.save(reserva);

        auditService.log(com.hotelcaciquebolivar.reservas.entity.audit.AuditAction.UPDATE, "DetalleReserva",
                detalle.getId(), "Cambio de modo de IVA a: " + (incluyeIva ? "INCLUIDO_IVA" : "EXCLUIDO_IVA"));

        return reservaCrudService.buscarPorId(reservaId);
    }

    /**
     * Calcula la parte de personas extra (precio persona extra x extras x noches)
     * del detalle, usando el snapshot congelado o el precio vigente del tipo.
     * Mantiene coherencia con {@code DetalleReservaMapper} y {@code ReservaPricingService}.
     */
    private BigDecimal calcularExtrasTotalDetalle(DetalleReserva detalle, long noches) {
        if (detalle.getHabitacion() == null || detalle.getHabitacion().getTipo() == null || noches <= 0) {
            return BigDecimal.ZERO;
        }
        int capBase = detalle.getHabitacion().getTipo().getCapacidadBase();
        int extras = Math.max(0, detalle.getCantidadOcupantes() - capBase);
        if (extras <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal precioExtra = detalle.getPrecioPersonaExtraSnapshot() != null
                ? detalle.getPrecioPersonaExtraSnapshot()
                : detalle.getHabitacion().getTipo().getPrecioPersonaExtra();
        if (precioExtra == null) {
            return BigDecimal.ZERO;
        }
        return precioExtra
                .multiply(BigDecimal.valueOf(extras))
                .multiply(BigDecimal.valueOf(noches));
    }
}
