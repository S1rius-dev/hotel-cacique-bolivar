package com.hotelcaciquebolivar.reservas.service.payment;

/**
 * Generador de referencia única para transacciones de pago.
 * Cada pasarela puede tener su propia implementación si necesita un formato distinto.
 */
public interface PaymentReferenceGenerator {

    /**
     * Genera una referencia única para una transacción de pago.
     *
     * @param reservaId identificador de la reserva
     * @param codigoReserva código alfanumérico de la reserva
     * @return referencia única (máximo 255 caracteres)
     */
    String generar(Long reservaId, String codigoReserva);

    /**
     * Tipo de pasarela que soporta este generador.
     *
     * @return tipo de pasarela, o null si es genérico
     */
    default com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType type() {
        return null;
    }
}
