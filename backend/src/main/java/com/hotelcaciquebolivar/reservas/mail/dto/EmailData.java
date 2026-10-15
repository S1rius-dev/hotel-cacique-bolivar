package com.hotelcaciquebolivar.reservas.mail.dto;

/**
 * DTO genérico que contiene los datos básicos necesarios para el envío de un correo electrónico.
 *
 * <p>Este record almacena la información esencial de un correo: la dirección del destinatario,
 * el asunto y el nombre del destinatario. No implementa la interfaz {@link DatosCorreo}
 * y no está diseñado para ser utilizado directamente por los servicios de envío de correos
 * del sistema; es un DTO base que puede ser utilizado para otros fines genéricos.</p>
 *
 * @param to            dirección de correo electrónico del destinatario
 * @param subject       asunto del correo electrónico
 * @param recipientName nombre completo del destinatario
 */
public record EmailData(
    String to,
    String subject,
    String recipientName
) {}
