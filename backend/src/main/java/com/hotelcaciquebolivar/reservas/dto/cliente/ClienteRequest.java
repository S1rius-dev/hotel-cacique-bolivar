package com.hotelcaciquebolivar.reservas.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la creación de un nuevo cliente.
 * Contiene los datos personales, tipo de identificación, municipio de expedición y teléfono del cliente.
 */
public record ClienteRequest(
        @NotBlank(message = "{ClienteRequest.cedula.notblank}")
        @Size(max = 15, message = "{ClienteRequest.cedula.size}")
        String cedula,
        @NotNull()
        Long tipoIdentificacionId,
        @NotNull()
        Long municipioExpedicionId,
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
        Long usuarioId
        ) {

}
