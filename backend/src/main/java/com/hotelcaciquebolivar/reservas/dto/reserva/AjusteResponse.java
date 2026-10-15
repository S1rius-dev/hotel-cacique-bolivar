package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.math.BigDecimal;

import com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo;
import com.hotelcaciquebolivar.reservas.entity.reserva.TipoAjuste;

/**
 * DTO de respuesta que contiene la información de un ajuste de precio aplicado a una reserva.
 */
public record AjusteResponse(
        /** Identificador único del ajuste. */
        Long id,
        /** Tipo de ajuste (descuento o cargo). */
        TipoAjuste tipoAjuste,
        /** Modo de cálculo del ajuste. */
        ModoCalculo modoCalculo,
        /** Concepto o descripción del ajuste. */
        String concepto,
        /** Valor del ajuste. */
        BigDecimal valor,
        /** Indica si el ajuste está activo. */
        Boolean activo
) {
}
