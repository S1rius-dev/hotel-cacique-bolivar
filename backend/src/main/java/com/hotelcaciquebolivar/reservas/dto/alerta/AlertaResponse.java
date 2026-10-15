package com.hotelcaciquebolivar.reservas.dto.alerta;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una alerta completa con su información de cliente y estado.
 * Se utiliza para la transferencia de datos de alertas hacia el cliente HTTP.
 */
public record AlertaResponse(
        Long id,
        Long clienteId,
        String clienteNombre,
        String tipo,
        String mensaje,
        Boolean estado,
        LocalDateTime fechaCreacion
) {

}
