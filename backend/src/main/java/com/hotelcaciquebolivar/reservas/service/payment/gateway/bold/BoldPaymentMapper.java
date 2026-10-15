package com.hotelcaciquebolivar.reservas.service.payment.gateway.bold;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.bold.BoldData;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.bold.BoldWebhookRequest;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

import tools.jackson.databind.ObjectMapper;

/**
 * Mapper entre payloads de Bold y el modelo interno {@link PaymentWebhookNotification}.
 */
@Component
public class BoldPaymentMapper {

    private final ObjectMapper objectMapper;

    public BoldPaymentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentWebhookNotification toNotificacion(
            BoldWebhookRequest webhook, String firma, String idempotenciaKey) {
        BoldData data = extraerTransaccion(webhook);
        String referencia = data != null ? data.effectiveReference() : null;
        // Fallback: Bold webhook usa data.metadata.reference y subject como referencia
        if ((referencia == null || referencia.isBlank()) && webhook != null && webhook.data() != null) {
            Object metaObj = webhook.data().get("metadata");
            if (metaObj instanceof Map<?, ?> metaMap) {
                Object ref = metaMap.get("reference");
                if (ref != null && !String.valueOf(ref).isBlank()) referencia = String.valueOf(ref);
            }
            if ((referencia == null || referencia.isBlank()) && webhook.subject() != null && !webhook.subject().isBlank()) {
                referencia = webhook.subject();
            }
        }
        // type también indica estado: SALE_APPROVED etc.
        String statusFromType = webhook != null ? webhook.type() : null;
        if (data != null && data.status() == null && statusFromType != null) {
            // mapear type como estado si data.status vacío
            data = new BoldData(data.id(), data.paymentId(), data.orderId(), data.reference(), statusFromType, data.currency(), data.amount(), data.amountInCents(), data.paymentMethod(), data.paymentMethodType());
            referencia = data.effectiveReference() != null ? data.effectiveReference() : referencia;
        }
        // Si la referencia viene en data como orderId/reference, usarla; si no, intentar extraer de firma/header
        String idem = idempotenciaKey;
        if ((idem == null || idem.isBlank()) && webhook != null) {
            if (webhook.signature() != null && !webhook.signature().isBlank()) idem = webhook.signature();
            else if (webhook.hash() != null && !webhook.hash().isBlank()) idem = webhook.hash();
        }
        PaymentStatus estado = data != null ? mapearEstado(data.status()) : PaymentStatus.ERROR;
        MetodoPago metodo = data != null ? mapearMetodoPago(data) : null;
        return new PaymentWebhookNotification(
                PaymentGatewayType.BOLD,
                referencia,
                estado,
                idem,
                firma,
                null,
                metodo);
    }

    @SuppressWarnings("unchecked")
    private BoldData extraerTransaccion(BoldWebhookRequest webhook) {
        if (webhook == null || webhook.data() == null) return null;
        Map<String, Object> data = webhook.data();
        // data puede contener directamente los campos o estar anidado en "transaction" / "payment"
        Object nested = data.get("transaction");
        if (nested == null) nested = data.get("payment");
        if (nested == null) nested = data.get("order");
        Map<String, Object> txMap;
        if (nested instanceof Map<?, ?> m) {
            txMap = (Map<String, Object>) m;
        } else {
            txMap = data;
        }
        try {
            return objectMapper.convertValue(txMap, BoldData.class);
        } catch (Exception ex) {
            // Fallback manual para payloads oficiales Bold (amount es objeto {total, currency})
            String id = str(txMap.get("id"));
            String paymentId = str(txMap.get("payment_id"));
            String orderId = str(txMap.get("orderId"));
            if (orderId == null) orderId = str(txMap.get("order_id"));
            if (orderId == null) orderId = str(txMap.get("reference"));
            // metadata.reference es el orderId real en webhooks Bold Botón de Pagos
            if (orderId == null) {
                Object metaObj2 = txMap.get("metadata");
                if (metaObj2 instanceof Map<?, ?> metaMap2) {
                    Object ref2 = metaMap2.get("reference");
                    if (ref2 != null) orderId = String.valueOf(ref2);
                }
            }
            String reference = str(txMap.get("reference"));
            if (reference == null) reference = orderId;
            String status = str(txMap.get("status"));
            String currency = str(txMap.get("currency"));
            // amount puede ser Long o Map {total, currency}
            Long amountInCents = toLong(txMap.get("amount_in_cents"));
            Long amount = null;
            Object amountObj = txMap.get("amount");
            if (amountObj instanceof Map<?, ?> amountMap) {
                Object total = amountMap.get("total");
                amount = toLong(total);
                if (amountInCents == null) amountInCents = amount;
                Object cur = amountMap.get("currency");
                if (cur != null) currency = String.valueOf(cur);
            } else {
                amount = toLong(amountObj);
                if (amountInCents == null) amountInCents = amount;
            }
            String paymentMethod = str(txMap.get("payment_method"));
            if (paymentMethod == null) paymentMethod = str(txMap.get("payment_method_type"));
            return new BoldData(id, paymentId, orderId, reference, status, currency, amount, amountInCents, paymentMethod, paymentMethod);
        }
    }

    public MetodoPago mapearMetodoPago(BoldData data) {
        if (data == null) return null;
        String type = data.paymentMethodType() != null ? data.paymentMethodType() : data.paymentMethod();
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "CARD", "CREDIT_CARD", "DEBIT_CARD" -> MetodoPago.TARJETA_CREDITO;
            case "PSE" -> MetodoPago.PSE;
            case "NEQUI" -> MetodoPago.NEQUI;
            case "BANCOLOMBIA", "TRANSFER", "TRANSFERENCIA" -> MetodoPago.TRANSFERENCIA;
            default -> MetodoPago.OTRO;
        };
    }

    public PaymentStatus mapearEstado(String status) {
        if (status == null) return PaymentStatus.ERROR;
        return switch (status.toUpperCase()) {
            case "APPROVED", "APPROVED_TRANSACTION", "SALE_APPROVED", "PAYMENT_APPROVED", "SUCCESS" -> PaymentStatus.APPROVED;
            case "PENDING", "PENDING_TRANSACTION", "IN_PROGRESS" -> PaymentStatus.PENDING;
            case "DECLINED", "REJECTED", "FAILED", "DENIED" -> PaymentStatus.DECLINED;
            case "VOIDED", "CANCELLED", "CANCELED" -> PaymentStatus.VOIDED;
            case "EXPIRED", "EXPIRED_TRANSACTION" -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.ERROR;
        };
    }

    private String str(Object o) {
        return o != null ? String.valueOf(o) : null;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception ignored) { return null; }
    }
}
