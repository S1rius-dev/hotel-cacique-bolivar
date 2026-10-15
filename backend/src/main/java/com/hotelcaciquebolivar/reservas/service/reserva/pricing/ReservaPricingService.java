package com.hotelcaciquebolivar.reservas.service.reserva.pricing;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

public interface ReservaPricingService {

    BigDecimal calcularTotalDetalle(DetalleReserva detalle);

    BigDecimal calcularExtrasDetalle(DetalleReserva detalle);

    BigDecimal calcularTotalReserva(Reserva reserva);

    PricingBreakdown calcularDesglose(Reserva reserva);

    void congelarPreciosDetalle(DetalleReserva detalle, java.time.LocalDate checkIn, java.time.LocalDate checkOut);
}
