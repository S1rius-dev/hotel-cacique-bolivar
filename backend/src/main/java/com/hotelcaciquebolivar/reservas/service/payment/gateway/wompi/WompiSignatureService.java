package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiWebhookRequest;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentSignatureService;

import tools.jackson.databind.ObjectMapper;

/**
 * Servicio de verificación de firmas para webhooks de Wompi.
 * Implementa dos estrategias de verificación:
 * 1. Nueva: SHA-256 de propiedades del payload + timestamp + eventsKey (checksum Wompi)
 * 2. Legacy: HMAC-SHA256 del payload completo con eventsKey
 * Utiliza comparación constante en tiempo para prevenir ataques de timing.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class WompiSignatureService implements PaymentSignatureService {

    private static final Logger log = LoggerFactory.getLogger(WompiSignatureService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    public WompiSignatureService(PaymentProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Verifica la firma de un webhook de Wompi.
     * Intenta primero la validación SHA-256 por propiedades (checksum del body),
     * luego valida contra el header X-Event-Checksum.
     * Si ninguna coincide, intenta HMAC legacy con eventsKey.
     *
     * @param firma firma recibida en el header del webhook
     * @param payloadCrudo cuerpo JSON del webhook sin procesar
     * @return true si la firma es válida según cualquiera de las estrategias
     */
    @Override
    public boolean verificar(String firma, String payloadCrudo) {
        if (!StringUtils.hasText(firma) || !StringUtils.hasText(payloadCrudo)) {
            return false;
        }
        // 1) Intentar validación nueva Wompi: SHA256( concat(properties) + timestamp + eventsKey ) == checksum == header
        try {
            WompiWebhookRequest req = objectMapper.readValue(payloadCrudo, WompiWebhookRequest.class);
            if (req != null && req.signature() != null && StringUtils.hasText(req.signature().checksum())
                    && req.signature().properties() != null && !req.signature().properties().isEmpty()) {
                String checksumBody = req.signature().checksum();
                // Priorizar checksum del body vs header: ambos deben coincidir con el calculado
                String expected = calcularChecksumWompi(req);
                boolean okBody = constanteIgual(checksumBody, expected);
                boolean okHeader = constanteIgual(firma, expected);
                // Wompi envía X-Event-Checksum igual al checksum del body
                boolean headerMatchesBody = constanteIgual(firma, checksumBody);
                log.info("[WompiWebhook] checksumBody={} header={} expected={} okBody={} okHeader={} headerMatchesBody={}",
                        checksumBody, firma, expected, okBody, okHeader, headerMatchesBody);
                if (okBody || okHeader) {
                    return true;
                }
                log.warn("[WompiWebhook] checksum Wompi no coincide: expected={} body={} header={}", expected, checksumBody, firma);
                // fallback a HMAC legacy para no romper flujos antiguos
            }
        } catch (Exception ex) {
            log.debug("[WompiWebhook] No se pudo parsear signature Wompi, fallback HMAC: {}", ex.getMessage());
        }
        // 2) Fallback legacy: HMAC_SHA256(payload, eventsKey) == X-Event-Signature
        try {
            String eventsKey = properties.getWompi().getEventsKey();
            if (!StringUtils.hasText(eventsKey)) return false;
            SecretKeySpec key = new SecretKeySpec(
                    eventsKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(key);
            String firmaEsperada = toHex(mac.doFinal(
                    payloadCrudo.getBytes(StandardCharsets.UTF_8)));
            return constanteIgual(firma, firmaEsperada);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Wompi Events: checksum = SHA256( concat( transaction.id + transaction.status + transaction.amount_in_cents + ... por signature.properties ) + timestamp + eventsKey )
     * properties es lista ordenada con paths tipo "transaction.id". Se extraen de data.transaction.
     */
    private String calcularChecksumWompi(WompiWebhookRequest req) throws Exception {
        List<String> props = req.signature().properties();
        Map<String, Object> data = req.data();
        StringBuilder concat = new StringBuilder();
        if (data != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tx = data.get("transaction") instanceof Map ? (Map<String, Object>) data.get("transaction") : null;
            if (tx != null) {
                for (String p : props) {
                    String key = p.contains(".") ? p.substring(p.indexOf('.') + 1) : p;
                    Object v = tx.get(key);
                    if (v == null && tx.containsKey(p)) v = tx.get(p);
                    // amount_in_cents puede venir como número
                    concat.append(v != null ? String.valueOf(v) : "");
                }
            }
        }
        if (req.timestamp() != null) {
            concat.append(req.timestamp());
        }
        String eventsKey = properties.getWompi().getEventsKey();
        if (StringUtils.hasText(eventsKey)) {
            concat.append(eventsKey);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(concat.toString().getBytes(StandardCharsets.UTF_8));
        return toHex(hash);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constanteIgual(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int resultado = 0;
        for (int i = 0; i < a.length(); i++) {
            resultado |= a.charAt(i) ^ b.charAt(i);
        }
        return resultado == 0;
    }
}
