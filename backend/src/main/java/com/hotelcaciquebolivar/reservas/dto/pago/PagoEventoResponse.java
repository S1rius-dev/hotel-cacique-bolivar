package com.hotelcaciquebolivar.reservas.dto.pago;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un evento de pago para el timeline del dashboard.
 * Contiene descripción, subtítulo y marca de tiempo del evento.
 */
public record PagoEventoResponse(
        Long id,
        String descripcion,
        String subtitulo,
        LocalDateTime timestamp
) {}
