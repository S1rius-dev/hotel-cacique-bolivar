package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;

/**
 * DTO de respuesta que contiene la información completa de una reserva.
 * Incluye datos del titular, fechas, detalles de habitaciones, ajustes, pagos y desglose de precios.
 */
public record ReservaResponse(
        /** Identificador único de la reserva. */
        Long id,
        /** Código único generado para la reserva. */
        String codigoReserva,
        /** Información del titular de la reserva. */
        TitularInfo titular,
        /** Fecha y hora de creación de la reserva. */
        LocalDateTime fechaCreacion,
        /** Fecha y hora de expiración del pago. */
        LocalDateTime fechaExpiracionPago,
        /** Fecha programada de check-in. */
        LocalDate fechaCheckIn,
        /** Fecha programada de check-out. */
        LocalDate fechaCheckOut,
        /** Hora real del check-in (si se realizó). */
        LocalDateTime horaCheckIn,
        /** Hora real del check-out (si se realizó). */
        LocalDateTime horaCheckOut,
        /** Cantidad de noches de la reserva. */
        Long noches,
        /** Estado actual de la reserva. */
        EstadoReserva estado,
        /** Origen de la reserva (walk-in, teléfono, web, etc.). */
        OrigenReserva origen,
        /** Monto total de la reserva. */
        BigDecimal total,
        /** Monto total pagado. */
        BigDecimal totalPagado,
        /** Saldo pendiente de pago. */
        BigDecimal saldoPendiente,
        /** Observaciones generales de la reserva. */
        String observaciones,
        /** Lista de detalles de habitaciones reservadas. */
        List<DetalleReservaResponse> detalles,
        /** Lista de ajustes de precio aplicados. */
        List<AjusteResponse> ajustes,
        /** Lista de pagos realizados. */
        List<PagoResponse> pagos,
        /** Desglose detallado de precios. */
        PricingBreakdown desglose
        ) {

}
