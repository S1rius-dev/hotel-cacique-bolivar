package com.hotelcaciquebolivar.reservas.exception.resource;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se intenta crear un recurso que ya existe en el sistema,
 * violando una restricción de unicidad. Indica que el recurso con el campo especificado
 * ya está registrado. Retorna error HTTP 409 (Conflict).
 * Hereda de {@link BusinessException} con código de error "RESOURCE_ALREADY_EXISTS".
 */
public class ResourceAlreadyExistsException extends BusinessException {

    /**
     * Constructor de ResourceAlreadyExistsException.
     *
     * @param resourceName nombre del tipo de recurso que ya existe (ej. "Usuario", "Producto")
     * @param fieldName    nombre del campo que viola la restricción de unicidad
     * @param fieldValue   valor del campo que ya existe en el sistema
     */
    public ResourceAlreadyExistsException(
            String resourceName,
            String fieldName,
            Object fieldValue
    ) {
        super(
                "RESOURCE_ALREADY_EXISTS",
                resourceName + " ya existe con "
                + fieldName + " = " + fieldValue,
                HttpStatus.CONFLICT
        );
    }
}
