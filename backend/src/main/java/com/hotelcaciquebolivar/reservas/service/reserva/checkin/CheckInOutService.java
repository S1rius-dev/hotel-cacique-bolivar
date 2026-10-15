package com.hotelcaciquebolivar.reservas.service.reserva.checkin;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleCheckInRequest;

/**
 * Interfaz del servicio de check-in y check-out de reservas.
 * Define las operaciones para gestionar el registro de entrada y salida de huéspedes,
 * tanto a nivel de reserva completa como a nivel de detalle individual.
 */
public interface CheckInOutService {
    /**
     * Realiza el check-in completo de una reserva, cambiando su estado a EN_CURSO
     * y todos sus detalles a OCUPADA.
     *
     * @param reservaId identificador de la reserva
     * @return la reserva con su estado actualizado
     */
    ReservaResponse realizarCheckIn(Long reservaId);

    /**
     * Realiza el check-out completo de una reserva, cambiando su estado a FINALIZADA
     * y todos sus detalles a LIBERADA.
     *
     * @param reservaId identificador de la reserva
     * @return la reserva con su estado actualizado
     */
    ReservaResponse realizarCheckOut(Long reservaId);

    /**
     * Realiza el check-in de un detalle específico de una reserva.
     * Permite el check-in individual de habitaciones cuando la reserva tiene múltiples detalles.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle de reserva
     * @param dto       datos adicionales del check-in
     * @return la reserva con el detalle actualizado
     */
    ReservaResponse checkInPorDetalle(Long reservaId, Long detalleId, DetalleCheckInRequest dto);

    /**
     * Realiza el check-out de un detalle específico de una reserva.
     * Permite el check-out individual de habitaciones cuando la reserva tiene múltiples detalles.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle de reserva
     * @return la reserva con el detalle actualizado
     */
    ReservaResponse checkOutPorDetalle(Long reservaId, Long detalleId);
}
