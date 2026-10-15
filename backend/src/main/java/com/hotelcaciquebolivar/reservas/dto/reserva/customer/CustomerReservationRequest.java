package com.hotelcaciquebolivar.reservas.dto.reserva.customer;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para que un cliente Cree una reserva desde la interfaz pública.
 * Contiene fechas, cantidad de huéspedes y tipos de habitación deseados.
 */
public record CustomerReservationRequest(
    /** Fecha de check-in deseada. */
    @NotNull LocalDate fechaCheckIn,
    /** Fecha de check-out deseada. */
    @NotNull LocalDate fechaCheckOut,
    /** Cantidad total de huéspedes (mínimo 1). */
    @NotNull @Min(1) Integer cantidadHuespedes,
    /** Lista de tipos de habitación solicitados. */
    @NotEmpty @Valid List<CustomerTipoRequest> tiposHabitacion,
    /** Identificador del titular de la reserva. */
    @NotNull Long titularId,
    /** Observaciones adicionales de la reserva. */
    String observaciones
) {}
