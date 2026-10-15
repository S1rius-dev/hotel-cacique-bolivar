package com.hotelcaciquebolivar.reservas.dto.empleado;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para la actualización de datos de un empleado existente.
 * Permite modificar nombres, apellidos, teléfono, cargo, fecha de ingreso, estado y usuario asociado.
 */
public record EmpleadoUpdateRequest(
        @NotBlank(message = "{EmpleadoRequest.nombres.notblank}")
        @Size(max = 100, message = "{EmpleadoRequest.nombres.size}")
        String nombres,
        @NotBlank(message = "{EmpleadoRequest.apellidos.notblank}")
        @Size(max = 100, message = "{EmpleadoRequest.apellidos.size}")
        String apellidos,
        @Size(max = 10, message = "{EmpleadoRequest.telefono.size}")
        String telefono,
        @NotBlank(message = "{EmpleadoRequest.cargo.notblank}")
        @Size(max = 50, message = "{EmpleadoRequest.cargo.size}")
        String cargo,
        @NotNull(message = "{EmpleadoRequest.fechaIngreso.notnull}")
        LocalDate fechaIngreso,
        Boolean activo,
        Long usuarioId
        ) {

}
