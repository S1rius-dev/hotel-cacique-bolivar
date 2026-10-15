package com.hotelcaciquebolivar.reservas.dto.cliente;

import java.time.LocalDateTime;
import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.catalogo.MunicipioDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.TipoIdentificacionDTO;


/**
 * DTO de respuesta que representa un cliente completo con toda su información.
 * Incluye datos personales, tipo de identificación, municipio de expedición, estado, usuario creador y lista de reservas asociadas.
 */
public record ClienteResponse(
        Long id,
        String cedula,
        TipoIdentificacionDTO tipoIdentificacion,
        MunicipioDTO municipioExpedicion,
        String nombreCompleto,
        String apellidoCompleto,
        String telefono,
        String observaciones,
        Boolean activo,
        LocalDateTime createdAt,
        Long usuarioId,
        String usuarioUsername,
        String usuarioEmail,
        List<Long> reservasIds
        ) {

}
