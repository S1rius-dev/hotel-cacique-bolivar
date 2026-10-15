package com.hotelcaciquebolivar.reservas.dto.pqrs;

import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;

/**
 * DTO de solicitud para actualizar la información de un PQRS existente.
 * Todos los campos son opcionales; solo se actualizan los campos proporcionados.
 */
public record PqrsUpdateRequest(
    /** Nuevo tipo de PQRS. */
    TipoPqrs tipo,
    /** Nuevo asunto o título. */
    String asunto,
    /** Nueva descripción detallada. */
    String descripcion,
    /** Nuevo correo electrónico de contacto. */
    String emailContacto,
    /** Nuevo número de teléfono de contacto. */
    String telefonoContacto,
    /** Nuevo nombre del remitente. */
    String nombreContacto
) {}
