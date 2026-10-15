package com.hotelcaciquebolivar.reservas.dto.pago;

import java.math.BigDecimal;

/**
 * DTO de respuesta que contiene las métricas consolidadas de pagos del sistema.
 * Incluye totales recaudados, desglose por canal (Wompi y efectivo), pagos pendientes,
 * métricas del turno de caja y métricas de tienda.
 */
public record MetricasPagosResponse(
        BigDecimal recaudadoHoy,
        BigDecimal wompiMonto,
        long wompiCount,
        BigDecimal efectivoMonto,
        long efectivoCount,
        long pendientes,
        BigDecimal efectivoMontoTurno,
        BigDecimal ingresosCajaTurno,
        BigDecimal diferenciaCuadre,
        BigDecimal pendienteMonto,
        long pendienteCount,
        // Tienda (caja TIENDA)
        BigDecimal tiendaMontoHoy,
        BigDecimal tiendaMontoTurno,
        BigDecimal tiendaPendienteMonto,
        long tiendaPendienteCount
) {
    public MetricasPagosResponse(BigDecimal recaudadoHoy, BigDecimal wompiMonto, long wompiCount,
            BigDecimal efectivoMonto, long efectivoCount, long pendientes) {
        this(recaudadoHoy, wompiMonto, wompiCount, efectivoMonto, efectivoCount, pendientes, null, null, null, null, 0, BigDecimal.ZERO, null, BigDecimal.ZERO, 0);
    }

    public MetricasPagosResponse(BigDecimal recaudadoHoy, BigDecimal wompiMonto, long wompiCount,
            BigDecimal efectivoMonto, long efectivoCount, long pendientes,
            BigDecimal efectivoMontoTurno, BigDecimal ingresosCajaTurno, BigDecimal diferenciaCuadre,
            BigDecimal pendienteMonto, long pendienteCount) {
        this(recaudadoHoy, wompiMonto, wompiCount, efectivoMonto, efectivoCount, pendientes,
                efectivoMontoTurno, ingresosCajaTurno, diferenciaCuadre, pendienteMonto, pendienteCount,
                BigDecimal.ZERO, null, BigDecimal.ZERO, 0);
    }
}
