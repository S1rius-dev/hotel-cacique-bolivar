package com.hotelcaciquebolivar.reservas.service.payment;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

class AnticipoPrimeraNocheCalculatorTest {

    private final AnticipoPrimeraNocheCalculator calculator =
            new AnticipoPrimeraNocheCalculator();

    private Reserva reserva(BigDecimal total, long noches) {
        return Reserva.builder()
                .total(total)
                .fechaCheckIn(java.time.LocalDate.of(2026, 8, 1))
                .fechaCheckOut(java.time.LocalDate.of(2026, 8, 1).plusDays(noches))
                .build();
    }

    @Test
    void calculaPrimeraNocheDeTotalYPnoches() {
        Reserva r = reserva(new BigDecimal("300000.00"), 3);
        assertEquals(new BigDecimal("100000.00"), calculator.calcularAnticipo(r));
    }

    @Test
    void redondeaHaciaArriba() {
        Reserva r = reserva(new BigDecimal("100000.00"), 3);
        assertEquals(new BigDecimal("33333.33"), calculator.calcularAnticipo(r));
    }

    @Test
    void conNochesCeroDevuelveTotal() {
        Reserva r = reserva(new BigDecimal("100000.00"), 0);
        assertEquals(new BigDecimal("100000.00"), calculator.calcularAnticipo(r));
    }

    @Test
    void totalNuloDevuelveCero() {
        Reserva r = reserva(null, 2);
        assertEquals(0, calculator.calcularAnticipo(r).compareTo(BigDecimal.ZERO));
    }
}
