package com.hotelcaciquebolivar.reservas.service.reserva.pricing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.AjusteReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.TipoAjuste;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;

/**
 * Verifica que los cálculos de precios siempre devuelvan pesos enteros (COP):
 * el ratio conIva/base con escala 6 y los ajustes porcentuales generaban
 * centavos que, sumados entre detalles, producían totales como 264.500,04.
 */
@ExtendWith(MockitoExtension.class)
class ReservaPricingServiceImplTest {

    @Mock
    private PrecioTemporalService precioTemporalService;

    @Mock
    private ConsumoRepository consumoRepository;

    private ReservaPricingServiceImpl service;

    private Reserva reserva;
    private DetalleReserva detalle;

    @BeforeEach
    void setUp() {
        service = new ReservaPricingServiceImpl(precioTemporalService, consumoRepository);

        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setId(1L);
        tipo.setNombre("Suite");
        tipo.setCapacidadBase(2);
        tipo.setCapacidadMaxima(4);
        tipo.setPrecioBase(new BigDecimal("68505"));
        tipo.setPrecioConIva(new BigDecimal("81520.95"));
        tipo.setPrecioPersonaExtra(new BigDecimal("5000"));

        Habitacion habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero("101");
        habitacion.setTipo(tipo);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);

        reserva = Reserva.builder()
                .fechaCheckIn(LocalDate.of(2026, 6, 10))
                .fechaCheckOut(LocalDate.of(2026, 6, 13))
                .build();

        detalle = DetalleReserva.builder()
                .habitacion(habitacion)
                .precioPorNoche(new BigDecimal("68505"))
                .cantidadOcupantes(2)
                .precioUsadoIncluyeIva(true)
                .precioUnitarioBase(new BigDecimal("68505"))
                .precioUnitarioConIva(new BigDecimal("81520.95"))
                .desgloseCongelado("[{\"fecha\":\"2026-06-10\",\"precioNoche\":68505,\"precioExtra\":5000},"
                        + "{\"fecha\":\"2026-06-11\",\"precioNoche\":68505,\"precioExtra\":5000},"
                        + "{\"fecha\":\"2026-06-12\",\"precioNoche\":68505,\"precioExtra\":5000}]")
                .build();
        detalle.setReserva(reserva);
        reserva.getDetalles().add(detalle);
    }

    @Test
    void calcularTotalDetalle_conIvaYRatioFraccionario_deberiaRetornarPesoEntero() {
        // 68505 x 1,19 = 81520,95 → 3 noches = 244.562,85 (debería quedar 244563)
        BigDecimal total = service.calcularTotalDetalle(detalle);

        assertEquals(0, total.scale());
        assertEquals(new BigDecimal("244563"), total);
    }

    @Test
    void calcularTotalDetalle_sinIva_deberiaRetornarPesoEntero() {
        detalle.setPrecioUsadoIncluyeIva(false);

        BigDecimal total = service.calcularTotalDetalle(detalle);

        assertEquals(0, total.scale());
        assertEquals(new BigDecimal("205515"), total);
    }

    @Test
    void calcularDesglose_conAjustePorcentual_deberiaRedondearImpactoYTotal() {
        reserva.setAjustes(List.of(AjusteReserva.builder()
                .id(1L)
                .reserva(reserva)
                .tipoAjuste(TipoAjuste.CARGO)
                .modoCalculo(ModoCalculo.PORCENTUAL)
                .concepto("Servicio")
                .valor(new BigDecimal("10"))
                .activo(true)
                .build()));

        PricingBreakdown desglose = service.calcularDesglose(reserva);

        // 10% de 244563 = 24456,30 → 24456
        assertEquals(new BigDecimal("24456"), desglose.totalCargos());
        assertEquals(0, desglose.totalCargos().scale());
        assertEquals(0, desglose.subtotal().scale());
        assertEquals(0, desglose.totalRecepcion().scale());
        assertEquals(0, desglose.totalFinal().scale());
        assertEquals(new BigDecimal("269019"), desglose.totalFinal());
    }

    @Test
    void calcularDesglose_conConsumosConCentavos_deberiaRedondearTotalFinal() {
        reserva.setId(1L);
        when(consumoRepository.sumByReservaIdAndEstadoIn(eq(1L), any()))
                .thenReturn(new BigDecimal("1500.40"));

        PricingBreakdown desglose = service.calcularDesglose(reserva);

        assertEquals(0, desglose.totalFinal().scale());
        // 244563 + 1500,40 = 246063,40 → 246063
        assertEquals(new BigDecimal("246063"), desglose.totalFinal());
    }

    @Test
    void calcularTotalReserva_multiplesDetallesConIva_deberiaSerEntero() {
        DetalleReserva segundo = DetalleReserva.builder()
                .habitacion(detalle.getHabitacion())
                .precioPorNoche(new BigDecimal("68505"))
                .cantidadOcupantes(2)
                .precioUsadoIncluyeIva(true)
                .precioUnitarioBase(new BigDecimal("68505"))
                .precioUnitarioConIva(new BigDecimal("81520.95"))
                .desgloseCongelado(detalle.getDesgloseCongelado())
                .build();
        segundo.setReserva(reserva);
        reserva.getDetalles().add(segundo);

        BigDecimal total = service.calcularTotalReserva(reserva);

        assertEquals(0, total.scale());
        assertEquals(new BigDecimal("489126"), total);
    }
}
