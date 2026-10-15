package com.hotelcaciquebolivar.reservas.service.payment;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;

import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutWidgetConfig;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentGatewayMetadata;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentIntentRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentRefundResult;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;

/**
 * Interfaz que define el contrato para las pasarelas de pago.
 * Cada implementación (Wompi, etc.) debe soportar checkout,
 * consulta de transacciones, normalización de webhooks y reembolsos.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PaymentGateway {

    /**
     * Retorna el tipo de pasarela de pago que representa esta implementación.
     *
     * @return tipo de pasarela (ej. WOMPI)
     */
    PaymentGatewayType type();

    /**
     * Prepara la configuración del widget de checkout para el pago en línea.
     *
     * @param request datos del intento de pago (monto, moneda, referencia, etc.)
     * @return configuración del widget con firma de integridad y clave pública
     */
    CheckoutWidgetConfig prepararCheckout(PaymentIntentRequest request);

    /**
     * Consulta el estado de una transacción en la pasarela de pago.
     *
     * @param referenciaExterna referencia única de la transacción
     * @return estado actual de la transacción
     */
    PaymentStatus consultarTransaccion(String referenciaExterna);

    /**
     * Consulta el método de pago real utilizado en la transacción.
     *
     * @param referenciaExterna referencia única de la transacción
     * @return método de pago o null si no se puede determinar
     */
    default MetodoPago consultarMetodoPago(String referenciaExterna) {
        return null;
    }

    /**
     * Normaliza una notificación webhook recibida de la pasarela a un formato interno.
     *
     * @param payloadCrudo cuerpo JSON del webhook
     * @param firma firma de verificación del webhook
     * @param idempotenciaKey clave de idempotencia (opcional)
     * @return notificación normalizada
     */
    PaymentWebhookNotification normalizarNotificacion(
            String payloadCrudo, String firma, String idempotenciaKey);

    /**
     * Realiza el reembolso de una transacción en la pasarela de pago.
     *
     * @param referenciaExterna referencia de la transacción a reembolsar
     * @param monto monto a reembolsar
     * @return resultado del reembolso con estado y monto
     */
    PaymentRefundResult reembolsar(String referenciaExterna, java.math.BigDecimal monto);

    /**
     * Retorna el servicio de verificación de firmas de esta pasarela.
     *
     * @return servicio de firmas
     */
    PaymentSignatureService signatureService();

    /**
     * Retorna la metadata pública de esta pasarela (nombre, logo, textos).
     * Útil para exponer información al frontend sin filtrar secretos.
     *
     * @return metadata pública
     */
    default PaymentGatewayMetadata getMetadata() {
        return new PaymentGatewayMetadata(
                type(),
                type().name(),
                type().name(),
                "Pago seguro con " + type().name(),
                null,
                "COP");
    }
}
