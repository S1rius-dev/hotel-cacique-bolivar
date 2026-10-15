package com.hotelcaciquebolivar.reservas.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la actualización de datos de un cliente existente.
 * Permite modificar nombre, apellido, teléfono, observaciones y estado de actividad.
 */
public record ClienteUpdateRequest(
        @NotBlank(message = "{ClienteRequest.nombreCompleto.notblank}")
        @Size(max = 30, message = "{ClienteRequest.nombreCompleto.size}")
        String nombreCompleto,
        @NotBlank(message = "{ClienteRequest.apellidoCompleto.notblank}")
        @Size(max = 30, message = "{ClienteRequest.apellidoCompleto.size}")
        String apellidoCompleto,
        @Size(max = 10, message = "{ClienteRequest.telefono.size}")
        @Pattern(regexp = "^[0-9]*$", message = "{ClienteRequest.telefono.pattern}")
        String telefono,
        @Size(max = 255, message = "{ClienteRequest.observaciones.size}")
        String observaciones,
        Boolean activo
        ) {

}
