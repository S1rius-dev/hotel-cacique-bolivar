package com.hotelcaciquebolivar.reservas.repository.pago;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.hotelcaciquebolivar.reservas.entity.pago.ReciboSequence;

import jakarta.persistence.LockModeType;

/**
 * Repositorio para la gestión de secuencias de numeración de recibos.
 * Utiliza bloqueo pesimista para garantizar la generación secuencial de números de recibo
 * en entornos concurrentes.
 */
public interface ReciboSequenceRepository extends JpaRepository<ReciboSequence, Integer> {

    /**
     * Busca la secuencia de recibos de un año específico con bloqueo pesimista para escritura.
     * Utilizado para generar números de recibo secuenciales de forma concurrente.
     *
     * @param year año de la secuencia
     * @return Optional con la secuencia bloqueada o vacío si no existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReciboSequence r WHERE r.year = :year")
    Optional<ReciboSequence> findByYearForUpdate(Integer year);
}
