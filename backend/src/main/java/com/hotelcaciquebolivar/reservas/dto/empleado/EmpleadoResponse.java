package com.hotelcaciquebolivar.reservas.dto.empleado;

import java.time.LocalDate;

/**
 * DTO de respuesta que representa un empleado con toda su información.
 * Incluye datos personales, cargo, fecha de ingreso, estado y datos del usuario asociado.
 */
public record EmpleadoResponse(
        Long id,
        String nombres,
        String apellidos,
        String telefono,
        String cargo,
        LocalDate fechaIngreso,
        Boolean activo,
        Long usuarioId,
        String usuarioUsername,
        String usuarioEmail
        ) {

}
