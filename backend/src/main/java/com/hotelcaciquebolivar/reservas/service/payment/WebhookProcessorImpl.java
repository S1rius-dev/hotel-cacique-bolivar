package com.hotelcaciquebolivar.reservas.service.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.exception.payment.WebhookVerificationException;

/**
 * Implementación del procesador de webhooks de pago.
 * Verifica la firma del webhook usando el servicio de firmas de la pasarela,
 * normaliza la notificación y la despacha al servicio de confirmación de transacciones.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
public class WebhookProcessorImpl implements WebhookProcessor {

    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentService paymentService;

    public WebhookProcessorImpl(
            PaymentGatewayFactory gatewayFactory,
            PaymentService paymentService) {
        this.gatewayFactory = gatewayFactory;
        this.paymentService = paymentService;
    }

    /**
     * {@inheritDoc}
     * Lanza {@link WebhookVerificationException} si la firma no es válida.
     * Utiliza el checksum del body como clave de idempotencia para reintentos de Wompi.
     */
    @Override
    @Transactional
    public String procesar(PaymentGatewayType gateway, String payloadCrudo, String firma) {
        PaymentGateway adapter = gatewayFactory.porTipo(gateway);

        if (!adapter.signatureService().verificar(firma, payloadCrudo)) {
            throw new WebhookVerificationException(
                    "Firma inv\u00e1lida recibida para la pasarela " + gateway);
        }

        // Idempotencia: si Wompi reenvía transaction.updated para misma tx, usar checksum como clave
        PaymentWebhookNotification tmp = adapter.normalizarNotificacion(payloadCrudo, firma, null);
        String idem = tmp.idempotenciaKey() != null ? tmp.idempotenciaKey() : firma;
        PaymentWebhookNotification notificacion = new PaymentWebhookNotification(
                tmp.gateway(), tmp.referenciaExterna(), tmp.estado(), idem, tmp.firma(), payloadCrudo, tmp.metodoPago());

        paymentService.confirmarTransaccion(notificacion);
        return "ok";
    }
}
