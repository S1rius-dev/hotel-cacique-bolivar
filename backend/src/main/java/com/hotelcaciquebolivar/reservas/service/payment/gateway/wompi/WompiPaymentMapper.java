package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiData;
import com.hotelcaciquebolivar.reservas.dto.payment.gateway.wompi.WompiWebhookRequest;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

import tools.jackson.databind.ObjectMapper;

/**
 * Mapper que convierte datos de Wompi (webhooks y respuestas de API)
 * a los formatos internos de la aplicación.
 * Maneja la extracción de transacciones desde payloads anidados
 * y el mapeo de estados y métodos de pago.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class WompiPaymentMapper {

    private final ObjectMapper objectMapper;

    public WompiPaymentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convierte un webhook de Wompi a una notificación interna normalizada.
     * Extrae la transacción del payload anidado y determina el estado y método de pago.
     *
     * @param webhook webhook deserializado de Wompi
     * @param firma firma recibida en el header
     * @param idempotenciaKey clave de idempotencia (si es null usa checksum del body)
     * @return notificación normalizada con referencia, estado y método de pago
     */
    public PaymentWebhookNotification toNotificacion(
            WompiWebhookRequest webhook, String firma, String idempotenciaKey) {
        WompiData data = extraerTransaccion(webhook);
        // Si no hay idempotenciaKey externo, usar checksum del body como idempotencia (Wompi reintenta)
        String idem = idempotenciaKey;
        if (!org.springframework.util.StringUtils.hasText(idem) && webhook != null && webhook.signature() != null) {
            idem = webhook.signature().checksum();
        }
        // payloadCrudo se inyectará en WebhookProcessorImpl; aquí mantenemos firma, referencia y método real
        return new PaymentWebhookNotification(
                PaymentGatewayType.WOMPI,
                data != null ? data.reference() : null,
                data != null ? mapearEstado(data.status()) : PaymentStatus.ERROR,
                idem,
                firma,
                null,
                data != null ? mapearMetodoPago(data) : null);
    }

    /**
     * Mapea el tipo de método de pago de Wompi al enumerativo interno.
     * Soporta CARD, NEQUI, PSE, BANCOLOMBIA_TRANSFER y BANCOLOMBIA_COLLECT.
     *
     * @param data datos de la transacción de Wompi
     * @return método de pago interno o null si no se puede determinar
     */
    public MetodoPago mapearMetodoPago(WompiData data) {
        if (data == null) return null;
        String type = data.paymentMethodType() != null ? data.paymentMethodType() : null;
        if (type == null && data.paymentMethod() != null) {
            type = data.paymentMethod().type();
        }
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "CARD" -> MetodoPago.TARJETA_CREDITO;
            case "NEQUI" -> MetodoPago.NEQUI;
            case "PSE" -> MetodoPago.PSE;
            case "BANCOLOMBIA_TRANSFER" -> MetodoPago.TRANSFERENCIA;
            case "BANCOLOMBIA_COLLECT" -> MetodoPago.TRANSFERENCIA;
            default -> MetodoPago.OTRO;
        };
    }

    @SuppressWarnings("unchecked")
    private WompiData extraerTransaccion(WompiWebhookRequest webhook) {
        if (webhook == null || webhook.data() == null) {
            return null;
        }
        Object tx = webhook.data().get(WompiPayloadKeys.TRANSACTION);
        if (tx == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(tx, WompiData.class);
        } catch (Exception ex) {
            // Fallback manual: extraer campos críticos sin deserializar completo (defensa ante dual-Jackson residual)
            if (tx instanceof java.util.Map<?, ?> map) {
                String id = map.get("id") != null ? String.valueOf(map.get("id")) : null;
                String reference = map.get("reference") != null ? String.valueOf(map.get("reference")) : null;
                String status = map.get("status") != null ? String.valueOf(map.get("status")) : null;
                String currency = map.get("currency") != null ? String.valueOf(map.get("currency")) : null;
                Long amountInCents = null;
                Object amt = map.get("amount_in_cents");
                if (amt instanceof Number n) amountInCents = n.longValue();
                else if (amt != null) try { amountInCents = Long.parseLong(String.valueOf(amt)); } catch (Exception ignored) {}
                String paymentMethodType = map.get("payment_method_type") != null ? String.valueOf(map.get("payment_method_type")) : null;
                // payment_method nested
                WompiData.WompiPaymentMethod pm = null;
                Object pmObj = map.get("payment_method");
                if (pmObj instanceof java.util.Map<?, ?> pmMap) {
                    String type = pmMap.get("type") != null ? String.valueOf(pmMap.get("type")) : null;
                    if (type == null) type = paymentMethodType;
                    pm = new WompiData.WompiPaymentMethod(type, null, null, null);
                }
                if (paymentMethodType == null && pm != null) paymentMethodType = pm.type();
                return new WompiData(id, reference, status, currency, amountInCents, paymentMethodType, pm);
            }
            throw ex;
        }
    }

    /**
     * Mapea el estado de transacción de Wompi al enumerativo interno.
     * Acepta APPROVED, PENDING, DECLINED, VOIDED, EXPIRED y retorna ERROR para valores desconocidos.
     *
     * @param status estado de la transacción en Wompi
     * @return estado interno de pago
     */
    public PaymentStatus mapearEstado(String status) {
        if (status == null) {
            return PaymentStatus.ERROR;
        }
        return switch (status) {
            case WompiPayloadKeys.APPROVED -> PaymentStatus.APPROVED;
            case WompiPayloadKeys.PENDING -> PaymentStatus.PENDING;
            case WompiPayloadKeys.DECLINED -> PaymentStatus.DECLINED;
            case WompiPayloadKeys.VOIDED -> PaymentStatus.VOIDED;
            case WompiPayloadKeys.EXPIRED -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.ERROR;
        };
    }
}
