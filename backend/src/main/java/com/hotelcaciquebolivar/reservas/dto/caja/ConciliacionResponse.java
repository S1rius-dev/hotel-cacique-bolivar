package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cuadre de las cajas del hotel en un período.
 *
 * <p>Se calcula sobre los movimientos de caja, no sobre los pagos, para que
 * incluya tanto el efectivo cobrado a las reservas como la venta de mostrador en
 * tienda, que no genera pago pero sí movimiento de caja.</p>
 *
 * @param desde inicio del período, inclusive
 * @param hasta fin del período, inclusive
 * @param totalRecepcion ingresos netos acumulados de la caja de recepción
 * @param totalTienda ingresos netos acumulados de la caja de tienda
 * @param cajas desglose por caja
 */
public record ConciliacionResponse(
        LocalDateTime desde,
        LocalDateTime hasta,
        BigDecimal totalRecepcion,
        BigDecimal totalTienda,
        List<ConciliacionCajaResponse> cajas
) {
}
