package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;

/**
 * Genera la firma de integridad que el Checkout Widget de Wompi valida
 * (Paso 3 de la integración de checkout).
 * <p>
 * Según la documentación oficial de Wompi (Widget & Checkout Web — Step 3):
 * {@code SHA256(reference + amountInCents + currency + integritySecret)}.
 * Cuando se usa {@code expirationTime}, se concatena como
 * {@code reference + amountInCents + currency + expirationTime + integritySecret}.
 * <p>
 * IMPORTANTE: No es HMAC, no lleva separadores ":" y NO usa el eventsKey.
 * Ver: https://docs.wompi.co/en/docs/colombia/widget-checkout-web/
 */
@Component
public class WompiIntegritySignatureGenerator {

    private static final Logger log = LoggerFactory.getLogger(WompiIntegritySignatureGenerator.class);

    private final PaymentProperties properties;

    public WompiIntegritySignatureGenerator(PaymentProperties properties) {
        this.properties = properties;
    }

    /**
     * Sin expirationTime (caso habitual).
     */
    public String generar(String reference, long amountInCents, String currency) {
        return generar(reference, amountInCents, currency, null);
    }

    /**
     * Con expirationTime opcional (ISO8601 UTC+0000). Si es null/blank se ignora.
     */
    public String generar(String reference, long amountInCents, String currency, String expirationTime) {
        String integrityKey = properties.getWompi().getIntegrityKey();
        if (!StringUtils.hasText(integrityKey)) {
            // Fallback temporal para no romper entornos que aún usan eventsKey como secret
            // pero loguear advertencia: debe configurarse WOMPI_INTEGRITY_KEY
            integrityKey = properties.getWompi().getEventsKey();
        }
        if (!StringUtils.hasText(integrityKey)) {
            throw new IllegalStateException(
                    "No se configuró WOMPI_INTEGRITY_KEY (integrity secret). "
                    + "Configure app.payments.wompi.integrity-key con el valor prod_integrity_... / test_integrity_... del Dashboard Wompi");
        }

        String cadena;
        if (StringUtils.hasText(expirationTime)) {
            cadena = reference + amountInCents + currency + expirationTime + integrityKey;
        } else {
            cadena = reference + amountInCents + currency + integrityKey;
        }

        // DEBUG: loguear sin exponer secret completo — útil para cazar 403 CloudFront
        if (log.isDebugEnabled()) {
            String secretHint = integrityKey.length() > 8
                    ? integrityKey.substring(0, 8) + "***" + integrityKey.substring(integrityKey.length() - 4)
                    : "***";
            log.debug("[WompiFirma] reference={} amountInCents={} currency={} expirationTime={} secretHint={} cadenaLen={}",
                    reference, amountInCents, currency, expirationTime, secretHint, cadena.length());
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadena.getBytes(StandardCharsets.UTF_8));
            String hex = toHex(hash);
            log.info("[WompiFirma] reference={} amountInCents={} currency={} signature={} effectiveBaseUrl={}",
                    reference, amountInCents, currency, hex, properties.getWompi().getEffectiveBaseUrl());
            return hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "No se pudo generar la firma de integridad de Wompi (SHA-256 no disponible)", ex);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
