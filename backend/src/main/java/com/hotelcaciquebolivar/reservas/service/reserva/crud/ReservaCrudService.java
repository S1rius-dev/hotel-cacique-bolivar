package com.hotelcaciquebolivar.reservas.service.reserva.crud;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;

/**
 * Interfaz del servicio CRUD de reservas.
 * Define las operaciones básicas de creación, lectura, actualización y eliminación
 * de reservas en el sistema.
 */
public interface ReservaCrudService {
    /**
     * Crea una nueva reserva en el sistema.
     *
     * @param dto datos de la reserva a crear
     * @return la reserva creada con su información completa
     */
    ReservaResponse crear(ReservaRequest dto);

    /**
     * Busca una reserva por su identificador único.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada con su información completa
     */
    ReservaResponse buscarPorId(Long id);

    /**
     * Lista todas las reservas con filtros y paginación.
     *
     * @param codigo        filtro por código de reserva (parcial, insensible a mayúsculas)
     * @param cliente       filtro por nombre del cliente (parcial, insensible a mayúsculas)
     * @param estado        filtro por estado de la reserva
     * @param origen        filtro por origen de la reserva
     * @param checkInDesde  filtro por fecha de check-in desde
     * @param checkInHasta  filtro por fecha de check-in hasta
     * @param checkOutDesde filtro por fecha de check-out desde
     * @param checkOutHasta filtro por fecha de check-out hasta
     * @param pageable      configuración de paginación
     * @return página de reservas que cumplen con los filtros especificados
     */
    PageResponse<ReservaResponse> listarTodos(
            String codigo, String cliente, EstadoReserva estado,
            OrigenReserva origen,
            LocalDate checkInDesde, LocalDate checkInHasta,
            LocalDate checkOutDesde, LocalDate checkOutHasta,
            Pageable pageable);

    /**
     * Actualiza la información de una reserva existente.
     *
     * @param id  identificador de la reserva a actualizar
     * @param dto datos actualizados de la reserva
     * @return la reserva actualizada con su información completa
     */
    ReservaResponse actualizar(Long id, ReservaUpdateRequest dto);

    /**
     * Elimina una reserva del sistema.
     *
     * @param id identificador de la reserva a eliminar
     */
    void eliminar(Long id);
}
