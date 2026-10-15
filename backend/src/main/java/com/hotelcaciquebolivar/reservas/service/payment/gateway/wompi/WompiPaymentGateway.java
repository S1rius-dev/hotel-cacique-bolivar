package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutWidgetConfig;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentGatewayMetadata;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentIntentRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentRefundResult;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiSignature;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiWebhookRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiWidgetConfig;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentGateway;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentSignatureService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Implementación de {@link PaymentGateway} para la pasarela Wompi.
 * Prepara el checkout con firma de integridad, consulta transacciones
 * por referencia o ID, normaliza webhooks y gestiona reembolsos.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class WompiPaymentGateway implements PaymentGateway {

    private final WompiClient client;
    private final WompiPaymentMapper mapper;
    private final WompiSignatureService signatureService;
    private final WompiIntegritySignatureGenerator integritySignatureGenerator;
    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    public WompiPaymentGateway(
            WompiClient client,
            WompiPaymentMapper mapper,
            WompiSignatureService signatureService,
            WompiIntegritySignatureGenerator integritySignatureGenerator,
            PaymentProperties properties,
            ObjectMapper objectMapper) {
        this.client = client;
        this.mapper = mapper;
        this.signatureService = signatureService;
        this.integritySignatureGenerator = integritySignatureGenerator;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /** {@inheritDoc} Retorna WOMPI como tipo de pasarela. */
    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.WOMPI;
    }

    @Override
    public PaymentSignatureService signatureService() {
        return signatureService;
    }

    /**
     * {@inheritDoc}
     * Convierte el monto a centavos, genera la firma de integridad SHA-256
     * y construye la configuración del widget Wompi.
     * Omite redirectUrl en entorno local para evitar bloqueo de CloudFront.
     */
    @Override
    public CheckoutWidgetConfig prepararCheckout(PaymentIntentRequest request) {
        long centavos = request.monto()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        String referencia = request.referenciaExterna();
        String moneda = request.moneda();
        String integridad = integritySignatureGenerator.generar(
                referencia, centavos, moneda);

        String redirectUrl = properties.getWompi().getRedirectUrl();
        // Wompi CloudFront bloquea http://localhost como redirect-url (WAF). En dev omitir para que widget abra (verificado con widget mínimo sin redirectUrl)
        if (redirectUrl != null && redirectUrl.startsWith("http://localhost")) {
            redirectUrl = null;
        }
        WompiWidgetConfig widget = new WompiWidgetConfig(
                properties.getWompi().getPublicKey(),
                moneda,
                centavos,
                referencia,
                redirectUrl,
                new WompiSignature(integridad));

        return new CheckoutWidgetConfig(
                widget.reference(),
                request.monto(),
                widget.amountInCents(),
                moneda,
                widget.publicKey(),
                widget.signature().integrity(),
                widget.redirectUrl());
    }

    /**
     * {@inheritDoc}
     * Intenta consultar por referencia; si falla, busca por ID como fallback.
     * Retorna ERROR si la transacción no se encuentra.
     */
    @Override
    public PaymentStatus consultarTransaccion(String referenciaExterna) {
        // Primero intentar por referencia (nuestro caso habitual); si no hay resultado, intentar por id
        try {
            var dataRef = client.consultarPorReferencia(referenciaExterna);
            if (dataRef != null) {
                return mapper.mapearEstado(dataRef.status());
            }
        } catch (Exception ignored) {
            // fallback a id
        }
        var data = client.consultarTransaccion(referenciaExterna);
        return data != null ? mapper.mapearEstado(data.status()) : PaymentStatus.ERROR;
    }

    /** {@inheritDoc} Consulta por referencia y luego por ID como fallback. */
    @Override
    public com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago consultarMetodoPago(String referenciaExterna) {
        try {
            var dataRef = client.consultarPorReferencia(referenciaExterna);
            if (dataRef != null) {
                var m = mapper.mapearMetodoPago(dataRef);
                if (m != null) return m;
            }
        } catch (Exception ignored) {
        }
        try {
            var data = client.consultarTransaccion(referenciaExterna);
            if (data != null) {
                return mapper.mapearMetodoPago(data);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Deserializa el payload JSON a {@link WompiWebhookRequest} y extrae
     * la referencia, estado y método de pago de la transacción.
     */
    @Override
    public PaymentWebhookNotification normalizarNotificacion(
            String payloadCrudo, String firma, String idempotenciaKey) {
        try {
            WompiWebhookRequest webhook = objectMapper.readValue(
                    payloadCrudo, WompiWebhookRequest.class);
            return mapper.toNotificacion(webhook, firma, idempotenciaKey);
        } catch (Exception ex) {
            // Captura JacksonException + IllegalArgumentException de convertValue fallback + cualquier deserialización Long
            org.slf4j.LoggerFactory.getLogger(WompiPaymentGateway.class)
                    .error("[WompiWebhook] normalizarNotificacion falló: {} — payload (truncado): {}",
                            ex.getMessage(),
                            payloadCrudo != null ? payloadCrudo.substring(0, Math.min(1200, payloadCrudo.length())) : "null", ex);
            throw new PaymentGatewayException(
                    "No se pudo interpretar la notificaci\u00f3n webhook de Wompi", ex);
        }
    }

    /** {@inheritDoc} Delega al {@link WompiClient} para realizar el reembolso. */
    @Override
    public PaymentRefundResult reembolsar(String referenciaExterna, BigDecimal monto) {
        client.reembolsar(referenciaExterna, monto);
        return new PaymentRefundResult(referenciaExterna, PaymentStatus.VOIDED, monto);
    }

    @Override
    public PaymentGatewayMetadata getMetadata() {
        return new PaymentGatewayMetadata(
                PaymentGatewayType.WOMPI,
                "WOMPI",
                "Wompi",
                "Pago seguro con Wompi",
                "/assets/gateways/wompi/Wompi_LogoPrincipal.svg",
                properties.getMoneda());
    }
}
