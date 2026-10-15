package com.hotelcaciquebolivar.reservas.dto.notificacion;

import java.time.LocalDateTime;
import java.util.Map;

import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;

/**
 * DTO de respuesta que representa una notificación completa del sistema.
 * Contiene tipo, mensaje, origen, detalles adicionales, referencia, estado de lectura y fechas de creación y lectura.
 */
public record NotificacionResponse(
    Long id,
    Long usuarioId,
    TipoNotificacion tipo,
    String mensaje,
    String origen,
    String origenNombre,
    Map<String, Object> detalles,
    Long referenciaId,
    String referenciaTipo,
    boolean leida,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaLectura
) {}
