package com.hotelcaciquebolivar.reservas.dto.reserva;

/**
 * DTO que contiene la información personal del titular de una reserva.
 * Utilizado para identificar y contactar al responsable de la reserva.
 */
public record TitularInfo(
        /** Identificador único del titular. */
        Long id,
        /** Número de cédula o identificación del titular. */
        String cedula,
        /** Tipo de documento de identificación. */
        String tipoIdentificacion,
        /** Nombre(s) del titular. */
        String nombreCompleto,
        /** Apellido(s) del titular. */
        String apellidoCompleto,
        /** Número de teléfono del titular. */
        String telefono
) {

}
