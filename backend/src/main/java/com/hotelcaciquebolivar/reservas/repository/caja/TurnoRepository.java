package com.hotelcaciquebolivar.reservas.repository.caja;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.caja.EstadoTurno;
import com.hotelcaciquebolivar.reservas.entity.caja.Turno;

/**
 * Repositorio para la gestión de turnos de caja.
 * Permite buscar turnos por caja y estado, y obtener historial de turnos por caja.
 */
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /**
     * Busca un turno por caja y estado específico.
     *
     * @param cajaId identificador de la caja
     * @param estado estado del turno (ABIERTO, CERRADO, etc.)
     * @return Optional con el turno encontrado o vacío si no existe
     */
    Optional<Turno> findByCajaIdAndEstado(Long cajaId, EstadoTurno estado);

    /**
     * Obtiene el historial de turnos de una caja específica, ordenados por fecha de apertura descendente.
     *
     * @param cajaId identificador de la caja
     * @param pageable paginación de resultados
     * @return página de turnos de la caja
     */
    Page<Turno> findByCajaIdOrderByFechaAperturaDesc(Long cajaId, Pageable pageable);
}
