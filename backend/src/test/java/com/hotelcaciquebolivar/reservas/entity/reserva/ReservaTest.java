package com.hotelcaciquebolivar.reservas.entity.reserva;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

class ReservaTest {

    private Reserva reserva;
    private Habitacion habitacion;
    private DetalleReserva detalle;

    @BeforeEach
    void setUp() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setId(1L);
        tipo.setNombre("Suite");
        tipo.setCapacidadBase(2);
        tipo.setCapacidadMaxima(4);
        tipo.setPrecioBase(new BigDecimal("200"));
        tipo.setPrecioPersonaExtra(new BigDecimal("50"));

        habitacion = new Habitacion();
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
                .precioPorNoche(new BigDecimal("200"))
                .cantidadOcupantes(2)
                .build();
    }

    @Test
    void calcularNoches_deberiaRetornarDiferenciaEnDias() {
        assertEquals(3, reserva.calcularNoches());
    }

    @Test
    void calcularNoches_conFechasIguales_deberiaRetornarUno() {
        reserva.setFechaCheckIn(LocalDate.of(2026, 6, 10));
        reserva.setFechaCheckOut(LocalDate.of(2026, 6, 10));
        assertEquals(1, reserva.calcularNoches());
    }

    @Test
    void recalcularTotal_conDetalles_deberiaSumarSubtotales() {
        reserva.agregarDetalle(detalle);
        reserva.recalcularTotal();
        assertEquals(new BigDecimal("600"), reserva.getTotal());
    }

    @Test
    void recalcularTotal_conPersonasExtra_deberiaIncluirCargoAdicional() {
        detalle.setCantidadOcupantes(3);
        reserva.agregarDetalle(detalle);
        reserva.recalcularTotal();
        assertEquals(new BigDecimal("750"), reserva.getTotal());
    }

    @Test
    void recalcularTotal_sinDetalles_deberiaSerCero() {
        reserva.recalcularTotal();
        assertEquals(BigDecimal.ZERO, reserva.getTotal());
    }

    @Test
    void recalcularTotal_conPrecioFraccionario_deberiaRedondearAEntero() {
        detalle.setPrecioPorNoche(new BigDecimal("200.50"));
        reserva.agregarDetalle(detalle);
        reserva.recalcularTotal();
        // 601,50 → HALF_UP a peso entero
        assertEquals(new BigDecimal("602"), reserva.getTotal());
    }

    @Test
    void confirmar_desdePendiente_deberiaCambiarAConfirmada() {
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.confirmar();
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void confirmar_desdeConfirmada_deberiaLanzarExcepcion() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        assertThrows(IllegalStateException.class, () -> reserva.confirmar());
    }

    @Test
    void cancelar_desdePendiente_deberiaCambiarACancelada() {
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.cancelar();
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void cancelar_desdeFinalizada_deberiaLanzarExcepcion() {
        reserva.setEstado(EstadoReserva.FINALIZADA);
        assertThrows(IllegalStateException.class, () -> reserva.cancelar());
    }

    @Test
    void cancelar_desdeCancelada_deberiaLanzarExcepcion() {
        reserva.setEstado(EstadoReserva.CANCELADA);
        assertThrows(IllegalStateException.class, () -> reserva.cancelar());
    }

    @Test
    void marcarNoShow_desdeConfirmada_deberiaCambiarANoShow() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.marcarNoShow();
        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
    }

    @Test
    void marcarNoShow_desdeEnCurso_deberiaLanzarExcepcion() {
        reserva.setEstado(EstadoReserva.EN_CURSO);
        assertThrows(IllegalStateException.class, () -> reserva.marcarNoShow());
    }

    @Test
    void agregarDetalle_conHabitacionRepetida_deberiaLanzarExcepcion() {
        reserva.agregarDetalle(detalle);
        DetalleReserva detalle2 = DetalleReserva.builder()
                .habitacion(habitacion)
                .precioPorNoche(new BigDecimal("200"))
                .cantidadOcupantes(2)
                .build();
        assertThrows(IllegalStateException.class, () -> reserva.agregarDetalle(detalle2));
    }

    @Test
    void validarFechas_conFechasValidas_noDebeLanzarExcepcion() {
        reserva.setFechaCheckIn(LocalDate.of(2026, 6, 10));
        reserva.setFechaCheckOut(LocalDate.of(2026, 6, 13));
        assertDoesNotThrow(() -> reserva.validarFechas());
    }

    @Test
    void validarFechas_conCheckOutAntesDeCheckIn_deberiaLanzarExcepcion() {
        reserva.setFechaCheckIn(LocalDate.of(2026, 6, 13));
        reserva.setFechaCheckOut(LocalDate.of(2026, 6, 10));
        assertThrows(IllegalStateException.class, () -> reserva.validarFechas());
    }

    @Test
    void validarFechas_conCheckOutNulo_deberiaLanzarExcepcion() {
        reserva.setFechaCheckIn(LocalDate.of(2026, 6, 10));
        reserva.setFechaCheckOut(null);
        assertThrows(IllegalStateException.class, () -> reserva.validarFechas());
    }

    @Test
    void validarTotal_conTotalNegativo_deberiaLanzarExcepcion() {
        reserva.setTotal(new BigDecimal("-1"));
        assertThrows(IllegalStateException.class, () -> reserva.validarTotal());
    }

    @Test
    void validarTotal_conTotalCero_noDebeLanzarExcepcion() {
        reserva.setTotal(BigDecimal.ZERO);
        assertDoesNotThrow(() -> reserva.validarTotal());
    }

    @Test
    void codigoReserva_deberiaGenerarseEnPrePersist() {
        Reserva nueva = Reserva.builder().build();
        nueva.onCreate();
        assertNotNull(nueva.getCodigoReserva());
        assertTrue(nueva.getCodigoReserva().matches("\\d{4}-[A-Z0-9]{8}"));
    }

    @Test
    void builder_deberiaCrearReservaConValoresPorDefecto() {
        Reserva r = Reserva.builder().build();
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(OrigenReserva.RECEPCION, r.getOrigen());
        assertNotNull(r.getDetalles());
        assertTrue(r.getDetalles().isEmpty());
    }

    @Test
    void removerDetalle_deberiaEliminarDetalle() {
        reserva.agregarDetalle(detalle);
        reserva.removerDetalle(detalle);
        assertTrue(reserva.getDetalles().isEmpty());
        assertNull(detalle.getReserva());
    }
}
