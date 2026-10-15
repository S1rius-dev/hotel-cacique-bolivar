package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo;
import com.hotelcaciquebolivar.reservas.entity.reserva.TipoAjuste;

/**
 * DTO que representa una línea individual en el desglose de precios de una reserva.
 * Puede representar un cargo adicional o un descuento aplicado.
 */
public record PricingLine(
        /** Identificador del ajuste asociado. */
        Long ajusteId,
        /** Tipo de ajuste (cargo o descuento). */
        TipoAjuste tipoAjuste,
        /** Modo de cálculo del ajuste (porcentaje o valor fijo). */
        ModoCalculo modoCalculo,
        /** Concepto o descripción del ajuste. */
        String concepto,
        /** Valor del ajuste (porcentaje o monto). */
        BigDecimal valor,
        /** Impacto económico calculado del ajuste en el total. */
        BigDecimal impacto
) {
}
