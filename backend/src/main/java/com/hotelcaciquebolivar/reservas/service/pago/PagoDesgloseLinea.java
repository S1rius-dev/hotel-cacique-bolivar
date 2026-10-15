package com.hotelcaciquebolivar.reservas.service.pago;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;

/**
 * Línea de reparto calculada para un pago, antes de persistirse.
 *
 * @param origen caja de destino del dinero
 * @param monto cantidad asignada a esta línea
 * @param concepto descripción legible del cobro
 * @param consumoId consumo de tienda al que se atribuye la línea, nulo si es de recepción
 */
public record PagoDesgloseLinea(
        OrigenCobro origen,
        BigDecimal monto,
        String concepto,
        Long consumoId
) {
}
