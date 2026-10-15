package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;

/**
 * Línea de desglose ya persistida de un pago.
 *
 * @param id identificador de la línea
 * @param origen caja de destino del dinero
 * @param monto cantidad asignada a esta caja
 * @param concepto descripción legible del cobro
 * @param consumoId consumo de tienda al que se atribuye, nulo si es de recepción
 */
public record PagoDesgloseResponse(
        Long id,
        OrigenCobro origen,
        BigDecimal monto,
        String concepto,
        Long consumoId
) {
}
