package com.hotelcaciquebolivar.reservas.service.pago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.SaldoPendiente;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoDesglose;
import com.hotelcaciquebolivar.reservas.entity.pago.TipoPago;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoDesgloseRepository;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PagoMapper;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoEventoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.TransaccionPagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.caja.CajaService;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.service.pago.PagoServiceImpl;
import com.hotelcaciquebolivar.reservas.service.pago.ReciboSequenceService;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private PagoMapper pagoMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private CajaService cajaService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private NotificacionMessageBuilder notificacionMessageBuilder;
    @Mock
    private ReciboSequenceService reciboSequenceService;
    @Mock
    private TransaccionPagoRepository transaccionPagoRepository;
    @Mock
    private PagoEventoRepository pagoEventoRepository;
    @Mock
    private PagoDesgloseRepository pagoDesgloseRepository;
    @Mock
    private ConsumoRepository consumoRepository;
    @Mock
    private PagoDesgloseService desgloseService;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Reserva reserva;
    private PagoRequest dto;
    private Pago pagoEntity;
    private Pago pagoGuardado;
    private PagoResponse responseMock;

    @BeforeEach
    void setUp() {
        ActorInfo actor = new ActorInfo("test@test.com", "Test User",
                "\ud83d\udc64 Recepcionista", "RECEPCION", "Test User");
        lenient().when(notificacionMessageBuilder.obtenerActor()).thenReturn(actor);
        lenient().when(reciboSequenceService.nextRecibo()).thenReturn("REC-2026-00001");
        lenient().when(pagoEventoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        reserva = Reserva.builder()
                .id(1L)
                .codigoReserva("2026-00001")
                .total(new BigDecimal("500"))
                .build();
        reserva.setEstado(EstadoReserva.PENDIENTE);

        dto = new PagoRequest(
                1L, new BigDecimal("500"),
                TipoPago.TOTAL, MetodoPago.EFECTIVO,
                null, "Pago total");

        pagoEntity = new Pago();
        pagoEntity.setMonto(new BigDecimal("500"));
        pagoEntity.setTipoPago(TipoPago.TOTAL);
        pagoEntity.setMetodoPago(MetodoPago.EFECTIVO);

        pagoGuardado = Pago.builder()
                .id(1L)
                .codigoPago("PAG-2026-00001")
                .reserva(reserva)
                .monto(new BigDecimal("500"))
                .tipoPago(TipoPago.TOTAL)
                .metodoPago(MetodoPago.EFECTIVO)
                .estado(EstadoPago.COMPLETADO)
                .build();

        responseMock = new PagoResponse(
                1L, "PAG-2026-00001", 1L, "2026-00001",
                new BigDecimal("500"), TipoPago.TOTAL, MetodoPago.EFECTIVO,
                EstadoPago.COMPLETADO, null, "Pago total", null);
    }

    // ============================================================
    // REGISTRAR
    // ============================================================
    @Test
    void registrar_deberiaCrearPagoCorrectamente() {
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dto)).thenReturn(pagoEntity);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        PagoResponse result = pagoService.registrar(dto, "test@email.com");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(EstadoPago.COMPLETADO, result.estado());
        verify(reservaRepository).findByIdWithLock(1L);
        verify(pagoRepository).save(any(Pago.class));
        verify(pagoMapper).toResponse(pagoGuardado);
    }

    @Test
    void registrar_reservaNoEncontrada_deberiaLanzarExcepcion() {
        when(reservaRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        PagoRequest dto = new PagoRequest(
                99L, new BigDecimal("500"),
                TipoPago.TOTAL, MetodoPago.EFECTIVO,
                null, null);

        assertThrows(ResourceNotFoundException.class, () -> pagoService.registrar(dto, "test@email.com"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void registrar_montoExcedeSaldo_deberiaLanzarExcepcion() {
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());

        PagoRequest dto = new PagoRequest(
                1L, new BigDecimal("999999"),
                TipoPago.PARCIAL, MetodoPago.EFECTIVO,
                null, null);

        assertThrows(ValidationException.class, () -> pagoService.registrar(dto, "test@email.com"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void registrar_autoConfirmaReservaPendiente_deberiaConfirmar() {
        reserva.setEstado(EstadoReserva.PENDIENTE);
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dto)).thenReturn(pagoEntity);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        pagoService.registrar(dto, "test@email.com");

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void registrar_reservaNoPendiente_noDebeAutoConfirmar() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dto)).thenReturn(pagoEntity);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        pagoService.registrar(dto, "test@email.com");

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository, never()).save(reserva);
    }

    @Test
    void registrar_conPagoParcial_deberiaCalcularSaldoCorrectamente() {
        BigDecimal montoParcial = new BigDecimal("200");
        PagoRequest dtoParcial = new PagoRequest(
                1L, montoParcial,
                TipoPago.PARCIAL, MetodoPago.TARJETA_CREDITO,
                null, null);

        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        pagoEntity.setMonto(montoParcial);
        when(pagoMapper.toEntity(dtoParcial)).thenReturn(pagoEntity);

        Pago pagoParcialGuardado = Pago.builder()
                .id(2L).codigoPago("PAG-2026-00002")
                .reserva(reserva).monto(montoParcial)
                .tipoPago(TipoPago.PARCIAL).metodoPago(MetodoPago.TARJETA_CREDITO)
                .estado(EstadoPago.COMPLETADO).build();

        PagoResponse responseParcial = new PagoResponse(
                2L, "PAG-2026-00002", 1L, "2026-00001",
                montoParcial, TipoPago.PARCIAL, MetodoPago.TARJETA_CREDITO,
                EstadoPago.COMPLETADO, null, null, null);

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoParcialGuardado);
        when(pagoMapper.toResponse(pagoParcialGuardado)).thenReturn(responseParcial);

        PagoResponse result = pagoService.registrar(dtoParcial, "test@email.com");

        assertNotNull(result);
        assertEquals(new BigDecimal("200"), result.monto());
        assertEquals(TipoPago.PARCIAL, result.tipoPago());
    }

    // ============================================================
    // DESGLOSE POR ORIGEN
    // ============================================================

    @Test
    void registrar_pagoEfectivo_deberiaPersistirDesgloseYMovimientosPorCaja() {
        PagoDesgloseLinea recepcion = new PagoDesgloseLinea(
                OrigenCobro.RECEPCION, new BigDecimal("400"), "Reserva 2026-00001", null);
        PagoDesgloseLinea tienda = new PagoDesgloseLinea(
                OrigenCobro.TIENDA, new BigDecimal("100"), "Gaseosa", 7L);
        when(desgloseService.calcularDesglose(reserva, new BigDecimal("500")))
                .thenReturn(List.of(recepcion, tienda));
        when(pagoDesgloseRepository.save(any(PagoDesglose.class)))
                .thenAnswer(i -> i.getArgument(0));

        registrarPagoEnEfectivo();

        verify(desgloseService).calcularDesglose(reserva, new BigDecimal("500"));
        verify(pagoDesgloseRepository, times(2)).save(any(PagoDesglose.class));
        verify(cajaService).registrarMovimientoPorPagoDesglose(
                eq(pagoGuardado), eq(new BigDecimal("400")),
                eq("RECEPCION"), eq("test@email.com"));
        verify(cajaService).registrarMovimientoPorPagoDesglose(
                eq(pagoGuardado), eq(new BigDecimal("100")),
                eq("TIENDA"), eq("test@email.com"));
    }

    @Test
    void registrar_pagoConTarjeta_noDebeGenerarDesgloseNiMovimientos() {
        PagoRequest dtoTarjeta = new PagoRequest(
                1L, new BigDecimal("500"),
                TipoPago.TOTAL, MetodoPago.TARJETA_CREDITO,
                null, null);
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dtoTarjeta)).thenReturn(pagoEntity);
        Pago pagoTarjetaGuardado = Pago.builder()
                .id(3L).codigoPago("PAG-2026-00003")
                .reserva(reserva).monto(new BigDecimal("500"))
                .tipoPago(TipoPago.TOTAL).metodoPago(MetodoPago.TARJETA_CREDITO)
                .estado(EstadoPago.COMPLETADO).build();
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoTarjetaGuardado);
        when(pagoMapper.toResponse(pagoTarjetaGuardado)).thenReturn(responseMock);

        pagoService.registrar(dtoTarjeta, "test@email.com");

        verify(desgloseService, never()).calcularDesglose(any(), any());
        verify(pagoDesgloseRepository, never()).save(any());
        verify(cajaService, never()).registrarMovimientoPorPagoDesglose(
                any(), any(), any(), any());
    }

    private void registrarPagoEnEfectivo() {
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dto)).thenReturn(pagoEntity);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        pagoService.registrar(dto, "test@email.com");
    }

    @Test
    void registrar_deberiaAuditarPagoRegistrado() {
        when(reservaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());
        when(pagoMapper.toEntity(dto)).thenReturn(pagoEntity);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        pagoService.registrar(dto, "test@email.com");

        verify(auditService).log(eq(AuditAction.PAYMENT_REGISTERED), eq("Pago"), eq(1L), anyString(), eq("test@email.com"));
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================
    @Test
    void buscarPorId_deberiaRetornarPago() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoGuardado));
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        PagoResponse result = pagoService.buscarPorId(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void buscarPorId_noEncontrado_deberiaLanzarExcepcion() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.buscarPorId(99L));
    }

    // ============================================================
    // LISTAR POR RESERVA
    // ============================================================
    @Test
    void listarPorReserva_deberiaRetornarLista() {
        when(pagoRepository.findByReservaIdOrderByFechaPagoDesc(1L))
                .thenReturn(List.of(pagoGuardado));
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);

        List<PagoResponse> result = pagoService.listarPorReserva(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void listarPorReserva_sinPagos_deberiaRetornarVacia() {
        when(pagoRepository.findByReservaIdOrderByFechaPagoDesc(1L))
                .thenReturn(List.of());

        List<PagoResponse> result = pagoService.listarPorReserva(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void listarPorReserva_conVariosPagos_deberiaRetornarTodos() {
        Pago pago2 = Pago.builder().id(2L).codigoPago("PAG-2026-00002").reserva(reserva)
                .monto(new BigDecimal("200")).estado(EstadoPago.COMPLETADO).build();
        PagoResponse response2 = new PagoResponse(
                2L, "PAG-2026-00002", 1L, "2026-00001",
                new BigDecimal("200"), null, null, EstadoPago.COMPLETADO, null, null, null);

        when(pagoRepository.findByReservaIdOrderByFechaPagoDesc(1L))
                .thenReturn(List.of(pagoGuardado, pago2));
        when(pagoMapper.toResponse(pagoGuardado)).thenReturn(responseMock);
        when(pagoMapper.toResponse(pago2)).thenReturn(response2);

        List<PagoResponse> result = pagoService.listarPorReserva(1L);

        assertEquals(2, result.size());
    }

    // ============================================================
    // REEMBOLSAR
    // ============================================================
    @Test
    void reembolsar_deberiaCambiarEstadoAReembolsado() {
        pagoGuardado.setEstado(EstadoPago.COMPLETADO);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoGuardado));

        Pago pagoReembolsado = Pago.builder()
                .id(1L).codigoPago("PAG-2026-00001").reserva(reserva)
                .monto(new BigDecimal("500")).estado(EstadoPago.REEMBOLSADO)
                .metodoPago(MetodoPago.EFECTIVO).build();

        PagoResponse responseReembolso = new PagoResponse(
                1L, "PAG-2026-00001", 1L, "2026-00001",
                new BigDecimal("500"), TipoPago.TOTAL, MetodoPago.EFECTIVO,
                EstadoPago.REEMBOLSADO, null, null, null);

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoReembolsado);
        when(pagoMapper.toResponse(pagoReembolsado)).thenReturn(responseReembolso);

        PagoResponse result = pagoService.reembolsar(1L, "test@email.com");

        assertEquals(EstadoPago.REEMBOLSADO, result.estado());
        verify(auditService).log(eq(AuditAction.UPDATE), eq("Pago"), eq(1L), anyString(), eq("test@email.com"));
    }

    @Test
    void reembolsar_pagoNoCompletado_deberiaLanzarExcepcion() {
        Pago pagoPendiente = Pago.builder()
                .id(2L).codigoPago("PAG-2026-00002")
                .reserva(reserva).monto(new BigDecimal("200"))
                .estado(EstadoPago.PENDIENTE).build();

        when(pagoRepository.findById(2L)).thenReturn(Optional.of(pagoPendiente));

        assertThrows(ValidationException.class, () -> pagoService.reembolsar(2L, "test@email.com"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void reembolsar_pagoNoEncontrado_deberiaLanzarExcepcion() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.reembolsar(99L, "test@email.com"));
    }

    // ============================================================
    // CONSULTAR SALDO PENDIENTE
    // ============================================================
    @Test
    void consultarSaldoPendiente_deberiaRetornarSaldo() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());

        SaldoPendiente result = pagoService.consultarSaldoPendiente(1L);

        assertEquals(1L, result.reservaId());
        assertEquals(new BigDecimal("500"), result.totalReserva());
        assertEquals(BigDecimal.ZERO, result.totalPagado());
        assertEquals(new BigDecimal("500"), result.saldoPendiente());
    }

    @Test
    void consultarSaldoPendiente_saldoCero_deberiaRetornarCero() {
        reserva.setTotal(new BigDecimal("0"));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());

        SaldoPendiente result = pagoService.consultarSaldoPendiente(1L);

        assertEquals(BigDecimal.ZERO, result.totalReserva());
        assertEquals(BigDecimal.ZERO, result.saldoPendiente());
    }

    @Test
    void consultarSaldoPendiente_conPagosParciales_deberiaCalcularBien() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        Pago pago1 = Pago.builder().id(1L).reserva(reserva).monto(new BigDecimal("200"))
                .estado(EstadoPago.COMPLETADO).build();
        Pago pago2 = Pago.builder().id(2L).reserva(reserva).monto(new BigDecimal("100"))
                .estado(EstadoPago.COMPLETADO).build();

        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of(pago1, pago2));

        SaldoPendiente result = pagoService.consultarSaldoPendiente(1L);

        assertEquals(new BigDecimal("500"), result.totalReserva());
        assertEquals(new BigDecimal("300"), result.totalPagado());
        assertEquals(new BigDecimal("200"), result.saldoPendiente());
    }

    @Test
    void consultarSaldoPendiente_reservaNoEncontrada_deberiaLanzarExcepcion() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pagoService.consultarSaldoPendiente(99L));
    }

    @Test
    void consultarSaldoPendiente_totalNulo_deberiaUsarCero() {
        reserva.setTotal(null);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaIdAndEstadoOrderByFechaPagoDesc(1L, EstadoPago.COMPLETADO))
                .thenReturn(List.of());

        SaldoPendiente result = pagoService.consultarSaldoPendiente(1L);

        assertEquals(BigDecimal.ZERO, result.totalReserva());
        assertEquals(BigDecimal.ZERO, result.saldoPendiente());
    }
}
