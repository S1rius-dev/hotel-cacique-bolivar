package com.hotelcaciquebolivar.reservas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validador de variables de entorno críticas en producción.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Ordered#HIGHEST_PRECEDENCE} para
 * ejecutarse lo antes posible durante el arranque. Activo exclusivamente en el perfil
 * {@code prod}. Verifica que las variables de entorno obligatorias estén configuradas
 * y tengan valores válidos antes de que la aplicación procese peticiones.</p>
 *
 * <p>Variables obligatorias validadas:</p>
 * <ul>
 *   <li>{@code JWT_SECRET} — clave secreta JWT (Base64, mínimo 32 bytes decodificados)</li>
 *   <li>{@code DB_PASSWORD} — contraseña de la base de datos</li>
 *   <li>{@code MAIL_PASSWORD} — contraseña del servidor de correo</li>
 *   <li>{@code WOMPI_PRIVATE_KEY} / {@code WOMPI_EVENTS_KEY} — solo si la pasarela activa es WOMPI</li>
 *   <li>{@code BOLD_IDENTITY_KEY} / {@code BOLD_SECRET_KEY} — solo si la pasarela activa es BOLD</li>
 *   <li>{@code ADMIN_EMAIL} — correo del administrador</li>
 *   <li>{@code ADMIN_PASSWORD} — contraseña del administrador</li>
 * </ul>
 *
 * <p>La validación de credenciales de pasarela es condicional: solo exige las claves
 * de la pasarela configurada en {@code app.payments.gateway}, permitiendo operar con
 * Bold sin tener secretos de Wompi y viceversa.</p>
 *
 * <p>Si falta alguna variable, aborta el arranque con un {@link IllegalStateException}
 * descriptivo. Esta validación es más estricta que {@link ProfileValidator} porque
 * en producción no se permite el arranque incompleto.</p>
 *
 * @see ProfileEnvironmentPostProcessor
 * @see ProfileValidator
 */
@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProdEnvironmentValidator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdEnvironmentValidator.class);

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.payments.gateway:WOMPI}")
    private String paymentsGateway;

    @Value("${app.payments.wompi.private-key:}")
    private String wompiPrivateKey;

    @Value("${app.payments.wompi.events-key:}")
    private String wompiEventsKey;

    @Value("${app.payments.bold.identity-key:}")
    private String boldIdentityKey;

    @Value("${app.payments.bold.secret-key:}")
    private String boldSecretKey;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    /**
     * Ejecuta la validación de variables de entorno al arrancar la aplicación.
     *
     * <p>Verifica la existencia y validez de cada variable obligatoria. Para
     * {@code JWT_SECRET} realiza una validación adicional decodificando Base64
     * y comprobando que el resultado tenga al menos 32 bytes (256 bits).</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     * @throws IllegalStateException si falta alguna variable o JWT_SECRET no cumple
     *         los requisitos de longitud o formato Base64
     */
    @Override
    public void run(String... args) {
        StringBuilder missing = new StringBuilder();

        if (isBlank(jwtSecret)) {
            missing.append(" JWT_SECRET");
        }
        if (isBlank(dbPassword)) {
            missing.append(" DB_PASSWORD");
        }
        if (isBlank(mailPassword)) {
            missing.append(" MAIL_PASSWORD");
        }
        String gatewayNormalized = paymentsGateway != null ? paymentsGateway.trim().toUpperCase() : "WOMPI";
        boolean isWompi = "WOMPI".equals(gatewayNormalized);
        boolean isBold = "BOLD".equals(gatewayNormalized);

        if (isWompi) {
            if (isBlank(wompiPrivateKey)) {
                missing.append(" WOMPI_PRIVATE_KEY");
            }
            if (isBlank(wompiEventsKey)) {
                missing.append(" WOMPI_EVENTS_KEY");
            }
        } else if (isBold) {
            if (isBlank(boldIdentityKey)) {
                missing.append(" BOLD_IDENTITY_KEY");
            }
            if (isBlank(boldSecretKey)) {
                missing.append(" BOLD_SECRET_KEY");
            }
        } else {
            // Pasarela desconocida: validar ambas familias para fail-fast explícito
            if (isBlank(wompiPrivateKey) && isBlank(boldIdentityKey)) {
                missing.append(" WOMPI_PRIVATE_KEY|BOLD_IDENTITY_KEY");
            }
        }
        if (isBlank(adminEmail)) {
            missing.append(" ADMIN_EMAIL");
        }
        if (isBlank(adminPassword)) {
            missing.append(" ADMIN_PASSWORD");
        }

        if (missing.length() > 0) {
            String msg = "[PROD][FAIL-FAST] Faltan variables críticas en prod:" + missing
                    + ". Configura las variables de entorno de Windows Server antes de arrancar.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        // Validación adicional: JWT debe ser Base64 >=32 bytes (igual que JwtService)
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(jwtSecret);
            if (decoded.length < 32) {
                throw new IllegalStateException("[PROD][FAIL-FAST] JWT_SECRET debe tener al menos 256 bits (32 bytes) tras decodificar Base64. Longitud actual: " + decoded.length);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("[PROD][FAIL-FAST] JWT_SECRET no es Base64 válida: " + e.getMessage(), e);
        }

        log.info("[PROD][VALIDATOR] Validación de entorno prod OK");
    }

    /**
     * Verifica si una cadena es {@code null} o está en blanco.
     *
     * @param s cadena a evaluar
     * @return {@code true} si la cadena es {@code null}, vacía o contiene solo espacios
     */
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
