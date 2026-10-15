package com.hotelcaciquebolivar.reservas.mail.dto;

/**
 * Interfaz que define el contrato común para todos los DTOs de datos de correo electrónico.
 *
 * <p>Cada DTO de correo que implementa esta interfaz debe proporcionar la información
 * básica necesaria para el envío: dirección del destinatario, asunto del correo
 * y nombre del destinatario. Esta interfaz permite que el servicio de plantillas
 * ({@link com.hotelcaciquebolivar.reservas.mail.EmailTemplateService}) y el servicio
 * de envío ({@link com.hotelcaciquebolivar.reservas.mail.SmtpEmailService}) trabajen
 * de forma genérica con cualquier tipo de dato de correo.</p>
 */
public interface DatosCorreo {

    /**
     * Obtiene la dirección de correo electrónico del destinatario.
     *
     * @return dirección de correo del destinatario
     */
    String destinatario();

    /**
     * Obtiene el asunto (subject) del correo electrónico.
     *
     * @return asunto del correo
     */
    String asunto();

    /**
     * Obtiene el nombre completo del destinatario del correo.
     *
     * @return nombre del destinatario
     */
    String nombreDestinatario();
}
