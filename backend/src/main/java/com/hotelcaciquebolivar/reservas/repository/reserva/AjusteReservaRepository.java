package com.hotelcaciquebolivar.reservas.repository.reserva;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.reserva.AjusteReserva;

/**
 * Repositorio para la gestión de ajustes a reservas.
 * Permite consultar ajustes asociados a una reserva específica.
 */
public interface AjusteReservaRepository extends JpaRepository<AjusteReserva, Long> {

    /**
     * Obtiene los ajustes de una reserva específica.
     *
     * @param reservaId identificador de la reserva
     * @return lista de ajustes de la reserva
     */
    List<AjusteReserva> findByReservaId(Long reservaId);
}
