package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;

/**
 * DTO de respuesta que contiene la información completa de un detalle de reserva (habitación).
 * Incluye datos de la habitación, precios, ocupantes y huéspedes asignados.
 */
public record DetalleReservaResponse(
        /** Identificador único del detalle de reserva. */
        Long id,
        /** Identificador de la habitación asignada. */
        Long habitacionId,
        /** Número de la habitación. */
        String habitacionNumero,
        /** Tipo de la habitación. */
        String habitacionTipo,
        /** Estado actual del detalle de reserva. */
        EstadoDetalleReserva estado,
        /** Precio por noche de la habitación. */
        BigDecimal precioPorNoche,
        /** Cantidad de ocupantes registrados. */
        Integer cantidadOcupantes,
        /** Cantidad de personas extras (sobrepasa la capacidad base). */
        int personasExtras,
        /** Precio por persona extra por noche. */
        BigDecimal precioPersonaExtra,
        /** Precio por persona extra al momento de la reserva (snapshot). */
        BigDecimal precioPersonaExtraSnapshot,
        /** Total cobrado por personas extras. */
        BigDecimal precioPersonaExtraTotal,
        /** Subtotal del detalle (habitación + personas extras). */
        BigDecimal subtotal,
        /** Capacidad base de la habitación (sin costo adicional). */
        Integer capacidadBase,
        /** Capacidad máxima de la habitación. */
        Integer capacidadMaxima,
        /** Observaciones o notas sobre la habitación. */
        String observaciones,
        /** Lista de huéspedes asignados a la habitación. */
        List<HuespedResponse> huespedes,
        /** Desglose congelado de precios (JSON serializado). */
        String desgloseCongelado,
        Boolean precioUsadoIncluyeIva,
        BigDecimal precioUnitarioBase,
        BigDecimal precioUnitarioConIva,
        BigDecimal precioUnitarioAplicado,
        BigDecimal subtotalDetalle,
        BigDecimal ivaTotalDetalle,
        BigDecimal totalDetalle
) {
}
