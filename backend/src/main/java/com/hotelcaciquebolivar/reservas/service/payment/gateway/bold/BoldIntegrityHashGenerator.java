package com.hotelcaciquebolivar.reservas.service.payment.gateway.bold;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;

/**
 * Generador de hash de integridad para Bold Embedded Checkout.
 *
 * <p><b>¿Qué es?</b> Un SHA-256 hex de la cadena {@code {Identificador}{Monto}{Divisa}{LlaveSecreta}}
 * que Bold exige para verificar que la orden no fue manipulada en el frontend.
 * El frontend nunca conoce la llave secreta; el hash se genera en backend y se envía
 * como {@code data-integrity-hash} / {@code signature} al botón/script.</p>
 *
 * <p><b>¿Para qué sirve?</b></p>
 * <ul>
 *   <li>Garantiza integridad: si el monto/divisa/referencia son alterados en el browser, el hash no coincide y Bold rechaza el pago.</li>
 *   <li>Evita replay: ligando el hash a la referencia única, no se puede reutilizar para otra orden.</li>
 *   <li>No es autenticación: no sustituye la verificación de webhook con {@code BOLD_SECRET_KEY}.</li>
 * </ul>
 *
 * <p><b>¿Cómo se usa?</b></p>
 * <ol>
 *   <li>Backend: al iniciar checkout genera {@code referencia} única (HOTEL...), calcula
 *       {@code montoEnPesos} y llama {@code generar(referencia, montoEnPesos, moneda)}
 *       para obtener el hash. Lo expone en {@code CheckoutResponse.signatureIntegrity}.</li>
 *   <li>Frontend: renderiza el botón Bold con {@code data-api-key={identityKey}}, {@code data-amount={cents}},
 *       {@code data-currency={moneda}}, {@code data-reference={referencia}} y {@code data-integrity-hash={hash}}.</li>
 *   <li>Bold valida el hash antes de abrir el checkout embebido; si es inválido muestra error.</li>
 *   <li>Webhook: Bold re-notifica con firma propia (HMAC) — verificada por {@link BoldSignatureService}.</li>
 * </ol>
 *
 * <p>Cadena: {@code identificador + montoEnPesosSinFormato + divisa + llaveSecreta}</p>
 * <p>Ejemplo: Identificador=HOTEL2026RES123UID, Monto=50000 (50000 COP), Divisa=COP,
 * LlaveSecreta=mi_secreta → {@code "HOTEL2026RES123UID50000COPmi_secreta"} → SHA256 hex.</p>
 */
@Component
public class BoldIntegrityHashGenerator {

    private final PaymentProperties properties;

    public BoldIntegrityHashGenerator(PaymentProperties properties) {
        this.properties = properties;
    }

    /**
     * Genera el hash de integridad SHA-256 para Bold.
     * Bold usa monto en COP sin decimales (ej 70000 para 70.000 COP), no centavos.
     *
     * @param identificador referencia única de la orden (orderId)
     * @param montoEnPesos monto en COP sin decimales
     * @param divisa código de moneda (ej: COP)
     * @return hash SHA-256 en hex minúsculas, o null si falta configuración
     */
    public String generar(String identificador, long montoEnPesos, String divisa) {
        String secretRaw = properties.getBold() != null ? properties.getBold().getSecretKey() : null;
        // Docs Bold: en pruebas secret="" (vacío) es válido — no rechazar por isBlank, solo si es null o identificador null
        if (secretRaw == null || identificador == null) {
            return null;
        }
        String secret = secretRaw.trim();
        // Permitir secret vacío para modo pruebas: cadena = id + monto + divisa + ""
        String cadena = identificador + montoEnPesos + (divisa != null ? divisa : "") + secret;
        try {
            byte[] encoded = cadena.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(encoded);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /**
     * Valida un hash recibido contra el esperado (comparación en tiempo constante).
     */
    public boolean verificar(String identificador, long montoEnPesos, String divisa, String hashRecibido) {
        if (hashRecibido == null || hashRecibido.isBlank()) return false;
        String esperado = generar(identificador, montoEnPesos, divisa);
        if (esperado == null) return false;
        if (esperado.length() != hashRecibido.length()) return false;
        int result = 0;
        for (int i = 0; i < esperado.length(); i++) {
            result |= esperado.charAt(i) ^ hashRecibido.charAt(i);
        }
        return result == 0;
    }
}
