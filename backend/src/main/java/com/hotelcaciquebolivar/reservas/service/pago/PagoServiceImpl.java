package com.hotelcaciquebolivar.reservas.service.pago;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.pago.MetricasPagosResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoEventoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.SaldoPendiente;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoDesglose;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoEvento;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PagoMapper;
import com.hotelcaciquebolivar.reservas.repository.caja.CajaRepository;
import com.hotelcaciquebolivar.reservas.repository.caja.MovimientoCajaRepository;
import com.hotelcaciquebolivar.reservas.repository.caja.TurnoRepository;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoDesgloseRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoEventoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.TransaccionPagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.caja.CajaService;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.PagoPayload;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de gestión de pagos.
 * Maneja el registro de pagos (efectivo y en línea), reembolsos,
 * métricas del dashboard y eventos auditables.
 * Publica eventos SSE y notificaciones tras cada operación relevante.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final PagoRepository pagoRepository;
    private final PagoDesgloseRepository pagoDesgloseRepository;
    private final ConsumoRepository consumoRepository;
    private final PagoDesgloseService desgloseService;
    private final ReservaRepository reservaRepository;
    private final TransaccionPagoRepository transaccionRepository;
    private final PagoEventoRepository pagoEventoRepository;
    private final ReciboSequenceService reciboSequenceService;
    private final PagoMapper pagoMapper;
    private final AuditService auditService;
    private final CajaService cajaService;
    private final CajaRepository cajaRepository;
    private final TurnoRepository turnoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    /**
     * {@inheritDoc}
     * Registra el pago, genera eventos de timeline, publica SSE y notificaciones.
     * Para efectivo genera recibo consecutivo; para Wompi crea eventos sintéticos.
     * Si la reserva estaba PENDIENTE o PENDIENTE_PAGO, la confirma.
     */
    @Override
    public PagoResponse registrar(PagoRequest dto, String email) {
        Reserva reserva = reservaRepository.findByIdWithLock(dto.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", dto.reservaId()));

        if (reserva.getEstado() == EstadoReserva.CANCELADA ||
                reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ValidationException("reserva",
                    "no se puede registrar pagos en reservas canceladas o finalizadas");
        }

        BigDecimal totalPagado = obtenerTotalPagado(reserva.getId());
        BigDecimal saldoPendiente = reserva.getTotal() != null
                ? reserva.getTotal().subtract(totalPagado)
                : BigDecimal.ZERO;

        if (dto.monto().compareTo(saldoPendiente) > 0) {
            throw new ValidationException("monto",
                    "el monto del pago ($" + dto.monto() + ") supera el saldo pendiente ($" + saldoPendiente + ")");
        }

        Pago pago = pagoMapper.toEntity(dto);
        pago.setReserva(reserva);
        pago.setEstado(EstadoPago.COMPLETADO);

        // Recibo consecutivo global para efectivo
        if (dto.metodoPago() == MetodoPago.EFECTIVO) {
            String recibo = reciboSequenceService.nextRecibo();
            pago.setCodigoPago(recibo);
            if (pago.getReferenciaExterna() == null || pago.getReferenciaExterna().isBlank()) {
                pago.setReferenciaExterna(recibo);
            }
        }

        Pago guardado = pagoRepository.save(pago);

        // Crear eventos auditables para timeline
        LocalDateTime now = LocalDateTime.now(BOGOTA);
        if (dto.metodoPago() == MetodoPago.EFECTIVO) {
            String empleado = email != null ? email : "Recepción";
            // Intentar resolver nombre real del actor si disponible
            try {
                ActorInfo actor = notificacionMessageBuilder.obtenerActor();
                if (actor != null && actor.nombre() != null) empleado = actor.nombre();
            } catch (Exception ignored) {}
            crearEvento(guardado, "Pago recibido en caja", guardado.getMonto() + " COP", now);
            crearEvento(guardado, "Registrado por " + empleado, null, now.plusSeconds(1));
            crearEvento(guardado, "Recibo #" + guardado.getCodigoPago() + " generado", null, now.plusSeconds(2));
        } else {
            crearEvento(guardado, "Transacción creada", null, now);
            crearEvento(guardado, "Enviada a Wompi", dto.metodoPago().name(), now.plusSeconds(1));
            crearEvento(guardado, "Aprobada por el banco", null, now.plusSeconds(2));
        }

        if (reserva.getEstado() == EstadoReserva.PENDIENTE || reserva.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
            reserva.confirmar();
            reservaRepository.save(reserva);
        }

        String canal = dto.metodoPago() == MetodoPago.EFECTIVO ? "EFECTIVO" : "WOMPI (Pago en línea)";
        String titularInfo = reserva.getTitularReserva() != null
                ? reserva.getTitularReserva().getNombreCompleto() + " " + reserva.getTitularReserva().getApellidoCompleto()
                        + " (" + reserva.getTitularReserva().getCedula() + ")"
                : "N/A";
        String referencia = guardado.getReferenciaExterna() != null ? guardado.getReferenciaExterna() : "—";
        String auditDetalle = String.format(
                "Pago registrado — %s\nReserva: %s\nTitular: %s\nMonto: $%s\nCanal: %s\nMétodo: %s\nReferencia: %s\nTipo: %s",
                guardado.getCodigoPago(), reserva.getCodigoReserva(), titularInfo,
                guardado.getMonto(), canal, guardado.getMetodoPago(), referencia, guardado.getTipoPago());
        // FIX audit: online usa titular.usuario.email/username (Google), no literal WOMPI
        String auditEmail = email;
        if ((auditEmail == null || auditEmail.isBlank()) && dto.metodoPago() != MetodoPago.EFECTIVO) {
            var titular = reserva.getTitularReserva();
            if (titular != null && titular.getUsuario() != null && titular.getUsuario().getEmail() != null) {
                auditEmail = titular.getUsuario().getEmail();
            } else {
                auditEmail = null; // queda SYSTEM, pero detalle lleva Titular para trazabilidad
            }
        }
        auditService.log(AuditAction.PAYMENT_REGISTERED, "Pago", guardado.getId(), auditDetalle, auditEmail);

        registrarDesgloseYCaja(guardado, reserva, email);

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.PAGO_REGISTRADO,
                new PagoPayload(
                        guardado.getId(),
                        guardado.getCodigoPago(),
                        reserva.getId(),
                        guardado.getMonto(),
                        guardado.getMetodoPago().name()),
                actor.email()));

        String clienteNombre = reserva.getTitularReserva() != null
                ? reserva.getTitularReserva().getNombreCompleto()
                + " " + reserva.getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoPago", guardado.getCodigoPago());
        detalles.put("codigoReserva", reserva.getCodigoReserva());
        detalles.put("monto", guardado.getMonto());
        detalles.put("metodoPago", guardado.getMetodoPago().name());
        // Mensaje neutro sin prefijo duplicado "Sistema Sistema" y con formato COP 70.000
        String montoFmt = formatCOP(guardado.getMonto());
        notificacionService.notificarATodos(TipoNotificacion.PAGO_REGISTRADO,
                "Se registró un pago de $" + montoFmt
                + " para la reserva " + reserva.getCodigoReserva()
                + " de " + clienteNombre + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "PAGO", guardado.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return pagoMapper.toResponse(guardado);
    }

    /**
     * Reparte el pago entre las cajas y asienta un movimiento por cada línea.
     *
     * <p>Solo los pagos en efectivo entran a caja, así que el desglose se persiste
     * únicamente para ese método de pago; en los demás el pago queda sin líneas y
     * conserva el comportamiento anterior. Ningún fallo de caja revierte el pago
     * ya registrado: se deja traza en el log para que el movimiento pueda
     * regularizarse después.</p>
     */
    private void registrarDesgloseYCaja(Pago guardado, Reserva reserva, String email) {
        if (guardado.getMetodoPago() != MetodoPago.EFECTIVO) {
            cajaService.registrarMovimientoPorPago(guardado, email);
            return;
        }

        List<PagoDesgloseLinea> lineas;
        try {
            lineas = desgloseService.calcularDesglose(reserva, guardado.getMonto());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PagoServiceImpl.class)
                    .warn("Pago {} guardado sin desglose; caja queda pendiente: {}",
                            guardado.getCodigoPago(), e.getMessage());
            return;
        }

        for (PagoDesgloseLinea linea : lineas) {
            try {
                PagoDesglose desglose = PagoDesglose.builder()
                        .pago(guardado)
                        .origen(linea.origen())
                        .monto(linea.monto())
                        .concepto(linea.concepto())
                        .consumo(linea.consumoId() != null
                                ? consumoRepository.findById(linea.consumoId()).orElse(null) : null)
                        .build();
                pagoDesgloseRepository.save(desglose);

                cajaService.registrarMovimientoPorPagoDesglose(
                        guardado, linea.monto(), linea.origen().name(), email);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(PagoServiceImpl.class)
                        .warn("Pago {}: línea de {} sin asentar en caja: {}",
                                guardado.getCodigoPago(), linea.origen(), e.getMessage());
            }
        }
    }

    private void crearEvento(Pago pago, String descripcion, String subtitulo, LocalDateTime ts) {
        PagoEvento ev = PagoEvento.builder()
                .pago(pago)
                .descripcion(descripcion)
                .subtitulo(subtitulo)
                .createdAt(ts)
                .build();
        pagoEventoRepository.save(ev);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PagoResponse buscarPorId(Long id) {
        return pagoMapper.toResponse(obtenerPago(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorReserva(Long reservaId) {
        return pagoRepository.findByReservaIdOrderByFechaPagoDesc(reservaId)
                .stream()
                .map(pagoMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     * Marca el pago como REEMBOLSADO, registra movimiento de caja, publica SSE y notificaciones.
     * Si tras el reembolso no queda saldo y la reserva estaba CONFIRMADA, la revierte a PENDIENTE.
     */
    @Override
    public PagoResponse reembolsar(Long id, String email) {
        Pago pago = obtenerPago(id);

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new ValidationException("estado",
                    "solo se puede reembolsar un pago en estado COMPLETADO");
        }

        Reserva reserva = pago.getReserva();
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ValidationException("reserva",
                    "no se puede reembolsar un pago de una reserva cancelada");
        }

        BigDecimal totalPagadoAntes = obtenerTotalPagado(reserva.getId());
        BigDecimal totalDespues = totalPagadoAntes.subtract(pago.getMonto());

        pago.setEstado(EstadoPago.REEMBOLSADO);
        Pago guardado = pagoRepository.save(pago);

        if (totalDespues.compareTo(BigDecimal.ZERO) <= 0
                && reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            boolean soloFueEstePago = totalPagadoAntes.compareTo(pago.getMonto()) == 0;
            if (soloFueEstePago) {
                reserva.setEstado(EstadoReserva.PENDIENTE);
                reservaRepository.save(reserva);
            }
        }

        try {
            cajaService.registrarMovimientoPorReembolso(guardado, email);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PagoServiceImpl.class).warn("Reembolso {} guardado pero caja pendiente: {}", guardado.getCodigoPago(), e.getMessage());
        }

        String canalReembolso = guardado.getMetodoPago() == MetodoPago.EFECTIVO ? "EFECTIVO" : "WOMPI (Pago en línea)";
        String detalleReembolso = String.format(
                "Pago reembolsado — %s\nReserva: %s\nMonto: $%s\nCanal: %s\nMétodo: %s\nSaldo después: $%s\nReferencia: %s",
                guardado.getCodigoPago(), guardado.getReserva().getCodigoReserva(),
                guardado.getMonto(), canalReembolso, guardado.getMetodoPago(), totalDespues,
                guardado.getReferenciaExterna() != null ? guardado.getReferenciaExterna() : "—");
        // FIX: actor real del reembolso (recepcionista) en lugar de SYSTEM
        auditService.log(AuditAction.UPDATE, "Pago", guardado.getId(), detalleReembolso, email);

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.PAGO_REEMBOLSADO,
                new PagoPayload(
                        guardado.getId(),
                        guardado.getCodigoPago(),
                        guardado.getReserva().getId(),
                        guardado.getMonto(),
                        guardado.getMetodoPago().name()),
                actor.email()));

        String clienteNombre = guardado.getReserva().getTitularReserva() != null
                ? guardado.getReserva().getTitularReserva().getNombreCompleto()
                + " " + guardado.getReserva().getTitularReserva().getApellidoCompleto()
                : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("codigoPago", guardado.getCodigoPago());
        detalles.put("codigoReserva", guardado.getReserva().getCodigoReserva());
        detalles.put("monto", guardado.getMonto());
        String montoFmtReembolso = formatCOP(guardado.getMonto());
        notificacionService.notificarATodos(TipoNotificacion.PAGO_REEMBOLSADO,
                "Se reembolsó un pago de $" + montoFmtReembolso
                + " de la reserva " + guardado.getReserva().getCodigoReserva()
                + " de " + clienteNombre + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "PAGO", guardado.getId(),
                List.of(Rol.ADMIN),
                actor.email());

        return pagoMapper.toResponse(guardado);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public SaldoPendiente consultarSaldoPendiente(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        BigDecimal total = reserva.getTotal() != null ? reserva.getTotal() : BigDecimal.ZERO;
        BigDecimal pagado = obtenerTotalPagado(reservaId);
        BigDecimal saldo = total.subtract(pagado);

        return new SaldoPendiente(
                reserva.getId(),
                reserva.getCodigoReserva(),
                total,
                pagado,
                saldo);
    }

    /**
     * {@inheritDoc}
     * Realiza filtrado en memoria combinando pagos y transacciones Wompi pendientes/rechazadas.
     * Ordena por fecha de creación descendente y aplica paginación manual.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PagoDashboardResponse> listarDashboard(String q, String canal, String estado, Pageable pageable) {
        List<Pago> pagos = pagoRepository.findAll();
        // Para wompi pendiente/rechazada sin Pago, también incluir TransaccionPago
        List<TransaccionPago> transacciones = transaccionRepository.findAll();

        List<PagoDashboardResponse> todos = new ArrayList<>();

        // Mapear pagos (efectivo + wompi aprobados)
        for (Pago p : pagos) {
            todos.add(toDashboard(p, null));
        }
        // Mapear transacciones wompi que no tienen pago asociado (pendientes/rechazadas) para completar dashboard
        for (TransaccionPago t : transacciones) {
            if (t.getPago() != null) continue; // ya representado por Pago
            // Solo wompi sin pago -> estado pendiente/rechazada
            if (t.getEstadoPasarela() == PaymentStatus.PENDING || t.getEstadoPasarela() == PaymentStatus.CREATED
                    || t.getEstadoPasarela() == PaymentStatus.DECLINED || t.getEstadoPasarela() == PaymentStatus.REJECTED
                    || t.getEstadoPasarela() == PaymentStatus.EXPIRED || t.getEstadoPasarela() == PaymentStatus.ERROR
                    || t.getEstadoPasarela() == PaymentStatus.VOIDED) {
                todos.add(toDashboardFromTransaccion(t));
            }
        }

        // Filtros
        String qLower = q != null ? q.trim().toLowerCase() : null;
        List<PagoDashboardResponse> filtrados = todos.stream()
                .filter(r -> {
                    if (qLower == null || qLower.isBlank()) return true;
                    return (r.clienteNombre() != null && r.clienteNombre().toLowerCase().contains(qLower))
                            || (r.codigoReserva() != null && r.codigoReserva().toLowerCase().contains(qLower))
                            || (r.numeroRecibo() != null && r.numeroRecibo().toLowerCase().contains(qLower))
                            || (r.wompiTransactionId() != null && r.wompiTransactionId().toLowerCase().contains(qLower))
                            || (r.habitacion() != null && r.habitacion().toLowerCase().contains(qLower));
                })
                .filter(r -> canal == null || canal.equalsIgnoreCase("todos") || r.canal().equalsIgnoreCase(canal))
                .filter(r -> estado == null || estado.equalsIgnoreCase("todos") || r.estado().equalsIgnoreCase(estado))
                .sorted(Comparator.comparing(PagoDashboardResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int total = filtrados.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<PagoDashboardResponse> pageContent = start >= total ? List.of() : filtrados.subList(start, end);
        return new PageImpl<>(pageContent, pageable, total);
    }

    /**
     * {@inheritDoc}
     * Calcula métricas en memoria: recaudado hoy, totales por canal, pendientes,
     * cuadre de caja recepción (turno abierto) y métricas tienda.
     */
    @Override
    @Transactional(readOnly = true)
    public MetricasPagosResponse metricas() {
        LocalDate hoy = LocalDate.now(BOGOTA);
        LocalDateTime inicio = hoy.atStartOfDay(BOGOTA).toLocalDateTime();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay(BOGOTA).toLocalDateTime();

        List<Pago> pagosHoy = pagoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPago.COMPLETADO
                        && p.getFechaPago() != null
                        && !p.getFechaPago().isBefore(inicio)
                        && p.getFechaPago().isBefore(fin))
                .toList();

        BigDecimal recaudadoHoy = pagosHoy.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Pago> completados = pagoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPago.COMPLETADO).toList();

        BigDecimal wompiMonto = completados.stream()
                .filter(p -> p.getMetodoPago() != MetodoPago.EFECTIVO)
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        long wompiCount = completados.stream().filter(p -> p.getMetodoPago() != MetodoPago.EFECTIVO).count();

        BigDecimal efectivoMonto = completados.stream()
                .filter(p -> p.getMetodoPago() == MetodoPago.EFECTIVO)
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        long efectivoCount = completados.stream().filter(p -> p.getMetodoPago() == MetodoPago.EFECTIVO).count();

        long pendientes = pagoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE).count();
        pendientes += transaccionRepository.findAll().stream()
                .filter(t -> t.getPago() == null && (t.getEstadoPasarela() == PaymentStatus.PENDING || t.getEstadoPasarela() == PaymentStatus.CREATED))
                .count();

        // Cuadre por turno RECEPCION + pendientes fuera de turno
        BigDecimal efectivoMontoTurno = null;
        BigDecimal ingresosCajaTurno = null;
        BigDecimal diferenciaCuadre = null;
        BigDecimal pendienteMonto = null;
        long pendienteCount = 0;
        try {
            var cajaOpt = cajaRepository.findByNombreAndActivoTrue("RECEPCION");
            if (cajaOpt.isPresent()) {
                var caja = cajaOpt.get();
                var turnoOpt = turnoRepository.findByCajaIdAndEstado(caja.getId(), com.hotelcaciquebolivar.reservas.entity.caja.EstadoTurno.ABIERTO);
                if (turnoOpt.isPresent()) {
                    var turno = turnoOpt.get();
                    LocalDateTime desdeTurno = turno.getFechaApertura();
                    LocalDateTime hastaTurno = LocalDateTime.now(BOGOTA);
                    efectivoMontoTurno = pagoRepository.sumEfectivoBetween(desdeTurno, hastaTurno);
                    if (efectivoMontoTurno == null) efectivoMontoTurno = BigDecimal.ZERO;
                    ingresosCajaTurno = movimientoCajaRepository.sumByTurnoIdAndTipo(turno.getId(), com.hotelcaciquebolivar.reservas.entity.caja.TipoMovimientoCaja.INGRESO);
                    if (ingresosCajaTurno == null) ingresosCajaTurno = BigDecimal.ZERO;
                    diferenciaCuadre = efectivoMontoTurno.subtract(ingresosCajaTurno);
                }
                // Movimientos sin turno (previamente "pendientes")
                pendienteMonto = movimientoCajaRepository.sumSinTurnoByCaja(caja.getId());
                if (pendienteMonto == null) pendienteMonto = BigDecimal.ZERO;
                pendienteCount = movimientoCajaRepository.countSinTurnoByCaja(caja.getId());
                // Si no hay turno, el gap global vs caja se explica por pendientes
                if (pendienteMonto.compareTo(BigDecimal.ZERO) > 0 && ingresosCajaTurno == null) {
                    ingresosCajaTurno = BigDecimal.ZERO;
                    efectivoMontoTurno = BigDecimal.ZERO;
                    diferenciaCuadre = pendienteMonto.negate();
                }
            }
        } catch (Exception ignored) {}

        // Métricas caja TIENDA
        BigDecimal tiendaMontoHoy = BigDecimal.ZERO;
        BigDecimal tiendaMontoTurno = null;
        BigDecimal tiendaPendienteMonto = BigDecimal.ZERO;
        long tiendaPendienteCount = 0;
        try {
            var tiendaOpt = cajaRepository.findByNombreAndActivoTrue("TIENDA");
            if (tiendaOpt.isPresent()) {
                var tienda = tiendaOpt.get();
                // Ventas hoy (movimientos INGRESO de TIENDA entre inicio/fin)
                BigDecimal hoyTienda = movimientoCajaRepository.sumByCajaIdAndTipoAndFechaBetween(
                        tienda.getId(), com.hotelcaciquebolivar.reservas.entity.caja.TipoMovimientoCaja.INGRESO, inicio, fin);
                if (hoyTienda != null) tiendaMontoHoy = hoyTienda;
                // Movimientos sin turno tienda
                BigDecimal pendTienda = movimientoCajaRepository.sumSinTurnoByCaja(tienda.getId());
                if (pendTienda != null) tiendaPendienteMonto = pendTienda;
                tiendaPendienteCount = movimientoCajaRepository.countSinTurnoByCaja(tienda.getId());
                // Monto del turno abierto tienda
                var turnoTiendaOpt = turnoRepository.findByCajaIdAndEstado(tienda.getId(), com.hotelcaciquebolivar.reservas.entity.caja.EstadoTurno.ABIERTO);
                if (turnoTiendaOpt.isPresent()) {
                    tiendaMontoTurno = movimientoCajaRepository.sumByTurnoIdAndTipo(
                            turnoTiendaOpt.get().getId(), com.hotelcaciquebolivar.reservas.entity.caja.TipoMovimientoCaja.INGRESO);
                    if (tiendaMontoTurno == null) tiendaMontoTurno = BigDecimal.ZERO;
                }
                // Si no hay turno pero hay pendientes, incluir monto pendiente hoy
                if (tiendaMontoHoy.compareTo(BigDecimal.ZERO) == 0 && tiendaPendienteMonto.compareTo(BigDecimal.ZERO) > 0) {
                    // No sumar automáticamente; se expone separado para que el dashboard lo distinga
                }
            }
        } catch (Exception ignored) {}

        return new MetricasPagosResponse(recaudadoHoy, wompiMonto, wompiCount, efectivoMonto, efectivoCount, pendientes,
                efectivoMontoTurno, ingresosCajaTurno, diferenciaCuadre, pendienteMonto, pendienteCount,
                tiendaMontoHoy, tiendaMontoTurno, tiendaPendienteMonto, tiendaPendienteCount);
    }

    /**
     * {@inheritDoc}
     * Si no hay eventos persistidos, genera eventos sintéticos según el método de pago.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PagoEventoResponse> listarEventos(Long pagoId) {
        Pago pago = obtenerPago(pagoId);
        List<PagoEvento> eventos = pagoEventoRepository.findByPagoIdOrderByCreatedAtAsc(pagoId);
        if (!eventos.isEmpty()) {
            return eventos.stream()
                    .map(e -> new PagoEventoResponse(e.getId(), e.getDescripcion(), e.getSubtitulo(), e.getCreatedAt()))
                    .toList();
        }
        // Fallback sintético si no hay eventos persistidos
        List<PagoEventoResponse> synthetic = new ArrayList<>();
        synthetic.add(new PagoEventoResponse(1L, "Transacción creada", null, pago.getCreatedAt()));
        if (pago.getMetodoPago() == MetodoPago.EFECTIVO) {
            synthetic.add(new PagoEventoResponse(2L, "Pago recibido en caja", pago.getMonto() + " COP", pago.getCreatedAt().plusSeconds(1)));
            synthetic.add(new PagoEventoResponse(3L, "Recibo #" + pago.getCodigoPago() + " generado", null, pago.getCreatedAt().plusSeconds(2)));
        } else {
            synthetic.add(new PagoEventoResponse(2L, "Enviada a Wompi", pago.getMetodoPago().name(), pago.getCreatedAt().plusSeconds(1)));
            synthetic.add(new PagoEventoResponse(3L, "Aprobada por el banco", null, pago.getCreatedAt().plusSeconds(2)));
        }
        return synthetic;
    }

    private PagoDashboardResponse toDashboard(Pago p, TransaccionPago t) {
        Reserva r = p.getReserva();
        String clienteNombre = r != null && r.getTitularReserva() != null
                ? r.getTitularReserva().getNombreCompleto() + " " + r.getTitularReserva().getApellidoCompleto()
                : "N/A";
        String habitacion = "Reserva " + (r != null ? r.getCodigoReserva() : "");
        if (r != null && r.getDetalles() != null && !r.getDetalles().isEmpty() && r.getDetalles().get(0).getHabitacion() != null) {
            habitacion = "Hab " + r.getDetalles().get(0).getHabitacion().getNumero() + " · " + r.getDetalles().get(0).getHabitacion().getTipo().getNombre();
        }
        String canal = p.getMetodoPago() == MetodoPago.EFECTIVO ? "efectivo" : "wompi";
        String estado = mapEstado(p.getEstado());
        String wompiId = t != null ? t.getReferenciaExterna() : p.getReferenciaExterna();
        String paymentMethod = p.getMetodoPago() != null ? p.getMetodoPago().name() : null;
        if (t != null && t.getMetodoPago() != null) paymentMethod = t.getMetodoPago().name();
        String recibidoPor = null;
        if (canal.equals("efectivo")) {
            // intentar extraer de observaciones o evento
            recibidoPor = "Recepción";
        }
        String numeroRecibo = canal.equals("efectivo") ? p.getCodigoPago() : null;
        LocalDateTime createdAt = p.getCreatedAt() != null ? p.getCreatedAt() : p.getFechaPago();
        return new PagoDashboardResponse(
                p.getId(), r != null ? r.getId() : null, r != null ? r.getCodigoReserva() : null,
                clienteNombre, habitacion, p.getMonto(), canal, estado, createdAt,
                canal.equals("wompi") ? wompiId : null,
                paymentMethod,
                t != null ? t.getEstadoPasarela().name() : null,
                null, recibidoPor, numeroRecibo);
    }

    private PagoDashboardResponse toDashboardFromTransaccion(TransaccionPago t) {
        Reserva r = t.getReserva();
        String clienteNombre = r != null && r.getTitularReserva() != null
                ? r.getTitularReserva().getNombreCompleto() + " " + r.getTitularReserva().getApellidoCompleto()
                : "N/A";
        String habitacion = "Reserva " + (r != null ? r.getCodigoReserva() : "");
        if (r != null && r.getDetalles() != null && !r.getDetalles().isEmpty() && r.getDetalles().get(0).getHabitacion() != null) {
            habitacion = "Hab " + r.getDetalles().get(0).getHabitacion().getNumero() + " · " + r.getDetalles().get(0).getHabitacion().getTipo().getNombre();
        }
        String estado = mapTransaccionEstado(t.getEstadoPasarela());
        String paymentMethod = t.getMetodoPago() != null ? t.getMetodoPago().name() : null;
        LocalDateTime createdAt = t.getCreatedAt() != null ? t.getCreatedAt() : t.getUpdatedAt();
        String motivo = null;
        if (t.getEstadoPasarela() == PaymentStatus.DECLINED || t.getEstadoPasarela() == PaymentStatus.REJECTED) {
            motivo = "Rechazada por el banco";
        }
        return new PagoDashboardResponse(
                t.getId(), r != null ? r.getId() : null, r != null ? r.getCodigoReserva() : null,
                clienteNombre, habitacion, t.getMonto(), "wompi", estado, createdAt,
                t.getReferenciaExterna(), paymentMethod, t.getEstadoPasarela().name(), motivo, null, null);
    }

    private String mapEstado(EstadoPago estado) {
        if (estado == EstadoPago.COMPLETADO) return "aprobada";
        if (estado == EstadoPago.PENDIENTE) return "pendiente";
        return "rechazada";
    }

    private String mapTransaccionEstado(PaymentStatus s) {
        if (s == PaymentStatus.APPROVED) return "aprobada";
        if (s == PaymentStatus.PENDING || s == PaymentStatus.CREATED) return "pendiente";
        return "rechazada";
    }

    private String formatCOP(BigDecimal monto) {
        if (monto == null) return "0";
        NumberFormat fmt = NumberFormat.getInstance(new Locale("es", "CO"));
        fmt.setMaximumFractionDigits(0);
        fmt.setMinimumFractionDigits(0);
        fmt.setGroupingUsed(true);
        return fmt.format(monto);
    }

    private BigDecimal obtenerTotalPagado(Long reservaId) {
        return pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(
                        reservaId, EstadoPago.COMPLETADO)
                .stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pago obtenerPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));
    }

}
