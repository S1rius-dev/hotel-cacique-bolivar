package com.hotelcaciquebolivar.reservas.mail.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que contiene los datos necesarios para el envío de un correo de confirmación de reserva.
 *
 * <p>Implementa la interfaz {@link DatosCorreo} y proporciona la información completa
 * de una reserva confirmada para la generación del correo de confirmación al cliente,
 * incluyendo código de reserva, fechas de check-in y check-out, número de noches,
 * desglose de montos (total, pagado en línea, saldo a pagar en hotel),
 * lista de habitaciones asignadas y estado actual de la reserva.</p>
 *
 * @param to              dirección de correo electrónico del destinatario
 * @param subject         asunto del correo de confirmación de reserva
 * @param recipientName   nombre completo del destinatario
 * @param codigoReserva   código único de identificación de la reserva
 * @param checkIn         fecha de entrada al hotel
 * @param checkOut        fecha de salida del hotel
 * @param noches          número total de noches de la reserva
 * @param total           monto total de la reserva
 * @param pagadoEnLinea   monto pagado en línea por el cliente al realizar la reserva
 * @param saldoHotel      saldo pendiente de pago que el cliente abonará en el hotel
 * @param habitaciones    lista de habitaciones asignadas a la reserva con su tipo y número
 * @param estado          estado actual de la reserva (confirmada, pendiente, etc.)
 */
public record ReservaConfirmadaData(
    String to,
    String subject,
    String recipientName,
    String codigoReserva,
    LocalDate checkIn,
    LocalDate checkOut,
    long noches,
    BigDecimal total,
    BigDecimal pagadoEnLinea,
    BigDecimal saldoHotel,
    List<HabitacionInfo> habitaciones,
    String estado
) implements DatosCorreo {

    /**
     * DTO interno que contiene la información de una habitación asignada a la reserva.
     *
     * @param tipo   tipo o categoría de la habitación (ej: "Suite", "Doble", "Sencilla")
     * @param numero número identificador de la habitación
     */
    public record HabitacionInfo(String tipo, String numero) {}

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
