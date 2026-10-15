package com.hotelcaciquebolivar.reservas.dto.reserva;

import java.util.List;

/**
 * DTO de respuesta que contiene la información de un huésped asignado a una habitación.
 * Incluye datos personales y alertas asociadas al huésped.
 */
public record HuespedResponse(
        /** Identificador de la relación huésped-detalle de reserva. */
        Long detalleHuespedId,
        /** Identificador único del huésped. */
        Long huespedId,
        /** Nombre(s) del huésped. */
        String nombreCompleto,
        /** Apellido(s) del huésped. */
        String apellidoCompleto,
        /** Número de cédula o identificación del huésped. */
        String cedula,
        /** Tipo de documento de identificación. */
        String tipoIdentificacion,
        /** Lista de alertas asociadas al huésped. */
        List<AlertaInfo> alertas
) {

}
