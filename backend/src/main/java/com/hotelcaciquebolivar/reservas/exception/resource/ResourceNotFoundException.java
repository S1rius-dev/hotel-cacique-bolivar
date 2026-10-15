package com.hotelcaciquebolivar.reservas.exception.resource;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se busca un recurso por su identificador y no se encuentra
 * en el sistema. Retorna error HTTP 404 (Not Found).
 * Hereda de {@link BusinessException} con código de error "RESOURCE_NOT_FOUND".
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * Constructor de ResourceNotFoundException.
     *
     * @param resource nombre del tipo de recurso que no se encontró (ej. "Habitación", "Cliente")
     * @param id       identificador del recurso buscado
     */
    public ResourceNotFoundException(String resource, Object id) {
        super(
                "RESOURCE_NOT_FOUND",
                resource + " con ID " + id + " no existe en el sistema",
                HttpStatus.NOT_FOUND
        );
    }
}
