package com.hotelcaciquebolivar.reservas.entity.auth;

/**
 * Enumeración que define los roles disponibles en el sistema de reservas.
 * Un usuario puede tener uno o más roles simultáneamente, lo que determina
 * sus permisos y funcionalidades accesibles.
 */
public enum Rol {
    /** Administrador del sistema con acceso total a todas las funcionalidades. */
    ADMIN,
    /** Recepcionista con permisos para gestionar reservas, check-in, check-out y pagos. */
    RECEPCIONISTA,
    /** Cliente registrado que puede realizar reservas y consultar su información. */
    CLIENTE
}
