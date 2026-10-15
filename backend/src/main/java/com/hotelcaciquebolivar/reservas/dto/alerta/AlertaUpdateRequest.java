package com.hotelcaciquebolivar.reservas.dto.alerta;

import com.hotelcaciquebolivar.reservas.entity.alerta.TipoAlerta;

/**
 * DTO de solicitud para la actualización parcial de una alerta.
 * Permite modificar el tipo, mensaje y/o estado de una alerta existente.
 */
public record AlertaUpdateRequest(
        TipoAlerta tipo,
        String mensaje,
        Boolean estado
) {

}
