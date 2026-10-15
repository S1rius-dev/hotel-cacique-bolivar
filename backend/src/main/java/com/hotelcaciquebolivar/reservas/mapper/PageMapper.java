package com.hotelcaciquebolivar.reservas.mapper;

import java.util.List;
import java.util.function.Function;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Mapper MapStruct para la conversion de paginaciones de Spring Data a DTOs {@link PageResponse}.
 *
 * <p>Proporciona metodos genericos que transforman {@link Page} de Spring Data en DTOs de respuesta
 * paginados, con soporte para mapeo directo de contenido o mapeo con transformacion de tipos.</p>
 */
@Mapper(componentModel = "spring")
public interface PageMapper {

    /**
     * Convierte un {@link Page} de Spring Data en un DTO {@link PageResponse} sin transformar el contenido.
     *
     * <p>Los elementos del contenido se mantienen tal cual, sin transformacion adicional.</p>
     *
     * @param <T>    el tipo de los elementos en la pagina.
     * @param page   la pagina de Spring Data a convertir.
     * @return el DTO de respuesta paginada con los metadatos de paginacion y el contenido.
     */
    default <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Convierte un {@link Page} de Spring Data en un DTO {@link PageResponse}, transformando el contenido
     * mediante una funcion de mapeo proporcionada.
     *
     * <p>Aplica la funcion {@code mapper} a cada elemento del contenido antes de construir el DTO de respuesta.</p>
     *
     * @param <T>    el tipo de los elementos en la pagina original.
     * @param <R>    el tipo de los elementos en la pagina transformada.
     * @param page   la pagina de Spring Data a convertir.
     * @param mapper la funcion de transformacion a aplicar a cada elemento.
     * @return el DTO de respuesta paginada con el contenido transformado y los metadatos de paginacion.
     */
    default <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
