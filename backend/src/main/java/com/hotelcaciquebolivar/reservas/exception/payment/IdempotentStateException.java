package com.hotelcaciquebolivar.reservas.exception.payment;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se detecta un conflicto de estado idempotente
 * durante el procesamiento de un pago. Indica que la transacción ya fue procesada
 * o está en un estado inconsistente. Retorna error HTTP 409 (Conflict).
 * Hereda de {@link BusinessException} con código de error "IDEMPOTENT_STATE_CONFLICT".
 */
public class IdempotentStateException extends BusinessException {

    /**
     * Constructor de IdempotentStateException.
     *
     * @param mensaje descripción del conflicto de idempotencia
     */
    public IdempotentStateException(String mensaje) {
        super("IDEMPOTENT_STATE_CONFLICT", mensaje, HttpStatus.CONFLICT);
    }
}
