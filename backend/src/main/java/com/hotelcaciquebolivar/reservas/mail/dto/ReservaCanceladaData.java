package com.hotelcaciquebolivar.reservas.mail.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que contiene los datos necesarios para el envío de un correo de notificación de cancelación de reserva.
 *
 * <p>Implementa la interfaz {@link DatosCorreo} y proporciona la información específica
 * para generar el correo de notificación cuando una reserva ha sido cancelada,
 * incluyendo el código de la reserva, la fecha de cancelación, el monto total
 * reembolsado al cliente y el motivo de la cancelación.</p>
 *
 * @param to               dirección de correo electrónico del destinatario
 * @param subject          asunto del correo de cancelación de reserva
 * @param recipientName    nombre completo del destinatario
 * @param codigoReserva    código único de identificación de la reserva cancelada
 * @param fechaCancelacion fecha y hora en que se efectuó la cancelación de la reserva
 * @param totalReembolsado monto total reembolsado al cliente como resultado de la cancelación
 * @param motivo           motivo o razón por la cual se canceló la reserva
 */
public record ReservaCanceladaData(
    String to,
    String subject,
    String recipientName,
    String codigoReserva,
    LocalDateTime fechaCancelacion,
    BigDecimal totalReembolsado,
    String motivo
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
