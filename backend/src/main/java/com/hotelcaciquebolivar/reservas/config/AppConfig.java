package com.hotelcaciquebolivar.reservas.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;
import com.hotelcaciquebolivar.reservas.mail.EmailProperties;

/**
 * Configuración principal de la aplicación.
 *
 * <p>Clase central que habilita la vinculación de propiedades de configuración
 * personalizadas definidas en archivos YAML/properties a sus clases POJO
 * correspondientes. Esto permite inyectar configuraciones tipadas en cualquier
 * bean de Spring mediante {@code @Autowired} o constructor injection.</p>
 *
 * <p>Propiedades habilitadas:</p>
 * <ul>
 *   <li>{@link CookieProperties} — configuración de cookies de autenticación</li>
 *   <li>{@link PaymentProperties} — configuración de pasarelas de pago (Wompi)</li>
 *   <li>{@link ReservationProperties} — configuración de reglas de negocio de reservas</li>
 *   <li>{@link EmailProperties} — configuración de correo electrónico</li>
 * </ul>
 *
 * @see CookieProperties
 * @see PaymentProperties
 * @see ReservationProperties
 */
@Configuration
@EnableConfigurationProperties({
        CookieProperties.class,
        PaymentProperties.class,
        ReservationProperties.class,
        EmailProperties.class,
        BackupProperties.class
})
public class AppConfig {
}
