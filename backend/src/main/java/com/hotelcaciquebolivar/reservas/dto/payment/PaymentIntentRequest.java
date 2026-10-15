package com.hotelcaciquebolivar.reservas.dto.payment;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

/**
 * DTO de solicitud para crear un intento de pago en la pasarela de pago.
 * Contiene la información necesaria para procesar un pago a través de la pasarela seleccionada.
 */
public record PaymentIntentRequest(
        /** Tipo de pasarela de pago a utilizar. */
        PaymentGatewayType gateway,
        /** Monto a cobrar. */
        BigDecimal monto,
        /** Código de moneda. */
        String moneda,
        /** Descripción del pago. */
        String descripcion,
        /** Referencia externa única de la transacción. */
        String referenciaExterna,
        /** Clave de idempotencia para evitar procesamiento duplicado. */
        String idempotenciaKey,
        /** Nombre completo del cliente. */
        String nombreCliente,
        /** Correo electrónico del cliente. */
        String emailCliente
) {
}
