package com.hotelcaciquebolivar.reservas.service.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

/**
 * Interfaz para el cálculo del anticipo requerido para confirmar una reserva.
 * Define la estrategia de cálculo del monto que el cliente debe pagar como adelanto.
 *
 * @author Hotel Cacique Bolívar
 */
public interface AnticipoCalculator {

    /**
     * Calcula el monto del anticipo para una reserva dada.
     *
     * @param reserva la reserva para la cual se calcula el anticipo
     * @return monto del anticipo en la moneda de la reserva
     */
    BigDecimal calcularAnticipo(Reserva reserva);
}
