package com.hotelcaciquebolivar.reservas.dto.habitacion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear un precio temporal para un tipo de habitación.
 * Contiene el ID del tipo de habitación, rango de fechas, precio por noche y precio por persona extra.
 */
public record PrecioTemporalRequest(

        @NotNull(message = "{PrecioTemporalRequest.tipoHabitacionId.notnull}")
        Long tipoHabitacionId,

        @NotNull(message = "{PrecioTemporalRequest.fechaInicio.notnull}")
        LocalDate fechaInicio,

        @NotNull(message = "{PrecioTemporalRequest.fechaFin.notnull}")
        LocalDate fechaFin,

        @NotNull(message = "{PrecioTemporalRequest.precioNoche.notnull}")
        @DecimalMin(value = "0.01", message = "{PrecioTemporalRequest.precioNoche.min}")
        BigDecimal precioNoche,

        @NotNull(message = "{PrecioTemporalRequest.precioPersonaExtra.notnull}")
        @DecimalMin(value = "0.01", message = "{PrecioTemporalRequest.precioPersonaExtra.min}")
        BigDecimal precioPersonaExtra,

        /** Precio por noche con IVA (valor impuesto almacenado). */
        @NotNull(message = "{PrecioTemporalRequest.precioNocheIva.notnull}")
        @DecimalMin(value = "0.00", message = "{PrecioTemporalRequest.precioNocheIva.min}")
        BigDecimal precioNocheIva
) {}