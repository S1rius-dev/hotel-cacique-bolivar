package com.hotelcaciquebolivar.reservas.service.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.exception.payment.WebhookVerificationException;

@ExtendWith(MockitoExtension.class)
class WebhookProcessorImplTest {

    @Mock
    private PaymentGatewayFactory gatewayFactory;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WebhookProcessorImpl webhookProcessor;

    @Test
    void procesaYConfirmaCuandoFirmaEsValida() {
        PaymentGateway gateway = mock(PaymentGateway.class);
        PaymentSignatureService signature = mock(PaymentSignatureService.class);
        when(gatewayFactory.porTipo(PaymentGatewayType.WOMPI)).thenReturn(gateway);
        when(gateway.signatureService()).thenReturn(signature);
        when(signature.verificar("firma", "payload")).thenReturn(true);

        PaymentWebhookNotification notif = new PaymentWebhookNotification(
                PaymentGatewayType.WOMPI, "tx-123", null, null, "firma", null);
        when(gateway.normalizarNotificacion("payload", "firma", null)).thenReturn(notif);

        String resultado = webhookProcessor.procesar(PaymentGatewayType.WOMPI, "payload", "firma");

        assertEquals("ok", resultado);
        verify(paymentService).confirmarTransaccion(argThat(n ->
                n.referenciaExterna().equals("tx-123") && n.gateway() == PaymentGatewayType.WOMPI && "payload".equals(n.payloadCrudo())));
    }

    @Test
    void rechazaCuandoLaFirmaEsInvalida() {
        PaymentGateway gateway = mock(PaymentGateway.class);
        PaymentSignatureService signature = mock(PaymentSignatureService.class);
        when(gatewayFactory.porTipo(PaymentGatewayType.WOMPI)).thenReturn(gateway);
        when(gateway.signatureService()).thenReturn(signature);
        when(signature.verificar("mala", "payload")).thenReturn(false);

        assertThrows(WebhookVerificationException.class,
                () -> webhookProcessor.procesar(PaymentGatewayType.WOMPI, "payload", "mala"));

        verify(gateway, never()).normalizarNotificacion(any(), any(), any());
        verify(paymentService, never()).confirmarTransaccion(any());
    }
}
