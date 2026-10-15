package com.hotelcaciquebolivar.reservas.service.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.caja.AbrirTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.CerrarTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.ConciliacionCajaResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.ConciliacionResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.GastoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.MovimientoResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.TurnoResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.caja.Caja;
import com.hotelcaciquebolivar.reservas.entity.caja.EstadoTurno;
import com.hotelcaciquebolivar.reservas.entity.caja.MovimientoCaja;
import com.hotelcaciquebolivar.reservas.entity.caja.TipoMovimientoCaja;
import com.hotelcaciquebolivar.reservas.entity.caja.TipoTurno;
import com.hotelcaciquebolivar.reservas.entity.caja.Turno;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.caja.CajaRepository;
import com.hotelcaciquebolivar.reservas.repository.caja.MovimientoCajaRepository;
import com.hotelcaciquebolivar.reservas.repository.caja.TurnoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.GastoPayload;
import com.hotelcaciquebolivar.reservas.sse.payload.TurnoPayload;

/**
 * Implementación del servicio de gestión de caja del sistema hotelero.
 * Maneja la apertura y cierre de turnos, registro de gastos, movimientos
 * por pagos, reembolsos y ventas de productos.
 *
 * <p>Los movimientos se registran siempre en la tabla movimientos_caja.
 * Si hay un turno activo, se asocia al mismo; si no, queda con turno nulo.</p>
 */
@Service
@Transactional
public class CajaServiceImpl implements CajaService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final Logger log = LoggerFactory.getLogger(CajaServiceImpl.class);

    /** Cajas incluidas en la conciliación, en el orden en que se reportan. */
    private static final List<String> CAJAS_CONCILIACION = List.of("RECEPCION", "TIENDA");

    private final CajaRepository cajaRepository;
    private final TurnoRepository turnoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final PagoRepository pagoRepository;
    private final UserRepository userRepository;
    private final PageMapper pageMapper;

    private final ApplicationEventPublisher eventPublisher;

    public CajaServiceImpl(CajaRepository cajaRepository, TurnoRepository turnoRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            PagoRepository pagoRepository,
            UserRepository userRepository,
            PageMapper pageMapper, ApplicationEventPublisher eventPublisher) {
        this.cajaRepository = cajaRepository;
        this.turnoRepository = turnoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.pagoRepository = pagoRepository;
        this.userRepository = userRepository;
        this.pageMapper = pageMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TurnoResponse abrirTurno(String cajaNombre, AbrirTurnoRequest dto, String email) {
        Caja caja = obtenerOCrearCaja(cajaNombre);
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));

        turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO)
                .ifPresent(t -> {
                    throw new ValidationException("turno", "ya existe un turno abierto para " + cajaNombre);
                });

        Turno turno = new Turno();
        turno.setCaja(caja);
        turno.setUsuarioApertura(usuario);
        turno.setFechaApertura(LocalDateTime.now(BOGOTA));
        turno.setFondoInicial(dto.fondoInicial() != null ? dto.fondoInicial() : BigDecimal.ZERO);
        turno.setTipoTurno(TipoTurno.desdeAhora());
        turno.setEstado(EstadoTurno.ABIERTO);

        Turno guardado = turnoRepository.save(turno);

        log.info("Turno abierto — Caja: {}, Usuario: {}, Fondo: ${}",
                cajaNombre, email, turno.getFondoInicial());

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.TURNO_ABIERTO,
                new TurnoPayload(
                        guardado.getId(),
                        cajaNombre,
                        email,
                        turno.getFondoInicial(),
                        null,
                        "ABIERTO")));

        BigDecimal ingresos = movimientoCajaRepository.sumByTurnoIdAndTipo(guardado.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal gastos = movimientoCajaRepository.sumByTurnoIdAndTipo(guardado.getId(), TipoMovimientoCaja.GASTO);
        List<MovimientoResponse> movimientos = listarMovimientos(guardado.getId());
        return toResponse(guardado, movimientos, ingresos, gastos);
    }

    @Override
    public TurnoResponse cerrarTurno(String cajaNombre, CerrarTurnoRequest dto, String email) {
        Caja caja = obtenerOCrearCaja(cajaNombre);
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ValidationException("turno", "no hay un turno abierto para " + cajaNombre));

        BigDecimal ingresos = movimientoCajaRepository
                .sumByTurnoIdAndTipo(turno.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal gastos = movimientoCajaRepository
                .sumByTurnoIdAndTipo(turno.getId(), TipoMovimientoCaja.GASTO);

        BigDecimal esperado = turno.getFondoInicial().add(ingresos).subtract(gastos);
        BigDecimal diferencia = dto.contadoFinal().subtract(esperado);

        turno.setUsuarioCierre(usuario);
        turno.setFechaCierre(LocalDateTime.now(BOGOTA));
        turno.setContadoFinal(dto.contadoFinal());
        turno.setEsperadoCierre(esperado);
        turno.setDiferencia(diferencia);
        turno.setEstado(EstadoTurno.CERRADO);
        turno.setObservacionesCierre(dto.observaciones());

        Turno guardado = turnoRepository.save(turno);

        log.info("Turno cerrado — Caja: {}, Usuario: {}, Esperado: ${}, Contado: ${}, Diferencia: ${}",
                cajaNombre, email, esperado, dto.contadoFinal(), diferencia);

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.TURNO_CERRADO,
                new TurnoPayload(
                        guardado.getId(),
                        cajaNombre,
                        email,
                        turno.getFondoInicial(),
                        dto.contadoFinal(),
                        "CERRADO")));

        List<MovimientoResponse> movimientos = listarMovimientos(turno.getId());
        return toResponse(guardado, movimientos, ingresos, gastos);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponse obtenerTurnoActivo(String cajaNombre) {
        Caja caja = obtenerOCrearCaja(cajaNombre);

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResourceNotFoundException("Turno activo", cajaNombre));

        BigDecimal ingresos = movimientoCajaRepository
                .sumByTurnoIdAndTipo(turno.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal gastos = movimientoCajaRepository
                .sumByTurnoIdAndTipo(turno.getId(), TipoMovimientoCaja.GASTO);

        List<MovimientoResponse> movimientos = listarMovimientos(turno.getId());
        return toResponse(turno, movimientos, ingresos, gastos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TurnoResponse> obtenerHistorial(String cajaNombre, Pageable pageable) {
        Caja caja = obtenerOCrearCaja(cajaNombre);

        Page<Turno> turnoPage = turnoRepository.findByCajaIdOrderByFechaAperturaDesc(caja.getId(), pageable);
        List<Turno> turnos = turnoPage.getContent();
        Map<Long, BigDecimal[]> ingresosGastosPorTurno = new HashMap<>();
        Map<Long, List<MovimientoResponse>> movimientosPorTurno = new HashMap<>();

        if (!turnos.isEmpty()) {
            List<Long> ids = turnos.stream().map(Turno::getId).toList();

            // Batch: totales de ingresos/gastos por turno
            List<Object[]> totals = movimientoCajaRepository.sumByTurnoIdsGrouped(ids);
            for (Object[] row : totals) {
                Long turnoId = (Long) row[0];
                TipoMovimientoCaja tipo = (TipoMovimientoCaja) row[1];
                BigDecimal monto = (BigDecimal) row[2];
                ingresosGastosPorTurno
                        .computeIfAbsent(turnoId, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                if (tipo == TipoMovimientoCaja.INGRESO) {
                    ingresosGastosPorTurno.get(turnoId)[0] = monto;
                } else {
                    ingresosGastosPorTurno.get(turnoId)[1] = monto;
                }
            }

            // Batch: movimientos por turno (una sola consulta)
            List<MovimientoCaja> todosMovimientos = movimientoCajaRepository.findByTurnoIdInOrderByFechaAsc(ids);
            for (MovimientoCaja m : todosMovimientos) {
                Long turnoId = m.getTurno().getId();
                movimientosPorTurno.computeIfAbsent(turnoId, k -> new ArrayList<>());
                movimientosPorTurno.get(turnoId).add(toMovimientoResponse(m));
            }
        }

        Map<Long, List<MovimientoResponse>> movimientosRef = movimientosPorTurno;
        Page<TurnoResponse> page = turnoPage.map(turno -> {
            BigDecimal[] ig = ingresosGastosPorTurno.get(turno.getId());
            BigDecimal ingresos = ig != null ? ig[0] : BigDecimal.ZERO;
            BigDecimal gastos = ig != null ? ig[1] : BigDecimal.ZERO;
            List<MovimientoResponse> movs = movimientosRef.getOrDefault(turno.getId(), List.of());
            return toResponse(turno, movs, ingresos, gastos);
        });
        return pageMapper.toPageResponse(page);
    }

    @Override
    public MovimientoResponse registrarGasto(String cajaNombre, GastoRequest dto, String email) {
        Caja caja = obtenerOCrearCaja(cajaNombre);
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO).orElse(null);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTurno(turno);
        movimiento.setCaja(caja);
        movimiento.setTipo(TipoMovimientoCaja.GASTO);
        movimiento.setConcepto(dto.concepto());
        movimiento.setMonto(dto.monto());
        movimiento.setMetodoPago(MetodoPago.EFECTIVO);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones(dto.observaciones());

        MovimientoCaja guardado = movimientoCajaRepository.save(movimiento);

        log.info("Gasto registrado — Caja: {}, Turno: {}, Concepto: {}, Monto: ${}, Usuario: {}",
                cajaNombre, turno != null ? turno.getId() : "Sin turno", dto.concepto(), dto.monto(), email);

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.GASTO_REGISTRADO,
                new GastoPayload(
                        guardado.getId(),
                        dto.concepto(),
                        dto.monto(),
                        email)));

        return toMovimientoResponse(guardado);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ConciliacionResponse conciliar(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new ValidationException("rango",
                    "el rango de fechas de conciliación no es válido");
        }

        Map<String, BigDecimal> ingresos = new HashMap<>();
        Map<String, BigDecimal> gastos = new HashMap<>();
        for (String caja : CAJAS_CONCILIACION) {
            ingresos.put(caja, BigDecimal.ZERO);
            gastos.put(caja, BigDecimal.ZERO);
        }

        List<Object[]> grupos = movimientoCajaRepository
                .sumAgrupadoPorCajaYTipo(CAJAS_CONCILIACION, inicio, fin);
        for (Object[] grupo : grupos) {
            String caja = (String) grupo[0];
            TipoMovimientoCaja tipo = (TipoMovimientoCaja) grupo[1];
            BigDecimal total = (BigDecimal) grupo[2];
            if (tipo == TipoMovimientoCaja.INGRESO) {
                ingresos.merge(caja, total, BigDecimal::add);
            } else {
                gastos.merge(caja, total, BigDecimal::add);
            }
        }

        List<ConciliacionCajaResponse> cajas = new ArrayList<>();
        for (String nombre : CAJAS_CONCILIACION) {
            BigDecimal ingreso = ingresos.get(nombre);
            BigDecimal gasto = gastos.get(nombre);
            cajas.add(new ConciliacionCajaResponse(
                    nombre, ingreso, gasto, ingreso.subtract(gasto)));
        }

        BigDecimal totalRecepcion = balance(CAJAS_CONCILIACION.get(0), ingresos, gastos);
        BigDecimal totalTienda = balance(CAJAS_CONCILIACION.get(1), ingresos, gastos);

        return new ConciliacionResponse(inicio, fin, totalRecepcion, totalTienda, cajas);
    }

    private BigDecimal balance(String caja, Map<String, BigDecimal> ingresos, Map<String, BigDecimal> gastos) {
        return ingresos.get(caja).subtract(gastos.get(caja));
    }

    @Override
    public void registrarMovimientoPorPago(Pago pago, String email) {
        if (pago.getMetodoPago() != MetodoPago.EFECTIVO
                || pago.getEstado() != EstadoPago.COMPLETADO) {
            return;
        }

        Caja caja = cajaRepository.findByNombreAndActivoTrue("RECEPCION").orElse(null);
        if (caja == null) {
            log.error("Caja RECEPCION no existe para pago {}", pago.getCodigoPago());
            return;
        }

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO).orElse(null);
        User usuario = null;
        if (email != null) usuario = userRepository.findByEmail(email).orElse(null);
        if (usuario == null && turno != null) usuario = turno.getUsuarioApertura();
        if (usuario == null) {
            log.error("Usuario no encontrado para pago {} email {}", pago.getCodigoPago(), email);
            return;
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTurno(turno);
        movimiento.setCaja(caja);
        movimiento.setTipo(TipoMovimientoCaja.INGRESO);
        movimiento.setConcepto("Pago " + pago.getReserva().getCodigoReserva());
        movimiento.setMonto(pago.getMonto());
        movimiento.setMetodoPago(MetodoPago.EFECTIVO);
        movimiento.setPago(pago);
        movimiento.setUsuario(usuario);

        movimientoCajaRepository.save(movimiento);

        log.info("Movimiento INGRESO por pago — Caja: {}, Turno: {}, Pago: {}, Monto: ${}",
                caja.getNombre(), turno != null ? turno.getId() : "Sin turno",
                pago.getCodigoPago(), pago.getMonto());
    }

    @Override
    public void registrarMovimientoPorPagoDesglose(Pago pago, BigDecimal monto, String cajaNombre, String email) {
        if (pago.getMetodoPago() != MetodoPago.EFECTIVO
                || pago.getEstado() != EstadoPago.COMPLETADO) {
            return;
        }
        if (monto == null || monto.signum() <= 0) {
            return;
        }

        Caja caja = cajaRepository.findByNombreAndActivoTrue(cajaNombre).orElse(null);
        if (caja == null) {
            log.error("Caja {} no existe para el pago {}; la línea queda sin asentar",
                    cajaNombre, pago.getCodigoPago());
            return;
        }

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO).orElse(null);
        User usuario = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        if (usuario == null && turno != null) usuario = turno.getUsuarioApertura();
        if (usuario == null) {
            log.error("Usuario no encontrado para el pago {} (email {}); la línea queda sin asentar",
                    pago.getCodigoPago(), email);
            return;
        }

        String concepto = "Pago " + pago.getCodigoPago() + " - " + cajaNombre;
        if (concepto.length() > 200) concepto = concepto.substring(0, 200);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTurno(turno);
        movimiento.setCaja(caja);
        movimiento.setTipo(TipoMovimientoCaja.INGRESO);
        movimiento.setConcepto(concepto);
        movimiento.setMonto(monto);
        movimiento.setMetodoPago(MetodoPago.EFECTIVO);
        movimiento.setPago(pago);
        movimiento.setUsuario(usuario);

        movimientoCajaRepository.save(movimiento);

        log.info("Movimiento INGRESO por línea de desglose — Caja: {}, Turno: {}, Pago: {}, Monto: ${}",
                caja.getNombre(), turno != null ? turno.getId() : "Sin turno",
                pago.getCodigoPago(), monto);
    }

    @Override
    public void registrarMovimientoPorReembolso(Pago pago, String email) {
        if (pago.getMetodoPago() != MetodoPago.EFECTIVO) {
            return;
        }

        Caja caja = cajaRepository.findByNombreAndActivoTrue("RECEPCION").orElse(null);
        if (caja == null) {
            log.error("Caja RECEPCION no existe para reembolso {}", pago.getCodigoPago());
            return;
        }

        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO).orElse(null);
        User usuario = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        if (usuario == null && turno != null) usuario = turno.getUsuarioApertura();
        if (usuario == null) {
            log.error("Usuario no encontrado para reembolso {} email {}", pago.getCodigoPago(), email);
            return;
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTurno(turno);
        movimiento.setCaja(caja);
        movimiento.setTipo(TipoMovimientoCaja.GASTO);
        movimiento.setConcepto("Reembolso " + pago.getCodigoPago());
        movimiento.setMonto(pago.getMonto());
        movimiento.setMetodoPago(MetodoPago.EFECTIVO);
        movimiento.setPago(pago);
        movimiento.setUsuario(usuario);

        movimientoCajaRepository.save(movimiento);

        log.info("Movimiento GASTO por reembolso — Caja: {}, Turno: {}, Pago: {}, Monto: ${}",
                caja.getNombre(), turno != null ? turno.getId() : "Sin turno",
                pago.getCodigoPago(), pago.getMonto());
    }

    @Override
    public void registrarMovimientoPorVenta(Long productoId, String productoNombre, int cantidad,
            BigDecimal montoTotal, MetodoPago metodoPago, String email, String cajaNombre) {
        String cajaTarget = cajaNombre != null ? cajaNombre : "TIENDA";
        MetodoPago metodo = metodoPago != null ? metodoPago : MetodoPago.EFECTIVO;
        Caja caja = cajaRepository.findByNombreAndActivoTrue(cajaTarget).orElse(null);
        if (caja == null) {
            log.error("Caja {} no existe para venta", cajaTarget);
            return;
        }
        Turno turno = turnoRepository.findByCajaIdAndEstado(caja.getId(), EstadoTurno.ABIERTO).orElse(null);
        User usuario = null;
        if (email != null) usuario = userRepository.findByEmail(email).orElse(null);
        if (usuario == null && turno != null) usuario = turno.getUsuarioApertura();
        if (usuario == null) {
            log.error("Usuario no encontrado para venta producto {} email {}", productoNombre, email);
            return;
        }
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Monto venta inválido {} para producto {}", montoTotal, productoNombre);
            return;
        }

        String concepto = "Venta TIENDA " + (productoNombre != null ? productoNombre : "Producto#" + productoId) + " x" + cantidad;
        if (concepto.length() > 200) concepto = concepto.substring(0, 200);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTurno(turno);
        movimiento.setCaja(caja);
        movimiento.setTipo(TipoMovimientoCaja.INGRESO);
        movimiento.setConcepto(concepto);
        movimiento.setMonto(montoTotal);
        movimiento.setMetodoPago(metodo);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones("Venta productoId=" + productoId + " caja=" + cajaTarget + " metodo=" + metodo.name());

        movimientoCajaRepository.save(movimiento);

        log.info("Movimiento INGRESO por venta — Caja: {}, Turno: {}, Producto: {}, Monto: ${}, Metodo: {}",
                cajaTarget, turno != null ? turno.getId() : "Sin turno", productoNombre, montoTotal, metodo);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMovimientosSinTurno(String cajaNombre) {
        return cajaRepository.findByNombreAndActivoTrue(cajaNombre)
                .map(c -> movimientoCajaRepository.countSinTurnoByCaja(c.getId())).orElse(0L);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumMovimientosSinTurno(String cajaNombre) {
        return cajaRepository.findByNombreAndActivoTrue(cajaNombre)
                .map(c -> {
                    BigDecimal s = movimientoCajaRepository.sumSinTurnoByCaja(c.getId());
                    return s != null ? s : BigDecimal.ZERO;
                }).orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientosSinTurno(String cajaNombre) {
        return cajaRepository.findByNombreAndActivoTrue(cajaNombre)
                .map(c -> movimientoCajaRepository.findByTurnoIsNullAndCajaIdOrderByFechaDesc(c.getId())
                        .stream()
                        .map(this::toMovimientoResponse)
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarTodosLosMovimientos(String cajaNombre) {
        return cajaRepository.findByNombreAndActivoTrue(cajaNombre)
                .map(c -> movimientoCajaRepository.findByCajaIdOrderByFechaDesc(c.getId())
                        .stream()
                        .map(this::toMovimientoResponse)
                        .toList())
                .orElse(List.of());
    }

    private Caja obtenerOCrearCaja(String nombre) {
        return cajaRepository.findByNombreAndActivoTrue(nombre)
                .orElseGet(() -> {
                    Caja caja = new Caja();
                    caja.setNombre(nombre);
                    caja.setActivo(true);
                    return cajaRepository.save(caja);
                });
    }

    private List<MovimientoResponse> listarMovimientos(Long turnoId) {
        return movimientoCajaRepository.findByTurnoIdOrderByFechaAsc(turnoId)
                .stream()
                .map(this::toMovimientoResponse)
                .toList();
    }

    private TurnoResponse toResponse(Turno turno, List<MovimientoResponse> movimientos,
            BigDecimal ingresos, BigDecimal gastos) {
        BigDecimal esperado = turno.getFondoInicial().add(ingresos).subtract(gastos);

        return new TurnoResponse(
                turno.getId(),
                turno.getCaja().getNombre(),
                turno.getTipoTurno().name(),
                turno.getTipoTurno().getDisplayName(),
                turno.getUsuarioApertura().getUsername(),
                turno.getUsuarioCierre() != null ? turno.getUsuarioCierre().getUsername() : null,
                turno.getFechaApertura(),
                turno.getFechaCierre(),
                turno.getFondoInicial(),
                ingresos,
                gastos,
                esperado,
                turno.getContadoFinal() != null ? turno.getContadoFinal() : esperado,
                turno.getDiferencia(),
                turno.getEstado().name(),
                turno.getObservacionesCierre(),
                movimientos);
    }

    private MovimientoResponse toMovimientoResponse(MovimientoCaja m) {
        return new MovimientoResponse(
                m.getId(),
                m.getTipo().name(),
                m.getConcepto(),
                m.getMonto(),
                m.getMetodoPago().name(),
                m.getPago() != null ? m.getPago().getId() : null,
                m.getUsuario().getUsername(),
                m.getCaja() != null ? m.getCaja().getNombre() : null,
                m.getTurno() != null ? m.getTurno().getId() : null,
                m.getFecha(),
                m.getObservaciones());
    }
}
