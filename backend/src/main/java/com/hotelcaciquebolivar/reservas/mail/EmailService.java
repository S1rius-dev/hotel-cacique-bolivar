package com.hotelcaciquebolivar.reservas.mail;

import com.hotelcaciquebolivar.reservas.mail.dto.PasswordRecoveryData;
import com.hotelcaciquebolivar.reservas.mail.dto.PqrsRespuestaData;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaCanceladaData;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaConfirmadaData;

/**
 * Interfaz que define el contrato para el envío de correos electrónicos del sistema de reservas.
 *
 * <p>Proporciona métodos para el envío de diferentes tipos de correos relacionados
 * con la gestión de reservas del hotel, como confirmaciones, cancelaciones,
 * respuestas a PQRS y recuperación de contraseñas.</p>
 *
 * <p>La implementación principal se encuentra en {@link SmtpEmailService}, la cual
 * envía los correos de forma asíncrona utilizando el protocolo SMTP a través
 * de Spring Boot's {@code JavaMailSender}.</p>
 */
public interface EmailService {

    /**
     * Envía un correo de confirmación de reserva al cliente.
     *
     * @param data datos de la reserva confirmada, incluyendo código, fechas,
     *             habitaciones, montos y estado de la reserva
     */
    void sendReservationConfirmation(ReservaConfirmadaData data);

    /**
     * Envía un correo de notificación de cancelación de reserva al cliente.
     *
     * @param data datos de la reserva cancelada, incluyendo código, fecha de cancelación,
     *             monto reembolsado y motivo de la cancelación
     */
    void sendReservationCancellation(ReservaCanceladaData data);

    /**
     * Envía un correo con la respuesta a una solicitud PQRS (Peticiones, Quejas,
     * Reclamos y Sugerencias) al cliente.
     *
     * @param data datos de la respuesta PQRS, incluyendo código, tipo, asunto,
     *             respuesta y fecha de respuesta
     */
    void sendPqrsResponse(PqrsRespuestaData data);

    /**
     * Envía un correo de recuperación de contraseña al usuario.
     *
     * @param data datos de recuperación de contraseña, incluyendo token,
     *             fecha de expiración y URL de restablecimiento
     */
    void sendPasswordRecovery(PasswordRecoveryData data);
}
