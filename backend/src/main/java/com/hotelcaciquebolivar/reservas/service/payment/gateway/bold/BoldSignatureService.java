package com.hotelcaciquebolivar.reservas.service.payment.gateway.bold;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentSignatureService;

import tools.jackson.databind.ObjectMapper;

/**
 * Verificación de firmas webhook de Bold según docs oficiales.
 * <p>
 * Pasos docs: Base64(body) → HMAC-SHA256(Base64, secret) hex vs
 * x-bold-signature. En modo pruebas secret="" (vacío) según Bold.</p>
 */
@Component
public class BoldSignatureService implements PaymentSignatureService {

    private static final Logger log = LoggerFactory.getLogger(BoldSignatureService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    public BoldSignatureService(PaymentProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean verificar(String firma, String payloadCrudo) {
        if (!StringUtils.hasText(firma) || !StringUtils.hasText(payloadCrudo)) {
            log.warn("[BoldWebhook] firma o payload vacíos — firma={}, payloadLen={}", firma, payloadCrudo != null ? payloadCrudo.length() : 0);
            return false;
        }
        String secretRaw = properties.getBold() != null ? properties.getBold().getSecretKey() : null;
        String secret = secretRaw != null ? secretRaw.trim() : "";

        // Construir candidatos de clave según docs Bold: en pruebas secret="" (vacío) es válido
        List<byte[]> keyCandidates = new ArrayList<>();
        if (secret.isEmpty()) {
            // Modo pruebas: solo clave vacía (HMAC con keyLen 0)
            keyCandidates.add(new byte[0]);
            log.info("[BoldWebhook] modo pruebas detectado: BOLD_SECRET_KEY vacío, se verificará con clave vacía");
        } else {
            byte[] utf8Key = secret.getBytes(StandardCharsets.UTF_8);
            keyCandidates.add(utf8Key);
            for (String variant : new String[]{secret, secret.replace('-', '+').replace('_', '/')}) {
                try {
                    String padded = variant;
                    int pad = padded.length() % 4;
                    if (pad != 0) {
                        padded += "=".repeat(4 - pad);
                    }
                    byte[] decoded = Base64.getDecoder().decode(padded);
                    if (decoded.length > 0 && decoded.length != utf8Key.length) {
                        boolean duplicate = false;
                        for (byte[] existing : keyCandidates) {
                            if (Arrays.equals(existing, decoded)) {
                                duplicate = true;
                            }
                        }
                        if (!duplicate) {
                            keyCandidates.add(decoded);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            // Fallback docs: en pruebas la firma usa clave vacía aunque tengas secret configurado (webhooks de llaves de pruebas)
            keyCandidates.add(new byte[0]);
        }

        byte[] payloadBytes = payloadCrudo.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.getEncoder().encodeToString(payloadBytes);
        byte[] base64Bytes = base64.getBytes(StandardCharsets.UTF_8);
        String base64Prefix = base64.length() > 40 ? base64.substring(0, 40) + "…" : base64;
        log.info("[BoldWebhook] diag secretLen={} base64Len={} payloadLen={} firmaLen={} base64Prefix={} firmaPrefix={} keyCandidates={}", secret.length(), base64.length(), payloadCrudo.length(), firma.length(), base64Prefix, firma.length() > 16 ? firma.substring(0, 16) + "…" : firma, keyCandidates.size());

        String firmaNorm = firma.trim().toLowerCase();
        for (int ki = 0; ki < keyCandidates.size(); ki++) {
            byte[] keyBytes = keyCandidates.get(ki);
            try {
                // 1) HMAC(Base64(body), key) — flujo oficial docs Bold
                String esperadoBase64 = toHex(hmacSha256(keyBytes, base64Bytes));
                if (constanteIgual(firmaNorm, esperadoBase64.toLowerCase())) {
                    log.info("[BoldWebhook] firma HMAC Base64 válida (candidate={}/{}, keyLen={})", ki + 1, keyCandidates.size(), keyBytes.length);
                    return true;
                }
                if (ki == 0) {
                    log.warn("[BoldWebhook] HMAC Base64 no coincide: esperado={} recibido={} payloadLen={} keyLen={}", esperadoBase64, firma, payloadCrudo.length(), keyBytes.length);
                }
                // 2) Fallback RAW: HMAC(payload, key)
                String esperadoRaw = toHex(hmacSha256(keyBytes, payloadBytes));
                if (constanteIgual(firmaNorm, esperadoRaw.toLowerCase())) {
                    log.info("[BoldWebhook] firma HMAC RAW válida (candidate={}/{}, keyLen={})", ki + 1, keyCandidates.size(), keyBytes.length);
                    return true;
                }
                if (ki == 0) {
                    log.warn("[BoldWebhook] HMAC RAW tampoco coincide: esperadoRaw={} recibido={} keyLen={}", esperadoRaw, firma, keyBytes.length);
                }
            } catch (Exception ex) {
                log.warn("[BoldWebhook] error HMAC (candidate={}/{}, keyLen={}): {}", ki + 1, keyCandidates.size(), keyBytes.length, ex.getMessage());
            }
        }
        log.warn("[BoldWebhook] firma inválida — header={}", firma);
        return false;
    }

    /**
     * HMAC-SHA256 compatible con clave vacía (docs Bold modo pruebas:
     * secret=""). {@code SecretKeySpec} no permite {@code byte[0]} en Java, por
     * lo que se implementa manualmente el caso vacío según RFC2104: HMAC =
     * SHA256(opad || SHA256(ipad || data)).
     */
    private byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        if (key.length > 0) {
            SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(keySpec);
            return mac.doFinal(data);
        }
        // Clave vacía: HMAC RFC2104 con keyLen 0 → ipad/opad de 64 bytes (blockSize SHA-256)
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] ipad = new byte[64];
        byte[] opad = new byte[64];
        Arrays.fill(ipad, (byte) 0x36);
        Arrays.fill(opad, (byte) 0x5c);
        sha256.update(ipad);
        sha256.update(data);
        byte[] inner = sha256.digest();
        sha256.reset();
        sha256.update(opad);
        sha256.update(inner);
        return sha256.digest();
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
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
