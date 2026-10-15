package com.hotelcaciquebolivar.reservas.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

/**
 * Implementación de {@link AnticipoCalculator} que calcula el anticipo
 * como el valor de una noche de estadía (total dividido entre noches).
 * Si la reserva tiene 0 noches, retorna el total completo.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class AnticipoPrimeraNocheCalculator implements AnticipoCalculator {

    /**
     * {@inheritDoc}
     * Calcula el valor por noche dividiendo el total entre el número de noches.
     * Redondea a 2 decimales con HALF_UP.
     */
    @Override
    public BigDecimal calcularAnticipo(Reserva reserva) {
        BigDecimal total = reserva.getTotal() != null
                ? reserva.getTotal()
                : BigDecimal.ZERO;
        long noches = reserva.calcularNoches();
        if (noches <= 0) {
            return total;
        }
        BigDecimal valorNoche = total.divide(
                BigDecimal.valueOf(noches), 2, RoundingMode.HALF_UP);
        return valorNoche.multiply(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);
    }
}
