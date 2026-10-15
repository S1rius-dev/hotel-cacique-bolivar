package com.hotelcaciquebolivar.reservas.repository.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;

/**
 * Repositorio para la gestión de huéspedes asociados a detalles de reserva.
 * Permite contar huéspedes activos en el hotel.
 */
public interface DetalleReservaHuespedRepository extends JpaRepository<DetalleReservaHuesped, Long> {

    /**
     * Cuenta el número de huéspedes únicos actualmente activos en el hotel.
     * Solo cuenta huéspedes cuyos detalles de reserva están en estado OCUPADA
     * y cuya reserva está en curso.
     *
     * @return cantidad de huéspedes activos únicos
     */
    @Query("SELECT COUNT(DISTINCT drh.cliente.id) FROM DetalleReservaHuesped drh JOIN drh.detalleReserva dr JOIN dr.reserva r WHERE dr.estado = 'OCUPADA' AND r.estado = 'EN_CURSO'")
    long countHuespedesActivos();
}
