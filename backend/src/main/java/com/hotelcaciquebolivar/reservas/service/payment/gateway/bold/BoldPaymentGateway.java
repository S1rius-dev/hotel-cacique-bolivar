package com.hotelcaciquebolivar.reservas.service.payment.gateway.bold;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutWidgetConfig;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentGatewayMetadata;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentIntentRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentRefundResult;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.bold.BoldWebhookRequest;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentGateway;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentSignatureService;

import tools.jackson.databind.ObjectMapper;

/**
 * Implementación stub de {@link PaymentGateway} para Bold.
 * Preparada para la futura integración con la API de Bold.
 * Actualmente no realiza llamadas externas: deja el checkout pendiente
 * de la implementación real.
 */
@Component
public class BoldPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(BoldPaymentGateway.class);

    private final PaymentProperties properties;
    private final BoldSignatureService signatureService;
    private final BoldIntegrityHashGenerator integrityHashGenerator;
    private final BoldPaymentMapper mapper;
    private final ObjectMapper objectMapper;

    public BoldPaymentGateway(PaymentProperties properties,
            BoldSignatureService signatureService,
            BoldIntegrityHashGenerator integrityHashGenerator,
            BoldPaymentMapper mapper,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureService = signatureService;
        this.integrityHashGenerator = integrityHashGenerator;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.BOLD;
    }

    @Override
    public PaymentSignatureService signatureService() {
        return signatureService;
    }

    @Override
    public PaymentGatewayMetadata getMetadata() {
        return new PaymentGatewayMetadata(
                PaymentGatewayType.BOLD,
                "BOLD",
                "Bold",
                "Pago seguro con Bold",
                "/assets/gateways/bold/Bold_Logo.svg",
                properties.getMoneda());
    }

    /**
     * Prepara la configuración del checkout Embedded de Bold.
     * Bold usa monto en COP sin decimales (ej 70000, no 7000000 centavos).
     * Genera hash SHA-256 {@code {Identificador}{Monto}{Divisa}{LlaveSecreta}}.
     */
    @Override
    public CheckoutWidgetConfig prepararCheckout(PaymentIntentRequest request) {
        long montoPesos = request.monto()
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        String referencia = request.referenciaExterna();
        String moneda = request.moneda();

        String identityKey = properties.getBold() != null ? properties.getBold().getIdentityKey() : null;
        if ((identityKey == null || identityKey.isBlank()) && properties.getBold() != null) {
            identityKey = properties.getBold().getPublicKey();
        }
        String redirectUrl = properties.getBold() != null ? properties.getBold().getRedirectUrl() : null;
        if (redirectUrl != null && redirectUrl.startsWith("http://localhost")) {
            redirectUrl = null;
        }

        String integrityHash = integrityHashGenerator.generar(referencia, montoPesos, moneda);
        if (integrityHash == null) {
            log.warn("[BoldCheckout] no se pudo generar integrity hash — BOLD_SECRET_KEY no configurada. referencia={}", referencia);
            integrityHash = "missing-secret";
        }

        log.info("[BoldCheckout] prepararCheckout referencia={} monto={} montoPesos={} moneda={} identityKey={} integrityHash={}",
                referencia, request.monto(), montoPesos, moneda,
                identityKey != null ? identityKey.substring(0, Math.min(8, identityKey.length())) + "…" : "null",
                integrityHash != null ? integrityHash.substring(0, Math.min(8, integrityHash.length())) + "…" : "null");

        return new CheckoutWidgetConfig(
                referencia,
                request.monto(),
                montoPesos,
                moneda,
                identityKey,
                integrityHash,
                redirectUrl);
    }

    @Override
    public PaymentStatus consultarTransaccion(String referenciaExterna) {
        log.warn("[Bold] consultarTransaccion stub no implementado para referencia {}", referenciaExterna);
        throw new PaymentGatewayException("Bold no está implementado aún — consulta no disponible");
    }

    @Override
    public MetodoPago consultarMetodoPago(String referenciaExterna) {
        log.warn("[Bold] consultarMetodoPago stub no implementado para referencia {}", referenciaExterna);
        return null;
    }

    @Override
    public PaymentWebhookNotification normalizarNotificacion(String payloadCrudo, String firma, String idempotenciaKey) {
        try {
            BoldWebhookRequest webhook = objectMapper.readValue(payloadCrudo, BoldWebhookRequest.class);
            // Si el payload no parsea como BoldWebhookRequest, intentar como Map flexible
            PaymentWebhookNotification notif = mapper.toNotificacion(webhook, firma, idempotenciaKey);
            // Inyectar payloadCrudo y firma para WebhookProcessor
            return new PaymentWebhookNotification(
                    notif.gateway(),
                    notif.referenciaExterna(),
                    notif.estado(),
                    notif.idempotenciaKey(),
                    firma,
                    payloadCrudo,
                    notif.metodoPago());
        } catch (Exception ex) {
            log.error("[BoldWebhook] normalizarNotificacion falló: {} — payload (truncado): {}",
                    ex.getMessage(),
                    payloadCrudo != null ? payloadCrudo.substring(0, Math.min(1200, payloadCrudo.length())) : "null", ex);
            throw new PaymentGatewayException("No se pudo interpretar la notificación webhook de Bold", ex);
        }
    }

    @Override
    public PaymentRefundResult reembolsar(String referenciaExterna, BigDecimal monto) {
        log.warn("[Bold] reembolsar stub no implementado para referencia {}", referenciaExterna);
        throw new PaymentGatewayException("Bold no está implementado aún — reembolso no disponible");
    }
}
