package com.hotelcaciquebolivar.reservas.repository.registro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;

/**
 * Repositorio para la gestión de huéspedes registrados en reservas.
 * Permite realizar operaciones CRUD sobre la relación entre detalles de reserva y huéspedes,
 * así como búsquedas avanzadas usando JPA Specifications.
 */
public interface HuespedRegistroRepository extends
        JpaRepository<DetalleReservaHuesped, Long>,
        JpaSpecificationExecutor<DetalleReservaHuesped> {
}
