package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.TipoPago;

/**
 * DTO de respuesta que representa un pago completo con toda su información.
 * Incluye código de pago, reserva asociada, monto, tipo, método, estado, referencia externa y fecha de pago.
 *
 * @param desglose reparto del monto entre las cajas, vacío si el pago no fue en efectivo
 */
public record PagoResponse(
        Long id,
        String codigoPago,
        Long reservaId,
        String codigoReserva,
        BigDecimal monto,
        TipoPago tipoPago,
        MetodoPago metodoPago,
        EstadoPago estado,
        String referenciaExterna,
        String observaciones,
        LocalDateTime fechaPago,
        List<PagoDesgloseResponse> desglose
) {
    public PagoResponse(Long id, String codigoPago, Long reservaId, String codigoReserva,
            BigDecimal monto, TipoPago tipoPago, MetodoPago metodoPago, EstadoPago estado,
            String referenciaExterna, String observaciones, LocalDateTime fechaPago) {
        this(id, codigoPago, reservaId, codigoReserva, monto, tipoPago, metodoPago, estado,
                referenciaExterna, observaciones, fechaPago, List.of());
    }
}
