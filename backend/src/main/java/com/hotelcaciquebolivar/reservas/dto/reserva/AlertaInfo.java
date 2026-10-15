package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.time.LocalDateTime;

/**
 * DTO que contiene la información de una alerta asociada a un huésped o reserva.
 * Las alertas se utilizan para notificaciones importantes del sistema.
 */
public record AlertaInfo(
        /** Identificador único de la alerta. */
        Long id,
        /** Tipo de alerta (ej: vencimiento, pago pendiente, etc.). */
        String tipo,
        /** Mensaje descriptivo de la alerta. */
        String mensaje,
        /** Fecha y hora de creación de la alerta. */
        LocalDateTime fechaCreacion,
        /** Estado de la alerta (true = activa, false = resuelta). */
        Boolean estado
        ) {

}
