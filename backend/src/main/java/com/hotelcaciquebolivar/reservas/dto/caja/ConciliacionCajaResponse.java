package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;

/**
 * Cuadre de una caja en un período: lo que entró, lo que salió y el saldo.
 *
 * @param caja nombre de la caja
 * @param ingresos suma de los ingresos del período
 * @param gastos suma de los gastos del período
 * @param balance ingresos menos gastos
 */
public record ConciliacionCajaResponse(
        String caja,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal balance
) {
}
