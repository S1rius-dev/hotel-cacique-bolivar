package com.hotelcaciquebolivar.reservas.controller.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentStatusResponse;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentService;

import jakarta.validation.Valid;

/**
 * Controlador REST público para el proceso de checkout y pago.
 * Permite iniciar el proceso de pago, consultar el estado de un pago
 * y verificar el estado del pago con la pasarela configurada.
 * No requiere autenticación (endpoint público).
 */
@RestController
@RequestMapping("/api/public/checkout")
public class CheckoutController {

    private final PaymentService paymentService;

    /**
     * Constructor que inyecta el servicio de pagos.
     *
     * @param paymentService servicio de gestión de pagos
     */
    public CheckoutController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Inicia el proceso de checkout para una reserva.
     * Crea una transacción de pago con la pasarela configurada.
     *
     * @param request datos del checkout a procesar, validados con @Valid
     * @return respuesta con la información de la transacción iniciada
     */
    @PostMapping
    public ResponseEntity<CheckoutResponse> iniciarCheckout(
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(paymentService.iniciarCheckout(request, null));
    }

    /**
     * Consulta el estado actual de un pago asociado a una reserva.
     *
     * @param reservaId identificador de la reserva
     * @return estado actual del pago
     */
    @GetMapping("/status")
    public ResponseEntity<PaymentStatusResponse> consultarStatus(
            @RequestParam Long reservaId) {
        return ResponseEntity.ok(paymentService.consultarStatus(reservaId));
    }

    /**
     * Verifica el estado del pago directamente con la pasarela configurada.
     * Actualiza el estado local del pago según la respuesta del proveedor.
     *
     * @param reservaId identificador de la reserva
     * @return estado verificado del pago
     */
    @PostMapping("/{reservaId}/verificar")
    public ResponseEntity<PaymentStatusResponse> verificar(
            @PathVariable Long reservaId) {
        return ResponseEntity.ok(paymentService.verificarEnWompi(reservaId));
    }
}
