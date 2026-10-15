package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que contiene el desglose completo de precios de una reserva.
 * Incluye subtotal, cargos, descuentos, consumos y total final.
 */
public record PricingBreakdown(
        /** Subtotal antes de cargos y descuentos. */
        BigDecimal subtotal,
        /** Total de cargos adicionales aplicados. */
        BigDecimal totalCargos,
        /** Total de descuentos aplicados. */
        BigDecimal totalDescuentos,
        /** Indica si se aplicó una exención de impuestos. */
        boolean exencionAplicada,
        /** Total final a pagar. */
        BigDecimal totalFinal,
        /** Lista de líneas de precio detalladas (cargos y descuentos). */
        List<PricingLine> lineas,
        /** Total de consumos en tienda asociados a la reserva. */
        BigDecimal totalConsumos,
        /**
         * Parte de {@code totalFinal} que corresponde a recepción, es decir todo
         * menos los consumos de tienda. Es la base del reparto del cobro entre
         * las cajas: el resto del total corresponde a tienda.
         *
         * <p>Se calcula aquí, y no a partir de {@code totalFinal - totalConsumos}
         * en el consumidor, porque las ramas de total fijo y exención no incluyen
         * los consumos en el total. En ambas, este valor absorbe el total completo
         * y la parte de tienda queda en cero.</p>
         */
        BigDecimal totalRecepcion
) {
    public PricingBreakdown(BigDecimal subtotal, BigDecimal totalCargos, BigDecimal totalDescuentos,
                            boolean exencionAplicada, BigDecimal totalFinal, List<PricingLine> lineas) {
        this(subtotal, totalCargos, totalDescuentos, exencionAplicada, totalFinal, lineas,
                BigDecimal.ZERO, totalFinal);
    }
}

