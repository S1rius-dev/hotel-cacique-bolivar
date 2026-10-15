package com.hotelcaciquebolivar.reservas.service.consumo;

import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoRequest;
import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoResponse;

/**
 * Interfaz que define las operaciones de gestión de consumos del sistema hotelero.
 * Los consumos representan productos o servicios adicionales consumidos por los huéspedes
 * durante su estancia, asociados a una reserva específica.
 */
public interface ConsumoService {

    /**
     * Agrega un nuevo consumo a una reserva específica.
     *
     * @param reservaId identificador único de la reserva a la que se agregará el consumo
     * @param dto objeto que contiene los datos del consumo a registrar
     * @param email email del usuario que registra el consumo
     * @return el consumo registrado con los datos completos
     */
    ConsumoResponse agregarConsumo(Long reservaId, ConsumoRequest dto, String email);

    /**
     * Lista todos los consumos asociados a una reserva específica.
     *
     * @param reservaId identificador único de la reserva
     * @return lista de consumos registrados para la reserva
     */
    List<ConsumoResponse> listarPorReserva(Long reservaId);

    /**
     * Anula un consumo específico de una reserva.
     * Marca el consumo como anulado sin eliminarlo del sistema.
     *
     * @param reservaId identificador único de la reserva
     * @param consumoId identificador único del consumo a anular
     * @param email email del usuario que anula el consumo
     */
    void anularConsumo(Long reservaId, Long consumoId, String email);
}
