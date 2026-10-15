package com.hotelcaciquebolivar.reservas.config.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;

/**
 * Configuración automática de la capa de pago.
 *
 * <p>Habilita la vinculación de propiedades de configuración para las
 * clases {@link PaymentProperties} y {@link ReservationProperties},
 * permitiendo inyectar configuraciones de pasarelas de pago y reglas
 * de negocio de reservas en cualquier bean de Spring.</p>
 *
 * @see PaymentProperties
 * @see ReservationProperties
 */
@Configuration
@EnableConfigurationProperties({PaymentProperties.class, ReservationProperties.class})
public class PaymentGatewayAutoConfig {
}
