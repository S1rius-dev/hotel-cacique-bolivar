package com.hotelcaciquebolivar.reservas.entity.pqrs;

/**
 * Enumeración que define los tipos de PQRS (Petición, Queja, Reclamo o Sugerencia).
 */
public enum TipoPqrs {
    /** Queja por un servicio o situación no satisfactoria. */
    QUEJA,
    /** Petición formal de información o acción. */
    PETICION,
    /** Reclamo por un problema específico que requiere solución. */
    RECLAMO,
    /** Sugerencia de mejora para los servicios del hotel. */
    SUGERENCIA,
    /** Felicitación por un buen servicio recibido. */
    FELICITACION,
    /** Reporte técnico sobre fallas en instalaciones o equipos. */
    REPORTE_TECNICO
}
