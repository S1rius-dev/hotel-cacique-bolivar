package com.hotelcaciquebolivar.reservas.dto.pqrs;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que contiene la información completa de un PQRS.
 * Incluye datos del PQRS, usuario creador, reserva asociada y fechas relevantes.
 */
public record PqrsResponse(
    /** Identificador único del PQRS. */
    Long id,
    /** Código único generado para el PQRS. */
    String codigo,
    /** Tipo de PQRS (Petición, Queja, Reclamo o Sugerencia). */
    String tipo,
    /** Nivel de prioridad asignado al PQRS. */
    String prioridad,
    /** Estado actual del PQRS en el flujo de atención. */
    String estado,
    /** Asunto o título del PQRS. */
    String asunto,
    /** Descripción detallada del PQRS. */
    String descripcion,
    /** Respuesta proporcionada al PQRS. */
    String respuesta,
    /** Fecha y hora de creación del PQRS. */
    LocalDateTime fechaCreacion,
    /** Fecha y hora de la última actualización del PQRS. */
    LocalDateTime fechaActualizacion,
    /** Fecha y hora de la respuesta al PQRS. */
    LocalDateTime fechaRespuesta,
    /** Identificador del usuario que creó el PQRS. */
    Long usuarioId,
    /** Correo electrónico del usuario creador. */
    String usuarioEmail,
    /** Nombre del usuario creador. */
    String usuarioNombre,
    /** Identificador de la reserva asociada al PQRS. */
    Long reservaId,
    /** Código de la reserva asociada. */
    String codigoReserva,
    /** Correo electrónico de contacto del remitente. */
    String emailContacto,
    /** Número de teléfono de contacto del remitente. */
    String telefonoContacto,
    /** Nombre completo del remitente. */
    String nombreContacto
) {}
