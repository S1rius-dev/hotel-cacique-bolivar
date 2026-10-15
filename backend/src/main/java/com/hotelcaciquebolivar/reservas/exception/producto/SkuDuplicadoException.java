package com.hotelcaciquebolivar.reservas.exception.producto;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se intenta crear o actualizar un producto con un SKU
 * que ya existe en el sistema. Retorna error HTTP 409 (Conflict).
 * Hereda de {@link BusinessException} con código de error "SKU_DUPLICADO".
 */
public class SkuDuplicadoException extends BusinessException {
    /**
     * Constructor de SkuDuplicadoException.
     *
     * @param sku código SKU duplicado que causó el conflicto
     */
    public SkuDuplicadoException(String sku) {
        super("SKU_DUPLICADO", "Ya existe un producto con el SKU '" + sku + "'", HttpStatus.CONFLICT);
    }
}
