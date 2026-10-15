package com.hotelcaciquebolivar.reservas.repository.cliente;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.cliente.Municipio;

/**
 * Repositorio para la gestión de municipios.
 * Permite buscar municipios por departamento y por nombre con autocompletado.
 */
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    /**
     * Obtiene todos los municipios de un departamento específico.
     *
     * @param departamento nombre del departamento
     * @return lista de municipios del departamento
     */
    List<Municipio> findByDepartamento(String departamento);

    /**
     * Obtiene los 20 primeros municipios cuyo nombre contiene el texto indicado (case-insensitive).
     * Utilizado para funcionalidad de autocompletado.
     *
     * @param nombre texto a buscar en el nombre del municipio
     * @return lista de hasta 20 municipios que coinciden con el nombre
     */
    List<Municipio> findTop20ByNombreContainingIgnoreCase(String nombre);
}
