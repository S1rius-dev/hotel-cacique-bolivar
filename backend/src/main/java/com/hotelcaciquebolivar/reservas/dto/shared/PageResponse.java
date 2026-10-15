package com.hotelcaciquebolivar.reservas.dto.shared;

import java.util.List;

/**
 * DTO genérico de respuesta paginada.
 * Contiene la lista de elementos, número de página, tamaño, total de elementos y total de páginas.
 *
 * @param <T> el tipo de los elementos en la página
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
        ) {

}
