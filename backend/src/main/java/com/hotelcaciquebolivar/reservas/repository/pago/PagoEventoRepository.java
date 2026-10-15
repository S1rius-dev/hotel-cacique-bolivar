package com.hotelcaciquebolivar.reservas.repository.pago;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.pago.PagoEvento;

/**
 * Repositorio para la gestión de eventos de pago.
 * Permite consultar el historial de eventos asociados a un pago específico.
 */
public interface PagoEventoRepository extends JpaRepository<PagoEvento, Long> {

    /**
     * Obtiene los eventos de un pago específico, ordenados por fecha de creación ascendente.
     *
     * @param pagoId identificador del pago
     * @return lista de eventos del pago en orden cronológico
     */
    List<PagoEvento> findByPagoIdOrderByCreatedAtAsc(Long pagoId);
}
