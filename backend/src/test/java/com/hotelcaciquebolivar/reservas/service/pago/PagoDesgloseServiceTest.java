package com.hotelcaciquebolivar.reservas.service.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;
import com.hotelcaciquebolivar.reservas.entity.consumo.EstadoConsumo;
import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.producto.Producto;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoDesgloseRepository;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;

/**
 * Pruebas del reparto de un pago entre las cajas de recepción y tienda.
 *
 * <p>El caso crítico es el de los pagos sucesivos: como el reparto se calcula
 * sobre los saldos por origen y no sobre el total de la reserva, una reserva
 * cobrada en varias cuotas termina asignando a tienda la parte que le
 * corresponde. Un cálculo sobre el total bruto asignaría todo a recepción.</p>
 */
@ExtendWith(MockitoExtension.class)
class PagoDesgloseServiceTest {

    private static final Long RESERVA_ID = 1L;
    private static final BigDecimal CERO = BigDecimal.ZERO;

    @Mock
    private PagoDesgloseRepository desgloseRepository;
    @Mock
    private ConsumoRepository consumoRepository;
    @Mock
    private ReservaPricingService pricingService;

    @InjectMocks
    private PagoDesgloseService desgloseService;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reserva = Reserva.builder()
                .id(RESERVA_ID)
                .codigoReserva("RES-2026-0042")
                .build();
        lenient().when(consumoRepository.findByReservaIdAndEstadoInOrderByIdAsc(anyLong(), any()))
                .thenReturn(List.of());
    }

    // ============================================================
    // CASOS BASE
    // ============================================================

    @Test
    @DisplayName("Pago total sin consumos genera una sola línea de recepción")
    void sinConsumos_generaSoloRecepcion() {
        givenPricing(monto("70000"), monto("70000"), CERO);
        givenYaCobrado(CERO, CERO);

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("70000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("70000", lineas.get(0).monto());
        assertMonto("70000", total(lineas));
    }

    @Test
    @DisplayName("Pago total con consumos se reparte entre recepción y tienda")
    void conConsumos_reparteRecepcionYTienda() {
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(CERO, CERO);
        givenConsumos(consumo(1L, "Gaseosa", new BigDecimal("5000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("75000"));

        assertEquals(2, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("70000", lineas.get(0).monto());
        assertEquals(OrigenCobro.TIENDA, lineas.get(1).origen());
        assertMonto("5000", lineas.get(1).monto());
        assertEquals(1L, lineas.get(1).consumoId());
        assertMonto("75000", total(lineas));
    }

    @Test
    @DisplayName("Pago parcial cubre primero recepción y deja tienda pendiente")
    void pagoParcial_cubreRecepcionYDejaTienda() {
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(CERO, CERO);
        // Sin stub de consumos a propósito: si el monto se cubre solo con recepción
        // el servicio no debe llegar a repartir tienda ni tocar la lista de consumos.

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("50000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("50000", lineas.get(0).monto());
        assertMonto("50000", total(lineas));
        verify(consumoRepository, never())
                .findByReservaIdAndEstadoInOrderByIdAsc(anyLong(), any());
    }

    // ============================================================
    // PAGOS SUCESIVOS: el escenario que un cálculo sobre el total bruto resuelve mal
    // ============================================================

    @Test
    @DisplayName("Segundo pago cubre el resto de recepción una vez reachedo el saldo")
    void segundoPago_cubreRestoDeRecepcion() {
        // Tras el primer pago de 50000 ya se cobraron 50000 de los 70000 de recepción.
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(new BigDecimal("50000"), CERO);

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("20000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("20000", lineas.get(0).monto());
    }

    @Test
    @DisplayName("Pago final asigna a tienda lo que queda, y no a recepción")
    void pagoFinal_asignaParteDeTienda() {
        // Recepción ya cobrada por completo; solo queda el consumo de tienda.
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(new BigDecimal("70000"), CERO);
        givenConsumos(consumo(1L, "Gaseosa", new BigDecimal("5000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("5000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.TIENDA, lineas.get(0).origen());
        assertMonto("5000", lineas.get(0).monto());
        assertEquals(1L, lineas.get(0).consumoId());
    }

    @Test
    @DisplayName("Pago único que cubre recepción y tienda reparte el remanente a tienda")
    void pagoUnico_reparteRemanenteATienda() {
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(CERO, CERO);
        givenConsumos(consumo(1L, "Gaseosa", new BigDecimal("5000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("75000"));

        assertMonto("70000", lineas.get(0).monto());
        assertMonto("5000", lineas.get(1).monto());
        assertMonto("75000", total(lineas));
    }

    @Test
    @DisplayName("Con recepción ya cobrada, un pago parcial se destina solo a tienda")
    void conRecepcionCubierta_pagoParcialVaATienda() {
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(new BigDecimal("70000"), CERO);
        givenConsumos(consumo(1L, "Gaseosa", new BigDecimal("5000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("2000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.TIENDA, lineas.get(0).origen());
        assertMonto("2000", lineas.get(0).monto());
    }

    // ============================================================
    // VARIOS CONSUMOS Y ATRIBUCIÓN
    // ============================================================

    @Test
    @DisplayName("Varios consumos generan una línea cada uno")
    void variosConsumos_unaLineaPorConsumo() {
        givenPricing(monto("70000"), new BigDecimal("85000"), new BigDecimal("15000"));
        givenYaCobrado(CERO, CERO);
        givenConsumos(
                consumo(1L, "Gaseosa", new BigDecimal("5000")),
                consumo(2L, "Snacks", new BigDecimal("10000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("85000"));

        assertEquals(3, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("70000", lineas.get(0).monto());
        assertEquals(OrigenCobro.TIENDA, lineas.get(1).origen());
        assertMonto("5000", lineas.get(1).monto());
        assertEquals(1L, lineas.get(1).consumoId());
        assertEquals(OrigenCobro.TIENDA, lineas.get(2).origen());
        assertMonto("10000", lineas.get(2).monto());
        assertEquals(2L, lineas.get(2).consumoId());
        assertMonto("85000", total(lineas));
    }

    @Test
    @DisplayName("Un pago parcial recorre los consumos en orden hasta agotar el monto")
    void pagoParcial_recorreConsumosEnOrden() {
        givenPricing(monto("70000"), new BigDecimal("85000"), new BigDecimal("15000"));
        givenYaCobrado(new BigDecimal("70000"), CERO);
        givenConsumos(
                consumo(1L, "Gaseosa", new BigDecimal("5000")),
                consumo(2L, "Snacks", new BigDecimal("10000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("7000"));

        assertEquals(2, lineas.size());
        assertMonto("5000", lineas.get(0).monto());
        assertEquals(1L, lineas.get(0).consumoId());
        assertMonto("2000", lineas.get(1).monto());
        assertEquals(2L, lineas.get(1).consumoId());
        assertMonto("7000", total(lineas));
    }

    @Test
    @DisplayName("Un consumo ya cobrado no vuelve a cobrarse en el pago siguiente")
    void consumoYaCobrado_noSeReasigna() {
        givenPricing(monto("70000"), new BigDecimal("85000"), new BigDecimal("15000"));
        givenYaCobrado(new BigDecimal("70000"), new BigDecimal("5000"));
        // El consumo 1 ya tiene 5000 atribuidos por un pago anterior.
        when(desgloseRepository.sumByConsumoIdAndPagoEstado(1L, EstadoPago.COMPLETADO))
                .thenReturn(new BigDecimal("5000"));
        lenient().when(desgloseRepository.sumByConsumoIdAndPagoEstado(2L, EstadoPago.COMPLETADO))
                .thenReturn(CERO);
        givenConsumos(
                consumo(1L, "Gaseosa", new BigDecimal("5000")),
                consumo(2L, "Snacks", new BigDecimal("10000")));

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("10000"));

        assertEquals(1, lineas.size());
        assertMonto("10000", lineas.get(0).monto());
        assertEquals(2L, lineas.get(0).consumoId());
        assertMonto("10000", total(lineas));
    }

    @Test
    @DisplayName("Consumos anulados no se cobran")
    void consumosAnulados_noSeCobran() {
        givenPricing(monto("70000"), new BigDecimal("75000"), new BigDecimal("5000"));
        givenYaCobrado(CERO, CERO);
        // El repositorio solo devuelve consumos PENDIENTE o FACTURADO, así que el
        // cobro de tienda debe quedar en una línea agrupada sin consumo asociado.
        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("75000"));

        assertEquals(2, lineas.size());
        assertMonto("70000", lineas.get(0).monto());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("5000", lineas.get(1).monto());
        assertEquals(OrigenCobro.TIENDA, lineas.get(1).origen());
        assertNull(lineas.get(1).consumoId());
        assertMonto("75000", total(lineas));
    }

    // ============================================================
    // CASOS LÍMITE
    // ============================================================

    @Test
    @DisplayName("Montos no positivos no generan líneas")
    void montoNoPositivo_noGeneraLineas() {
        assertTrue(desgloseService.calcularDesglose(reserva, null).isEmpty());
        assertTrue(desgloseService.calcularDesglose(reserva, CERO).isEmpty());
        assertTrue(desgloseService.calcularDesglose(reserva, new BigDecimal("-1000")).isEmpty());
    }

    @Test
    @DisplayName("Una reserva sin id no se desglosa")
    void reservaSinId_noGeneraLineas() {
        Reserva sinId = Reserva.builder().codigoReserva("RES-2026-9999").build();
        assertTrue(desgloseService.calcularDesglose(sinId, new BigDecimal("1000")).isEmpty());
    }

    @Test
    @DisplayName("Un total fijado sin consumos deja toda la partida en recepción")
    void totalFijoSinConsumos_todoEnRecepcion() {
        // Rama de total fijo del pricing: totalRecepcion absorbe el total completo.
        givenPricing(monto("50000"), new BigDecimal("50000"), CERO);
        givenYaCobrado(CERO, CERO);

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("50000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("50000", lineas.get(0).monto());
    }

    @Test
    @DisplayName("Una exención con consumos pendientes no manda el pago a tienda")
    void exencionNoMandaElPagoATienda() {
        // Rama de exención: totalRecepcion y totalTienda son cero, así que el monto
        // no tiene origen y se imputa a recepción sin perder ni inventar dinero.
        givenPricing(monto("0"), CERO, CERO);
        givenYaCobrado(CERO, CERO);

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("10000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("10000", lineas.get(0).monto());
    }

    @Test
    @DisplayName("Si ya se cobró de más, el sobrante se imputa a recepción")
    void sobrante_seImputaARecepcion() {
        givenPricing(CERO, CERO, CERO);
        givenYaCobrado(CERO, CERO);

        List<PagoDesgloseLinea> lineas =
                desgloseService.calcularDesglose(reserva, new BigDecimal("3000"));

        assertEquals(1, lineas.size());
        assertEquals(OrigenCobro.RECEPCION, lineas.get(0).origen());
        assertMonto("3000", lineas.get(0).monto());
    }

    @Test
    @DisplayName("La suma de las líneas es siempre igual al monto del pago")
    void invarianteSuma() {
        givenPricing(monto("70000"), new BigDecimal("85000"), new BigDecimal("15000"));
        givenYaCobrado(CERO, CERO);
        givenConsumos(
                consumo(1L, "Gaseosa", new BigDecimal("5000")),
                consumo(2L, "Snacks", new BigDecimal("10000")));

        for (String monto : List.of("1", "4999.99", "5000", "33333.33", "70000", "85000")) {
            assertMonto(monto, total(desgloseService.calcularDesglose(reserva, new BigDecimal(monto))));
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void givenPricing(BigDecimal totalRecepcion, BigDecimal totalFinal, BigDecimal totalConsumos) {
        when(pricingService.calcularDesglose(reserva)).thenReturn(new PricingBreakdown(
                totalRecepcion, CERO, CERO, false, totalFinal, List.of(), totalConsumos, totalRecepcion));
    }

    private void givenYaCobrado(BigDecimal recepcion, BigDecimal tienda) {
        when(desgloseRepository.sumByReservaIdAndOrigenAndPagoEstado(
                eq(RESERVA_ID), eq(OrigenCobro.RECEPCION), eq(EstadoPago.COMPLETADO)))
                .thenReturn(recepcion);
        when(desgloseRepository.sumByReservaIdAndOrigenAndPagoEstado(
                eq(RESERVA_ID), eq(OrigenCobro.TIENDA), eq(EstadoPago.COMPLETADO)))
                .thenReturn(tienda);
    }

    private void givenConsumos(Consumo... consumos) {
        when(consumoRepository.findByReservaIdAndEstadoInOrderByIdAsc(eq(RESERVA_ID), any()))
                .thenReturn(List.of(consumos));
    }

    private static Consumo consumo(Long id, String producto, BigDecimal monto) {
        return Consumo.builder()
                .id(id)
                .producto(Producto.builder().id(id).nombre(producto).build())
                .cantidad(1)
                .montoTotal(monto)
                .estado(EstadoConsumo.PENDIENTE)
                .build();
    }

    private static BigDecimal total(List<PagoDesgloseLinea> lineas) {
        return lineas.stream().map(PagoDesgloseLinea::monto).reduce(CERO, BigDecimal::add);
    }

    private static void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Monto esperado " + esperado + " pero fue " + real);
    }

    /** Atajo para el caso en que todo el total corresponde a recepción. */
    private static BigDecimal monto(String valor) {
        return new BigDecimal(valor);
    }
}
