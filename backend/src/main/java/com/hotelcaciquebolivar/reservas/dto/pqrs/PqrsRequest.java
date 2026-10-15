package com.hotelcaciquebolivar.reservas.dto.pqrs;

import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear un nuevo PQRS (Petición, Queja, Reclamo o Sugerencia).
 * Contiene la información necesaria para registrar un PQRS en el sistema.
 */
public record PqrsRequest(
    /** Tipo de PQRS (Petición, Queja, Reclamo o Sugerencia). */
    @NotNull TipoPqrs tipo,
    /** Asunto o título del PQRS. */
    String asunto,
    /** Descripción detallada del PQRS. */
    @NotBlank String descripcion,
    /** Identificador de la reserva asociada (opcional). */
    Long reservaId,
    /** Correo electrónico de contacto del remitente. */
    String emailContacto,
    /** Número de teléfono de contacto del remitente. */
    String telefonoContacto,
    /** Nombre completo del remitente. */
    String nombreContacto
) {}
