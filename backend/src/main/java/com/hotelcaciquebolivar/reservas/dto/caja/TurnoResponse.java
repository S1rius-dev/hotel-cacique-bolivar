package com.hotelcaciquebolivar.reservas.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta que representa un turno de caja completo.
 * Incluye información de apertura, cierre, totales de ingresos, egresos, diferencia de cuadre y movimientos asociados.
 */
public record TurnoResponse(
        Long id,
        String cajaNombre,
        String tipoTurno,
        String tipoTurnoDisplay,
        String usuarioApertura,
        String usuarioCierre,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        BigDecimal fondoInicial,
        BigDecimal totalIngresosEfectivo,
        BigDecimal totalEgresos,
        BigDecimal esperadoCierre,
        BigDecimal contadoFinal,
        BigDecimal diferencia,
        String estado,
        String observacionesCierre,
        List<MovimientoResponse> movimientos) {

}
