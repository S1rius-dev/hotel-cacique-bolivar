package com.hotelcaciquebolivar.reservas.service.reserva.customer.assignment;

import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;

/**
 * Interfaz de estrategia de asignación de habitaciones.
 * Define el contrato para algoritmos de selección de habitaciones disponibles
 * para reservas. Permite diferentes estrategias como menor uso, aleatoria, etc.
 */
public interface RoomAssignmentStrategy {

    /**
     * Asigna una cantidad requerida de habitaciones de una lista de disponibles.
     *
     * @param disponibles      lista de habitaciones disponibles para asignar
     * @param cantidadRequerida cantidad de habitaciones a asignar
     * @return lista de habitaciones asignadas según la estrategia
     */
    List<Habitacion> asignar(List<Habitacion> disponibles, int cantidadRequerida);
}
