package com.hotelcaciquebolivar.reservas.config.reservations;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Propiedades de configuración de reglas de negocio para reservas.
 *
 * <p>Vincula las propiedades bajo el prefijo {@code app.reservations} en
 * {@code application.yml}. Define parámetros que controlan el comportamiento
 * del sistema de reservas.</p>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "app.reservations")
@Data
public class ReservationProperties {

    /**
     * Minutos que una reserva en PENDIENTE_PAGO permanece vigente antes de expirar.
     * Configurable vía PAYMENT_EXPIRATION_MINUTES, default 20.
     */
    private int paymentExpirationMinutes = 20;
}
