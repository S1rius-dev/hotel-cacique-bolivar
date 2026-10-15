package com.hotelcaciquebolivar.reservas.dto.alerta;

import com.hotelcaciquebolivar.reservas.entity.alerta.TipoAlerta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para la creación de una alerta.
 * Contiene la información necesaria para registrar una alerta asociada a un cliente.
 */
public record AlertaRequest(
        @NotNull Long clienteId,
        @NotNull TipoAlerta tipo,
        @NotBlank String mensaje
) {

}
