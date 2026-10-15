package com.hotelcaciquebolivar.reservas.controller.payment;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.service.payment.WebhookProcessor;

/**
 * Controlador REST para recibir notificaciones webhook de pasarelas de pago.
 * Procesa eventos de pago de diferentes proveedores (Wompi, Bold, etc.)
 * y actualiza el estado de las transacciones en el sistema.
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookController.class);

    private final WebhookProcessor webhookProcessor;

    /**
     * Constructor que inyecta el procesador de webhooks.
     *
     * @param webhookProcessor procesador de webhooks de pago
     */
    public WebhookController(WebhookProcessor webhookProcessor) {
        this.webhookProcessor = webhookProcessor;
    }

    /**
     * Recibe y procesa notificaciones webhook de Wompi (compatibilidad).
     * Mantiene el endpoint legacy {@code /wompi} delegando al handler genérico.
     * Soporta tanto el header X-Event-Checksum (nuevo) como X-Event-Signature (legacy).
     *
     * @param payload  cuerpo del mensaje webhook en formato JSON
     * @param checksum firma de verificación del evento (header nuevo, opcional)
     * @param firma    firma de verificación del evento (header legacy, opcional)
     * @return respuesta con el resultado del procesamiento
     */
    @PostMapping("/wompi")
    public ResponseEntity<Map<String, Object>> recibirWompi(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Event-Checksum", required = false) String checksum,
            @RequestHeader(value = "X-Event-Signature", required = false) String firma,
            @RequestHeader(value = "X-Bold-Signature", required = false) String boldSig,
            @RequestHeader(value = "X-Signature", required = false) String genericSig) {
        String payload = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : null;
        String efectiva = firstNonBlank(checksum, firma, boldSig, genericSig);
        String result = webhookProcessor.procesar(PaymentGatewayType.WOMPI, payload, efectiva);
        return ResponseEntity.ok(Map.of("status", "ok", "result", result));
    }

    /**
     * Recibe y procesa notificaciones webhook de cualquier pasarela de pago.
     * Soporta Wompi (X-Event-Checksum/X-Event-Signature) y Bold (X-Bold-Signature/X-Signature).
     *
     * @param gateway  tipo de pasarela de pago (WOMPI, BOLD, etc.)
     * @param payload  cuerpo del mensaje webhook en formato JSON
     * @param checksum firma de verificación del evento (Wompi header nuevo, opcional)
     * @param firma    firma de verificación del evento (Wompi header legacy, opcional)
     * @return respuesta con el resultado del procesamiento
     */
    @PostMapping("/{gateway}")
    public ResponseEntity<Map<String, Object>> recibir(
            @PathVariable PaymentGatewayType gateway,
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Event-Checksum", required = false) String checksum,
            @RequestHeader(value = "X-Event-Signature", required = false) String firma,
            @RequestHeader(value = "X-Bold-Signature", required = false) String boldSig,
            @RequestHeader(value = "X-Signature", required = false) String genericSig,
            @RequestHeader(value = "X-Integrity-Signature", required = false) String integritySig) {
        String payload = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : null;
        String efectiva = firstNonBlank(checksum, firma, boldSig, genericSig, integritySig);
        log.info("[Webhook][{}] POST payloadLen={} firma={} checksum={} boldSig={} genericSig={} integritySig={} efectiva={}", gateway, payload != null ? payload.length() : 0, firma, checksum, boldSig, genericSig, integritySig, efectiva != null ? efectiva.substring(0, Math.min(16, efectiva.length())) + "…" : "null");
        String result = webhookProcessor.procesar(gateway, payload, efectiva);
        return ResponseEntity.ok(Map.of("status", "ok", "result", result));
    }

    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
