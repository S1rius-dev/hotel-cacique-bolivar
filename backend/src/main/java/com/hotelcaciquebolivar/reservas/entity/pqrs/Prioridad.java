package com.hotelcaciquebolivar.reservas.entity.pqrs;

/**
 * Enumeración que define los niveles de prioridad para las PQRS.
 */
public enum Prioridad {
    /** Prioridad baja. Se atiende cuando hay disponibilidad. */
    BAJA,
    /** Prioridad media. Se atiende en un plazo razonable. */
    MEDIA,
    /** Prioridad alta. Se requiere atención pronta. */
    ALTA,
    /** Prioridad urgente. Requiere atención inmediata. */
    URGENTE
}
