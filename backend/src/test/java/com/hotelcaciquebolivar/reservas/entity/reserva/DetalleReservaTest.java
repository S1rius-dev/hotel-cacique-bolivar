package com.hotelcaciquebolivar.reservas.entity.reserva;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

class DetalleReservaTest {

    private DetalleReserva detalle;
    private Habitacion habitacion;
    private Reserva reserva;
    private Cliente cliente;

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

        TipoIdentificacion tipoId = new TipoIdentificacion();
        tipoId.setId(1L);
        tipoId.setNombre("CC");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombreCompleto("Juan");
        cliente.setApellidoCompleto("Perez");
        cliente.setCedula("1234567890");
        cliente.setTipoIdentificacion(tipoId);

        reserva = Reserva.builder()
                .fechaCheckIn(LocalDate.of(2026, 6, 10))
                .fechaCheckOut(LocalDate.of(2026, 6, 13))
                .build();

        detalle = DetalleReserva.builder()
                .habitacion(habitacion)
                .precioPorNoche(new BigDecimal("200"))
                .cantidadOcupantes(3)
                .build();
        detalle.setReserva(reserva);
    }

    @Test
    void calcularSubtotal_conNochesValidas_deberiaRetornarPrecioPorNoches() {
        BigDecimal subtotal = detalle.calcularSubtotal(3);
        assertEquals(new BigDecimal("600"), subtotal);
    }

    @Test
    void calcularSubtotal_conNochesCero_deberiaRetornarCero() {
        BigDecimal subtotal = detalle.calcularSubtotal(0);
        assertEquals(BigDecimal.ZERO, subtotal);
    }

    @Test
    void calcularSubtotal_sinParametros_deberiaUsarNochesDeReserva() {
        BigDecimal subtotal = detalle.calcularSubtotal();
        assertEquals(new BigDecimal("600"), subtotal);
    }

    @Test
    void calcularTotal_conPersonasExtra_deberiaIncluirCargoExtra() {
        BigDecimal total = detalle.calcularTotal();
        assertEquals(new BigDecimal("750"), total);
    }

    @Test
    void calcularTotal_sinPersonasExtra_deberiaIgualAlSubtotal() {
        detalle.setCantidadOcupantes(2);
        BigDecimal total = detalle.calcularTotal();
        assertEquals(new BigDecimal("600"), total);
    }

    @Test
    void calcularTotal_sinReserva_deberiaRetornarCero() {
        detalle.setReserva(null);
        assertEquals(BigDecimal.ZERO, detalle.calcularTotal());
    }

    @Test
    void calcularSubtotal_sinReserva_deberiaRetornarCero() {
        detalle.setReserva(null);
        assertEquals(BigDecimal.ZERO, detalle.calcularSubtotal());
    }

    @Test
    void validarCapacidad_conOcupantesValidos_noDebeLanzarExcepcion() {
        assertDoesNotThrow(() -> detalle.validarCapacidad());
    }

    @Test
    void validarCapacidad_conOcupantesExcedidos_deberiaLanzarExcepcion() {
        detalle.setCantidadOcupantes(5);
        assertThrows(IllegalStateException.class, () -> detalle.validarCapacidad());
    }

    @Test
    void validarCapacidad_conOcupantesCero_deberiaLanzarExcepcion() {
        detalle.setCantidadOcupantes(0);
        assertThrows(IllegalStateException.class, () -> detalle.validarCapacidad());
    }

    @Test
    void validarCapacidad_conOcupantesNulo_deberiaLanzarExcepcion() {
        detalle.setCantidadOcupantes(null);
        assertThrows(IllegalStateException.class, () -> detalle.validarCapacidad());
    }

    @Test
    void agregarHuesped_deberiaAgregarCorrectamente() {
        detalle.agregarHuesped(cliente);
        assertEquals(1, detalle.getHuespedes().size());
        assertEquals(cliente.getId(), detalle.getHuespedes().get(0).getCliente().getId());
    }

    @Test
    void agregarHuesped_duplicado_deberiaLanzarExcepcion() {
        detalle.agregarHuesped(cliente);
        assertThrows(IllegalStateException.class, () -> detalle.agregarHuesped(cliente));
    }

    @Test
    void agregarHuesped_excedeLimite_deberiaLanzarExcepcion() {
        detalle.setCantidadOcupantes(1);
        detalle.agregarHuesped(cliente);
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombreCompleto("Maria");
        assertThrows(IllegalStateException.class, () -> detalle.agregarHuesped(cliente2));
    }

    @Test
    void realizarCheckIn_desdeReservada_deberiaCambiarAOcupada() {
        detalle.setEstado(EstadoDetalleReserva.RESERVADA);
        detalle.realizarCheckIn();
        assertEquals(EstadoDetalleReserva.OCUPADA, detalle.getEstado());
    }

    @Test
    void realizarCheckIn_desdeOcupada_deberiaSerIdempotente() {
        detalle.setEstado(EstadoDetalleReserva.OCUPADA);
        detalle.realizarCheckIn();
        assertEquals(EstadoDetalleReserva.OCUPADA, detalle.getEstado());
    }

    @Test
    void realizarCheckOut_desdeOcupada_deberiaCambiarALiberada() {
        detalle.setEstado(EstadoDetalleReserva.OCUPADA);
        detalle.realizarCheckOut();
        assertEquals(EstadoDetalleReserva.LIBERADA, detalle.getEstado());
    }

    @Test
    void realizarCheckOut_desdeReservada_deberiaLanzarExcepcion() {
        detalle.setEstado(EstadoDetalleReserva.RESERVADA);
        assertThrows(IllegalStateException.class, () -> detalle.realizarCheckOut());
    }

    @Test
    void cancelar_desdeReservada_deberiaCambiarACancelada() {
        detalle.setEstado(EstadoDetalleReserva.RESERVADA);
        detalle.cancelar();
        assertEquals(EstadoDetalleReserva.CANCELADA, detalle.getEstado());
    }

    @Test
    void cancelar_desdeLiberada_deberiaLanzarExcepcion() {
        detalle.setEstado(EstadoDetalleReserva.LIBERADA);
        assertThrows(IllegalStateException.class, () -> detalle.cancelar());
    }
}
