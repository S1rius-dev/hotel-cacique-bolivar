package com.hotelcaciquebolivar.reservas.service.payment;

import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentStatusResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;

/**
 * Interfaz del servicio de gestión de pagos en línea.
 * Define las operaciones para iniciar checkout, confirmar transacciones
 * vía webhook, consultar estados y cancelar transacciones.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PaymentService {

    /**
     * Inicia el proceso de checkout para una reserva.
     * Calcula el anticipo, verifica transacciones existentes y genera la configuración del widget.
     *
     * @param request datos de la solicitud de checkout
     * @param email correo del usuario que inicia el checkout
     * @return respuesta con la configuración del widget de pago
     * @throws ResourceNotFoundException si la reserva no existe
     * @throws ValidationException si la reserva está cancelada/finalizada o el anticipo es inválido
     */
    CheckoutResponse iniciarCheckout(CheckoutRequest request, String email);

    /**
     * Confirma o actualiza el estado de una transacción a partir de una notificación webhook.
     * Para transacciones APPROVED sin pago asociado, registra el pago automáticamente.
     *
     * @param notificacion notificación normalizada del webhook
     * @return estado actualizado de la transacción
     */
    PaymentStatusResponse confirmarTransaccion(PaymentWebhookNotification notificacion);

    /**
     * Consulta el estado de pago de una reserva desde la transacción más reciente.
     *
     * @param reservaId identificador de la reserva
     * @return estado actual del pago de la reserva
     * @throws ResourceNotFoundException si la reserva no existe
     */
    PaymentStatusResponse consultarStatus(Long reservaId);

    /**
     * Verifica el estado de la transacción directamente en la pasarela y sincroniza el estado local.
     * Si la pasarela reporta APPROVED y no hay pago registrado, compensa automáticamente.
     * Mantiene nombre legacy {@code verificarEnWompi} por compatibilidad.
     *
     * @param reservaId identificador de la reserva
     * @return estado sincronizado de la transacción
     * @throws ResourceNotFoundException si la reserva no existe
     */
    PaymentStatusResponse verificarEnWompi(Long reservaId);

    /**
     * Cancela una transacción de pago que no haya sido compensada ni aprobada.
     *
     * @param transaccionId identificador de la transacción a cancelar
     * @throws ResourceNotFoundException si la transacción no existe
     * @throws ValidationException si la transacción ya fue compensada o aprobada
     */
    void cancelarTransaccion(Long transaccionId);
}
