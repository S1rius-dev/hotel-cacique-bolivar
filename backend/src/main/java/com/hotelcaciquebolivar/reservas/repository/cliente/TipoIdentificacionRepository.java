package com.hotelcaciquebolivar.reservas.repository.cliente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;

/**
 * Repositorio para la gestión de tipos de identificación.
 * Permite buscar tipos de identificación por su código.
 */
public interface TipoIdentificacionRepository
        extends JpaRepository<TipoIdentificacion, Long> {

    /**
     * Busca un tipo de identificación por su código.
     *
     * @param codigo código del tipo de identificación (ej: CC, CE, NIT)
     * @return Optional con el tipo de identificación encontrado o vacío si no existe
     */
    Optional<TipoIdentificacion> findByCodigo(String codigo);
}
