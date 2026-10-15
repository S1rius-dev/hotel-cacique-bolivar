package com.hotelcaciquebolivar.reservas.service.reserva.auto;

/**
 * Interfaz del servicio de procesamiento automático de No-Show.
 * Define el contrato para el marcado automático de reservas como NO_SHOW
 * cuando los huéspedes no se presentan en la fecha de check-in.
 */
public interface NoShowService {
    /**
     * Procesa automáticamente las reservas que no fueron presentadas (No-Show).
     * Marca como NO_SHOW las reservas confirmadas o pendientes cuya fecha de check-in
     * ya pasó sin que se haya realizado check-in.
     *
     * @return número de reservas marcadas como NO_SHOW
     */
    int procesarNoShow();
}
