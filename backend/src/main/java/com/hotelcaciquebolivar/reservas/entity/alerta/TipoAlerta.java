package com.hotelcaciquebolivar.reservas.entity.alerta;

/**
 * Enumeración que define los tipos de alerta que pueden asociarse a un cliente.
 * Cada tipo representa una categoría de comportamiento o situación que requiere
 * seguimiento por parte del personal del hotel.
 */
public enum TipoAlerta {
    /** Cliente con problemas de pago o situaciones económicas que requieren atención. */
    ECONOMICO,
    /** Cliente involucrado en hurtos o actos delictivos. */
    HURTO,
    /** Cliente con comportamiento conflictivo o problemático. */
    CONFLICTIVO,
    /** Cliente fumador que puede afectar habitaciones o áreas comunes. */
    FUMADOR,
    /** Cliente que ha causado daños a instalaciones o mobiliario. */
    DANOS

}
