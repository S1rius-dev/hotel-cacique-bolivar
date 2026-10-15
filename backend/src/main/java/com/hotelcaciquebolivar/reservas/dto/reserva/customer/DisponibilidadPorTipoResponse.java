package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.math.BigDecimal;

/**
 * DTO de respuesta que contiene la disponibilidad de habitaciones por tipo.
 * Utilizado para mostrar al cliente las opciones disponibles para su reserva.
 */
public record DisponibilidadPorTipoResponse(
    /** Identificador del tipo de habitación. */
    Long tipoHabitacionId,
    /** Cantidad de habitaciones disponibles de este tipo. */
    int habitacionesDisponibles,
    /** Total de habitaciones de este tipo en el hotel. */
    int totalHabitaciones,
    /** Precio efectivo por noche (con ajustes aplicados). */
    BigDecimal precioNocheEfectivo,
    /** Precio efectivo por persona extra por noche. */
    BigDecimal precioPersonaExtraEfectivo,
    /** Total estimado por noche para el tipo de habitación. */
    BigDecimal totalEstimadoPorNoche
) {}
