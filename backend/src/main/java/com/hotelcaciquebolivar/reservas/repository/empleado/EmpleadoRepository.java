package com.hotelcaciquebolivar.reservas.repository.empleado;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.empleado.Empleado;

/**
 * Repositorio para la gestión de empleados del hotel.
 * Permite buscar empleados por su ID de usuario asociado.
 */
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    /**
     * Busca un empleado por su ID de usuario asociado.
     *
     * @param userId identificador del usuario
     * @return Optional con el empleado encontrado o vacío si no existe
     */
    Optional<Empleado> findByUsuarioId(Long userId);

}
