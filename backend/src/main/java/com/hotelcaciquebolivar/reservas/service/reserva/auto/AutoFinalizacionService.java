package com.hotelcaciquebolivar.reservas.service.reserva.auto;

/**
 * Interfaz del servicio de finalización automática de reservas.
 * Define el contrato para el procesamiento automático de reservas cuyo check-out
 * ha vencido y requieren ser finalizadas.
 */
public interface AutoFinalizacionService {
    /**
     * Finaliza automáticamente las reservas cuya fecha de check-out ha vencido.
     *
     * @return número de reservas finalizadas exitosamente
     */
    int finalizarReservasVencidas();
}
