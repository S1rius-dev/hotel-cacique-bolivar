package com.hotelcaciquebolivar.reservas.repository.reserva;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelcaciquebolivar.reservas.entity.reserva.BloqueoReserva;

/**
 * Repositorio para la gestión de bloqueos de reserva.
 * Permite buscar bloqueos activos totales (sin fechas) y bloqueos que intersectan con un rango de fechas.
 */
public interface BloqueoReservaRepository extends JpaRepository<BloqueoReserva, Long> {

    /**
     * Obtiene todos los bloqueos de reserva activos que son totales (sin fechas definidas).
     * Estos bloqueos impiden reservas en cualquier fecha.
     *
     * @return lista de bloqueos totales activos
     */
    @Query("SELECT b FROM BloqueoReserva b WHERE b.activo = true AND (b.fechaInicio IS NULL AND b.fechaFin IS NULL)")
    List<BloqueoReserva> findBloqueoTotalActivo();

    /**
     * Obtiene los bloqueos de reserva activos que intersectan con un rango de fechas específico.
     * Un bloqueo intersecta si sus fechas se superponen con el rango indicado.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return lista de bloqueos activos que intersectan con el rango
     */
    @Query("SELECT b FROM BloqueoReserva b WHERE b.activo = true AND b.fechaInicio IS NOT NULL AND b.fechaFin IS NOT NULL AND NOT (b.fechaFin < :desde OR b.fechaInicio > :hasta)")
    List<BloqueoReserva> findActivosQueIntersectan(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
