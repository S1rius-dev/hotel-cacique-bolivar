package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;

/**
 * DTO que representa una reserva activa asociada a una habitación.
 * Utilizado en el panel diario de habitaciones para mostrar la reserva en curso.
 */
public record ReservaActiva(
        /** Identificador de la reserva. */
        Long reservaId,
        /** Identificador del detalle de reserva (habitación). */
        Long detalleId,
        /** Código único de la reserva. */
        String codigoReserva,
        /** Nombre del titular de la reserva. */
        String titularNombre,
        /** Fecha de check-in. */
        LocalDate checkIn,
        /** Fecha de check-out. */
        LocalDate checkOut,
        /** Estado actual de la reserva. */
        EstadoReserva estadoReserva,
        /** Cantidad de ocupantes en la habitación. */
        Integer cantidadOcupantes,
        /** Precio por noche de la habitación. */
        BigDecimal precioPorNoche,
        /** Subtotal de la reserva. */
        BigDecimal subtotal,
        /** Total de la reserva. */
        BigDecimal total,
        /** Monto total pagado. */
        BigDecimal totalPagado,
        /** Saldo pendiente de pago. */
        BigDecimal saldoPendiente,
        /** Lista de huéspedes asignados a la habitación. */
        List<HuespedResponse> huespedes
) {
}
