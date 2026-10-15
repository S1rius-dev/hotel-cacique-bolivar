package com.hotelcaciquebolivar.reservas.exception.producto;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se intenta reservar o consumir una cantidad de stock
 * mayor a la disponible para un producto. Retorna error HTTP 409 (Conflict).
 * Incluye detalles del producto, stock disponible y cantidad solicitada.
 * Hereda de {@link BusinessException} con código de error "STOCK_INSUFICIENTE".
 */
public class StockInsuficienteException extends BusinessException {
    /**
     * Constructor de StockInsuficienteException.
     *
     * @param productoId  identificador del producto con stock insuficiente
     * @param disponible  cantidad de stock disponible actualmente
     * @param solicitado  cantidad de stock solicitada por el usuario
     */
    public StockInsuficienteException(Long productoId, int disponible, int solicitado) {
        super("STOCK_INSUFICIENTE",
                "Stock insuficiente para producto " + productoId + ": disponible " + disponible + ", solicitado " + solicitado,
                HttpStatus.CONFLICT);
    }
}
