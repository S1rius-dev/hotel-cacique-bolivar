package com.hotelcaciquebolivar.reservas.mail.dto;

import java.time.LocalDateTime;

/**
 * DTO que contiene los datos necesarios para el envío de un correo de recuperación de contraseña.
 *
 * <p>Implementa la interfaz {@link DatosCorreo} y proporciona la información específica
 * para generar el correo de restablecimiento de contraseña, incluyendo el token
 * de verificación, la fecha de expiración del token y la URL completa de
 * restablecimiento que el usuario debe visitar.</p>
 *
 * @param to               dirección de correo electrónico del destinatario
 * @param subject          asunto del correo de recuperación de contraseña
 * @param recipientName    nombre completo del destinatario
 * @param token            token único de verificación para el restablecimiento de contraseña
 * @param fechaExpiracion fecha y hora en que el token de recuperación expira
 * @param urlReset        URL completa de restablecimiento de contraseña que contiene el token
 */
public record PasswordRecoveryData(
    String to,
    String subject,
    String recipientName,
    String token,
    LocalDateTime fechaExpiracion,
    String urlReset
) implements DatosCorreo {

    /**
     * {@inheritDoc}
     */
    @Override
    public String destinatario() { return to; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asunto() { return subject; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String nombreDestinatario() { return recipientName; }
}
