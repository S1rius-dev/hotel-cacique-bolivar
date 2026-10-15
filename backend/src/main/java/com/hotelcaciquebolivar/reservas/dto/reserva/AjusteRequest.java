package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo;
import com.hotelcaciquebolivar.reservas.entity.reserva.TipoAjuste;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear un ajuste de precio en una reserva.
 * Los ajustes pueden ser descuentos o cargos adicionales, calculados en porcentaje o valor fijo.
 */
public record AjusteRequest(
        /** Tipo de ajuste (descuento o cargo). */
        @NotNull TipoAjuste tipoAjuste,
        /** Modo de cálculo del ajuste (porcentaje o valor fijo). */
        @NotNull ModoCalculo modoCalculo,
        /** Concepto o descripción del ajuste. */
        String concepto,
        /** Valor del ajuste (porcentaje o monto fijo según el modo de cálculo). */
        BigDecimal valor
) {
}
