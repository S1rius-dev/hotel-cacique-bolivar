package com.hotelcaciquebolivar.reservas.dto.cliente;

/**
 * DTO de respuesta pública con información básica de un cliente.
 * Expone únicamente datos personales limitados para uso externo, excluyendo datos sensibles como observaciones e ID de usuario.
 */
public record ClientePublicResponse(
        Long id,
        String cedula,
        String nombreCompleto,
        String apellidoCompleto,
        String telefono,
        String email
) {

}
