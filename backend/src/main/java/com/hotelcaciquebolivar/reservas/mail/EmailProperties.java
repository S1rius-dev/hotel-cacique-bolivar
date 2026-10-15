package com.hotelcaciquebolivar.reservas.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase de propiedades de configuración para el envío de correos electrónicos del hotel.
 *
 * <p>Estas propiedades se cargan desde el prefijo {@code app.mail} en los archivos
 * de configuración de Spring Boot (application.yml / application.properties).
 * Se encarga de centralizar los datos necesarios para el envío de correos,
 * como la dirección del remitente, el nombre del remitente, la URL base
 * del sitio web y la URL del logo del hotel.</p>
 *
 * <p>Ejemplo de configuración:</p>
 * <pre>
 * app:
 *   mail:
 *     from: reservas@example.com
 *     sender-name: Hotel Cacique Bolívar
 *     base-url: https://demo.example.com
 *     logo-url: https://demo.example.com/images/logo.png
 * </pre>
 */
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

    /**
     * Dirección de correo electrónico del remitente utilizado en el envío de correos.
     */
    private String from;
    /**
     * Nombre que se muestra como remitente en los correos electrónicos enviados.
     */
    private String senderName;

    /**
     * URL base del sitio web del hotel, utilizada para construir enlaces dentro de los correos.
     */
    private String baseUrl;

    /**
     * URL de la imagen del logo del hotel, utilizada en las plantillas de correo.
     */
    private String logoUrl;

    /**
     * Obtiene la dirección de correo del remitente.
     *
     * @return dirección de correo electrónico del remitente
     */
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    /**
     * Obtiene el nombre del remitente que se muestra en los correos.
     *
     * @return nombre del remitente
     */
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    /**
     * Obtiene la URL base del sitio web del hotel.
     *
     * @return URL base del hotel
     */
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    /**
     * Obtiene la URL del logo del hotel para su uso en las plantillas de correo.
     *
     * @return URL de la imagen del logo
     */
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
