package com.hotelcaciquebolivar.reservas.service.reserva.customer;

import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.DisponibilidadPorTipoResponse;

/**
 * Interfaz del servicio de reservas para clientes (frontend público).
 * Define las operaciones disponibles para que los clientes puedan crear reservas,
 * consultar disponibilidad por tipo de habitación y consultar precios efectivos.
 */
public interface CustomerReservationService {

    /**
     * Crea una nueva reserva desde la perspectiva del cliente.
     * Asigna automáticamente la habitación según la estrategia configurada.
     *
     * @param request datos de la reserva del cliente
     * @return la reserva creada con la habitación asignada
     */
    CustomerReservationResponse crear(CustomerReservationRequest request);

    /**
     * Consulta la disponibilidad de habitaciones agrupada por tipo para un rango de fechas.
     *
     * @param checkIn  fecha de check-in
     * @param checkOut fecha de check-out
     * @return lista de disponibilidad por tipo de habitación
     */
    List<DisponibilidadPorTipoResponse> consultarDisponibilidadPorTipo(LocalDate checkIn, LocalDate checkOut);

    /**
     * Consulta el precio efectivo para un tipo de habitación en un rango de fechas.
     * Considera descuentos, promotions y ajustes de precio.
     *
     * @param tipoHabitacionId identificador del tipo de habitación
     * @param checkIn          fecha de check-in
     * @param checkOut         fecha de check-out
     * @return el precio efectivo con el desglose correspondiente
     */
    com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse consultarPrecioEfectivo(Long tipoHabitacionId, LocalDate checkIn, LocalDate checkOut);
}
