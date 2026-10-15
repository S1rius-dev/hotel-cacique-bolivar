package com.hotelcaciquebolivar.reservas.mail.dto;

import java.time.LocalDateTime;

/**
 * DTO que contiene los datos necesarios para el envío de un correo de respuesta a una solicitud PQRS.
 *
 * <p>Implementa la interfaz {@link DatosCorreo} y proporciona la información específica
 * para generar el correo de notificación de respuesta a una PQRS (Petición, Queja,
 * Reclamo o Sugerencia), incluyendo el código de la PQRS, el tipo de solicitud,
 * el asunto original, la respuesta proporcionada y la fecha de respuesta.</p>
 *
 * @param to             dirección de correo electrónico del destinatario
 * @param subject        asunto del correo de respuesta PQRS
 * @param recipientName  nombre completo del destinatario
 * @param codigoPqrs     código único de identificación de la solicitud PQRS
 * @param tipo           tipo de solicitud PQRS (Petición, Queja, Reclamo o Sugerencia)
 * @param asunto         asunto original de la solicitud PQRS
 * @param respuesta      respuesta proporcionada por el hotel a la solicitud PQRS
 * @param fechaRespuesta fecha y hora en que se registró la respuesta a la PQRS
 */
public record PqrsRespuestaData(
    String to,
    String subject,
    String recipientName,
    String codigoPqrs,
    String tipo,
    String asunto,
    String respuesta,
    LocalDateTime fechaRespuesta
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
