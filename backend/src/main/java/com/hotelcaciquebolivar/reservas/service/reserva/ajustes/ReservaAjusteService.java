package com.hotelcaciquebolivar.reservas.service.reserva.ajustes;

import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;

/**
 * Interfaz del servicio de ajustes de reserva.
 * Define las operaciones para gestionar ajustes (cargos extra o descuentos)
 * asociados a una reserva.
 */
public interface ReservaAjusteService {

    /**
     * Agrega un ajuste a una reserva específica.
     *
     * @param reservaId identificador de la reserva
     * @param dto       datos del ajuste a agregar
     * @return el ajuste creado con su información completa
     */
    AjusteResponse agregarAjuste(Long reservaId, AjusteRequest dto);

    /**
     * Edita un ajuste existente de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId  identificador del ajuste a editar
     * @param dto       datos actualizados del ajuste
     * @return el ajuste actualizado
     */
    AjusteResponse editarAjuste(Long reservaId, Long ajusteId, AjusteRequest dto);

    /**
     * Elimina un ajuste de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId  identificador del ajuste a eliminar
     */
    void eliminarAjuste(Long reservaId, Long ajusteId);
}
