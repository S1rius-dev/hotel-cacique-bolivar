package com.hotelcaciquebolivar.reservas.repository.caja;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.caja.Caja;

/**
 * Repositorio para la gestión de cajas del hotel.
 * Permite buscar cajas por nombre y estado de actividad.
 */
public interface CajaRepository extends JpaRepository<Caja, Long> {

    /**
     * Busca una caja por su nombre.
     *
     * @param nombre nombre de la caja a buscar
     * @return Optional con la caja encontrada o vacío si no existe
     */
    Optional<Caja> findByNombre(String nombre);

    /**
     * Busca una caja activa por su nombre.
     *
     * @param nombre nombre de la caja a buscar
     * @return Optional con la caja activa encontrada o vacío si no existe
     */
    Optional<Caja> findByNombreAndActivoTrue(String nombre);
}
