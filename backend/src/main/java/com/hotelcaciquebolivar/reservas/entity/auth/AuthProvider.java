package com.hotelcaciquebolivar.reservas.entity.auth;

/**
 * Enumeración que define los proveedores de autenticación soportados por el sistema.
 * Determina cómo se autentica un usuario para acceder a la aplicación.
 */
public enum AuthProvider {
    /** Autenticación local con correo electrónico y contraseña. */
    LOCAL,
    /** Autenticación mediante cuenta de Google. */
    GOOGLE,
    /** Autenticación mediante cuenta de Facebook. */
    FACEBOOK
}
