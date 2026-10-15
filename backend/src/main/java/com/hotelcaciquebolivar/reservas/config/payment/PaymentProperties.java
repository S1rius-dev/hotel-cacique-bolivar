package com.hotelcaciquebolivar.reservas.config.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Propiedades de configuración del sistema de pagos.
 *
 * <p>Vincula las propiedades bajo el prefijo {@code app.payments} en
 * {@code application.yml}. Define el tipo de pasarela de pago, la moneda
 * utilizada y la configuración específica de cada pasarela (Wompi, Bold).</p>
 *
 * <p>Cada pasarela tiene su propio bloque de credenciales; solo las de la
 * pasarela activa son requeridas al arrancar. Ver {@link com.hotelcaciquebolivar.reservas.config.ProdEnvironmentValidator}.</p>
 *
 * @see Wompi
 * @see Bold
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "app.payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProperties {

    private PaymentGatewayType gateway = PaymentGatewayType.WOMPI;
    private String moneda = "COP";
    private Wompi wompi = new Wompi();
    private Bold bold = new Bold();

    /**
     * Obtiene el tipo de pasarela de pago configurada.
     *
     * @return tipo de pasarela de pago (por defecto {@link PaymentGatewayType#WOMPI})
     */
    public PaymentGatewayType getGateway() {
        return gateway;
    }

    /**
     * Establece el tipo de pasarela de pago.
     *
     * @param gateway tipo de pasarela de pago a utilizar
     */
    public void setGateway(PaymentGatewayType gateway) {
        this.gateway = gateway;
    }

    /**
     * Obtiene la moneda utilizada para las transacciones.
     *
     * @return código de moneda ISO (por defecto "COP")
     */
    public String getMoneda() {
        return moneda;
    }

    /**
     * Establece la moneda para las transacciones.
     *
     * @param moneda código de moneda ISO a utilizar
     */
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    /**
     * Obtiene la configuración específica de Wompi.
     *
     * @return configuración de Wompi
     */
    public Wompi getWompi() {
        return wompi;
    }

    /**
     * Establece la configuración específica de Wompi.
     *
     * @param wompi configuración de Wompi a utilizar
     */
    public void setWompi(Wompi wompi) {
        this.wompi = wompi;
    }

    /**
     * Obtiene la configuración específica de Bold.
     *
     * @return configuración de Bold
     */
    public Bold getBold() {
        return bold;
    }

    /**
     * Establece la configuración específica de Bold.
     *
     * @param bold configuración de Bold a utilizar
     */
    public void setBold(Bold bold) {
        this.bold = bold;
    }

    /**
     * Configuración específica de la pasarela de pago Wompi.
     *
     * <p>Contiene las claves de acceso, URLs de redirección y webhook,
     * y la lógica de auto-detección del entorno (sandbox vs producción)
     * basada en el prefijo de la clave pública.</p>
     */
    public static class Wompi {

        private String publicKey;
        private String privateKey;
        private String eventsKey;
        private String integrityKey;
        private String baseUrl;
        private String redirectUrl;
        private String webhookUrl;

        /**
         * Obtiene la URL base efectiva de Wompi con auto-detección de entorno.
         *
         * <p>Si {@code baseUrl} está configurada explícitamente, la retorna directamente
         * (con corrección automática si la URL apunta a producción pero la clave es
         * de sandbox). Si no está configurada, auto-detecta el entorno según el
         * prefijo de la clave pública:</p>
         * <ul>
         *   <li>{@code pub_test*} → sandbox ({@code https://sandbox.wompi.co/v1})</li>
         *   <li>{@code pub_prod*} u otra → producción ({@code https://production.wompi.co/v1})</li>
         * </ul>
         *
         * @return URL base de la API de Wompi para el entorno detectado
         */
        public String getEffectiveBaseUrl() {
            if (baseUrl != null && !baseUrl.isBlank()) {
                // heurística: si es test key pero URL apunta a prod, corregir a sandbox y logear
                if (publicKey != null && publicKey.startsWith("pub_test")
                        && baseUrl.contains("production.wompi.co")) {
                    return "https://sandbox.wompi.co/v1";
                }
                return baseUrl;
            }
            if (publicKey != null && publicKey.startsWith("pub_test")) {
                return "https://sandbox.wompi.co/v1";
            }
            return "https://production.wompi.co/v1";
        }

        /**
         * Obtiene la clave pública de Wompi.
         *
         * @return clave pública (prefijo {@code pub_test_} para sandbox, {@code pub_prod_} para producción)
         */
        public String getPublicKey() {
            return publicKey;
        }

        /**
         * Establece la clave pública de Wompi.
         *
         * @param publicKey clave pública de Wompi
         */
        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        /**
         * Obtiene la clave privada de Wompi.
         *
         * @return clave privada para autenticación con la API
         */
        public String getPrivateKey() {
            return privateKey;
        }

        /**
         * Establece la clave privada de Wompi.
         *
         * @param privateKey clave privada de Wompi
         */
        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        /**
         * Obtiene la clave de eventos de Wompi.
         *
         * @return clave para validar webhooks de eventos
         */
        public String getEventsKey() {
            return eventsKey;
        }

        /**
         * Establece la clave de eventos de Wompi.
         *
         * @param eventsKey clave de eventos de Wompi
         */
        public void setEventsKey(String eventsKey) {
            this.eventsKey = eventsKey;
        }

        /**
         * Obtiene la clave de integridad de Wompi.
         *
         * @return clave para verificar la integridad de transacciones
         */
        public String getIntegrityKey() {
            return integrityKey;
        }

        /**
         * Establece la clave de integridad de Wompi.
         *
         * @param integrityKey clave de integridad de Wompi
         */
        public void setIntegrityKey(String integrityKey) {
            this.integrityKey = integrityKey;
        }

        /**
         * Obtiene la URL base de la API de Wompi.
         *
         * @return URL base configurada, o {@code null} si se usa auto-detección
         */
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Establece la URL base de la API de Wompi.
         *
         * @param baseUrl URL base de la API de Wompi
         */
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * Obtiene la URL de redirección post-pago.
         *
         * @return URL a la que Wompi redirige después del pago
         */
        public String getRedirectUrl() {
            return redirectUrl;
        }

        /**
         * Establece la URL de redirección post-pago.
         *
         * @param redirectUrl URL de redirección después del pago
         */
        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        /**
         * Obtiene la URL del webhook de Wompi.
         *
         * @return URL que Wompi invoca para notificar eventos de transacciones
         */
        public String getWebhookUrl() {
            return webhookUrl;
        }

        /**
         * Establece la URL del webhook de Wompi.
         *
         * @param webhookUrl URL del webhook de Wompi
         */
        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
    }

    /**
     * Configuración específica de la pasarela de pago Bold.
     *
     * <p>Contiene las claves y URLs necesarias para la integración con Bold.
     * <ul>
     *   <li>{@code identityKey} → {@code BOLD_IDENTITY_KEY} (llave de identidad / api-key del botón)</li>
     *   <li>{@code secretKey} → {@code BOLD_SECRET_KEY} (llave secreta para hash de integridad y firma webhook)</li>
     * </ul>
     * Ningún campo es obligatorio si la pasarela activa no es Bold.</p>
     */
    public static class Bold {

        private String publicKey;
        private String privateKey;
        private String identityKey;
        private String secretKey;
        private String baseUrl;
        private String redirectUrl;
        private String webhookUrl;

        /**
         * Obtiene la URL base efectiva de Bold.
         * Si no está configurada retorna la URL por defecto de Bold sandbox/prod.
         *
         * @return URL base de la API de Bold
         */
        public String getEffectiveBaseUrl() {
            if (baseUrl != null && !baseUrl.isBlank()) {
                return baseUrl;
            }
            return "https://api.bold.co";
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getIdentityKey() {
            return identityKey;
        }

        public void setIdentityKey(String identityKey) {
            this.identityKey = identityKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
    }
}
