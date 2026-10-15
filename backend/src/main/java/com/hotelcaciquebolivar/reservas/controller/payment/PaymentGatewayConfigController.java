package com.hotelcaciquebolivar.reservas.controller.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.payment.PaymentGatewayMetadata;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentGatewayFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador público que expone la metadata de la pasarela de pago activa.
 * Permite al frontend obtener de forma dinámica el nombre, logo y textos
 * de la pasarela configurada sin hardcodear valores de Wompi/Bold.
 */
@RestController
@RequestMapping("/api/public/payments")
@Tag(name = "Pasarela de pago (pública)", description = "Metadata de la pasarela configurada")
public class PaymentGatewayConfigController {

    private final PaymentGatewayFactory gatewayFactory;

    public PaymentGatewayConfigController(PaymentGatewayFactory gatewayFactory) {
        this.gatewayFactory = gatewayFactory;
    }

    @Operation(
            summary = "Obtener metadata de la pasarela activa",
            description = "Retorna información pública de la pasarela configurada (nombre, logo, textos) sin exponer secretos.")
    @GetMapping("/gateway")
    public ResponseEntity<PaymentGatewayMetadata> getGatewayMetadata() {
        PaymentGatewayMetadata metadata = gatewayFactory.activa().getMetadata();
        return ResponseEntity.ok(metadata);
    }
}
