package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;

/**
 * Línea del reparto de un pago entre las cajas, sin persistir.
 * Es lo que la interfaz muestra como vista previa antes de confirmar el cobro.
 *
 * @param origen caja de destino del dinero
 * @param monto cantidad asignada a esta caja
 * @param concepto descripción legible del cobro
 * @param consumoId consumo de tienda al que se atribuye, nulo si es de recepción
 */
public record PagoDesgloseLineaResponse(
        OrigenCobro origen,
        BigDecimal monto,
        String concepto,
        Long consumoId
) {
}
