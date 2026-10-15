package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear un nuevo detalle de reserva (habitación dentro de una reserva).
 * Contiene la información de la habitación asignada y los ocupantes.
 */
public record DetalleReservaRequest(
        /** Identificador de la habitación a asignar. */
        @NotNull(message = "{DetalleReservaRequest.habitacionId.notnull}")
        Long habitacionId,
        /** Precio por noche de la habitación. */
        @NotNull(message = "{DetalleReservaRequest.precioPorNoche.notnull}")
        BigDecimal precioPorNoche,
        /** Cantidad de ocupantes en la habitación (mínimo 1). */
        @NotNull(message = "{DetalleReservaRequest.cantidadOcupantes.notnull}")
        @Min(value = 1, message = "{DetalleReservaRequest.cantidadOcupantes.min}")
        Integer cantidadOcupantes,
        /** Observaciones o notas sobre la habitación. */
        String observaciones,
        /** Indica si el precio a aplicar incluye IVA. Opcional, por defecto se puede definir en servicio. */
        Boolean precioIncluyeIva
        ) {

}
