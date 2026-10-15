package com.hotelcaciquebolivar.reservas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Propiedades de configuración de cookies de autenticación.
 *
 * <p>Vincula las propiedades bajo el prefijo {@code app.cookie} en
 * {@code application.yml} a esta clase tipada. Se usa para controlar
 * el comportamiento de las cookies de sesión JWT.</p>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@Data
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {

    /**
     * Indica si las cookies de autenticación deben marcarse como Secure.
     * dev/test → false, prod → true.
     * Mapea a ${COOKIE_SECURE} vía application.yml para compatibilidad con .env legacy.
     */
    private boolean secure = false;
}
