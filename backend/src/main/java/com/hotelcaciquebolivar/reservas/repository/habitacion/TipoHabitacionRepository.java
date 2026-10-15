package com.hotelcaciquebolivar.reservas.repository.habitacion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;

/**
 * Repositorio para la gestión de tipos de habitación.
 * Permite buscar tipos de habitación por su nombre.
 */
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {

    /**
     * Busca un tipo de habitación por su nombre.
     *
     * @param numero nombre del tipo de habitación
     * @return Optional con el tipo de habitación encontrado o vacío si no existe
     */
    Optional<TipoHabitacion> findByNombre(String numero);
}
