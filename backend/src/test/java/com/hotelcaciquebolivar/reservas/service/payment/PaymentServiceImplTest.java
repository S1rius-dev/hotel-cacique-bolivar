package com.hotelcaciquebolivar.reservas.service.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutWidgetConfig;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentIntentRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentStatusResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.repository.pago.TransaccionPagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.payment.pago.PagoCompensacionService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentGatewayFactory gatewayFactory;
    @Mock
    private TransaccionPagoRepository transaccionRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private AnticipoCalculator anticipoCalculator;
    @Mock
    private PagoCompensacionService pagoCompensacionService;
    @Mock
    private PaymentReferenceGeneratorFactory referenceGeneratorFactory;
    @Mock
    private PaymentReferenceGenerator referenceGenerator;
    @Mock
    private PaymentProperties properties;
    @Mock
    private ReservationProperties reservationProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentGateway gateway;

    private Reserva reserva() {
        return Reserva.builder()
                .id(10L)
                .codigoReserva("RES-2026-0001")
                .total(new BigDecimal("600000.00"))
                .fechaCheckIn(java.time.LocalDate.of(2026, 8, 1))
                .fechaCheckOut(java.time.LocalDate.of(2026, 8, 3))
                .build();
    }

    @Test
    void iniciarCheckoutPersisteTransaccionYDevuelveWidget() {
        Reserva reserva = reserva();
        when(reservaRepository.findByIdWithLock(10L)).thenReturn(Optional.of(reserva));
        when(anticipoCalculator.calcularAnticipo(reserva)).thenReturn(new BigDecimal("300000.00"));
        when(transaccionRepository.findTopByReservaIdAndEstadoPasarelaOrderByCreatedAtDesc(
                10L, PaymentStatus.CREATED)).thenReturn(Optional.empty());
        when(referenceGeneratorFactory.para(PaymentGatewayType.WOMPI)).thenReturn(referenceGenerator);
        when(referenceGenerator.generar(10L, "RES-2026-0001")).thenReturn("REF-123");

        gateway = mock(PaymentGateway.class);
        when(gatewayFactory.activa()).thenReturn(gateway);
        when(properties.getGateway()).thenReturn(PaymentGatewayType.WOMPI);
        when(properties.getMoneda()).thenReturn("COP");
        when(gateway.prepararCheckout(any(PaymentIntentRequest.class)))
                .thenReturn(new CheckoutWidgetConfig(
                        "REF-123", new BigDecimal("300000.00"), 30000000L, "COP",
                        "pub-test", "firma-integridad", "https://localhost:5173/ok"));

        when(transaccionRepository.save(any(TransaccionPago.class)))
                .thenAnswer(inv -> {
                    TransaccionPago t = inv.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        CheckoutRequest request = new CheckoutRequest(10L, new BigDecimal("300000.00"),
                MetodoPago.TARJETA_CREDITO, null);
        CheckoutResponse response = paymentService.iniciarCheckout(request, "client@x.com");

        assertNotNull(response);
        assertEquals("REF-123", response.referenciaExterna());
        assertEquals(PaymentGatewayType.WOMPI, response.gateway());
        assertEquals(new BigDecimal("300000.00"), response.monto());
        assertEquals(30000000L, response.amountInCents());
        assertEquals("pub-test", response.publicKey());
        assertEquals("firma-integridad", response.signatureIntegrity());
        assertEquals("https://localhost:5173/ok", response.redirectUrl());
        verify(transaccionRepository).save(any(TransaccionPago.class));
    }

    @Test
    void rechazaCheckoutCuandoYaExisteUnoEnCurso() {
        Reserva reserva = reserva();
        when(reservaRepository.findByIdWithLock(10L)).thenReturn(Optional.of(reserva));
        when(anticipoCalculator.calcularAnticipo(reserva)).thenReturn(new BigDecimal("300000.00"));
        when(reservationProperties.getPaymentExpirationMinutes()).thenReturn(20);
        TransaccionPago existente = transaccion(10L, "tx-999");
        existente.setId(99L);
        existente.setIdempotenciaKey("idem-999");
        existente.setCreatedAt(java.time.LocalDateTime.now());
        when(transaccionRepository.findTopByReservaIdAndEstadoPasarelaOrderByCreatedAtDesc(
                10L, PaymentStatus.CREATED)).thenReturn(Optional.of(existente));

        gateway = mock(PaymentGateway.class);
        when(gatewayFactory.activa()).thenReturn(gateway);
        when(properties.getGateway()).thenReturn(PaymentGatewayType.WOMPI);
        when(properties.getMoneda()).thenReturn("COP");
        when(gateway.prepararCheckout(any(PaymentIntentRequest.class)))
                .thenReturn(new CheckoutWidgetConfig(
                        "tx-999", new BigDecimal("300000.00"), 30000000L, "COP",
                        "pub-test", "firma-reusada", "https://localhost:5173/ok"));

        CheckoutRequest request = new CheckoutRequest(10L, new BigDecimal("300000.00"),
                MetodoPago.TARJETA_CREDITO, null);

        CheckoutResponse response = paymentService.iniciarCheckout(request, "client@x.com");
        assertNotNull(response);
        assertEquals(99L, response.transaccionId());
        assertEquals("tx-999", response.referenciaExterna());
        assertEquals(30000000L, response.amountInCents());
        assertEquals("firma-reusada", response.signatureIntegrity());
        verify(gateway).prepararCheckout(any(PaymentIntentRequest.class));
        verify(transaccionRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void confirmarTransaccionAprobadaRegistraPago() {
        TransaccionPago transaccion = transaccion(10L, "tx-123");
        when(transaccionRepository.findByReferenciaExternaAndGateway(
                "tx-123", PaymentGatewayType.WOMPI)).thenReturn(Optional.of(transaccion));
        when(pagoCompensacionService.registrarPagoOnline(transaccion))
                .thenAnswer(inv -> {
                    transaccion.setPago(Pago.builder().id(5L).estado(EstadoPago.COMPLETADO).build());
                    transaccion.setReferenciaInterna("PAG-2026-ABC12345");
                    return new PagoResponse(5L, "PAG-2026-ABC12345", 10L, "RES-2026-0001",
                            new BigDecimal("300000.00"), null, MetodoPago.TARJETA_CREDITO,
                            EstadoPago.COMPLETADO, "tx-123", null, null);
                });
        when(transaccionRepository.save(any(TransaccionPago.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentWebhookNotification notif = new PaymentWebhookNotification(
                PaymentGatewayType.WOMPI, "tx-123", PaymentStatus.APPROVED, "key", "firma", null);

        PaymentStatusResponse response = paymentService.confirmarTransaccion(notif);

        assertNotNull(response);
        assertEquals(PaymentStatus.APPROVED, response.estadoPasarela());
        assertNotNull(transaccion.getPago());
        assertEquals("PAG-2026-ABC12345", response.codigoPago());
        verify(pagoCompensacionService).registrarPagoOnline(transaccion);
    }

    @Test
    void confirmarTransaccionNoEncontradaLanzaError() {
        when(transaccionRepository.findByReferenciaExternaAndGateway(
                "desconocida", PaymentGatewayType.WOMPI)).thenReturn(Optional.empty());

        PaymentWebhookNotification notif = new PaymentWebhookNotification(
                PaymentGatewayType.WOMPI, "desconocida", PaymentStatus.APPROVED, "key", "firma", null);

        // Tras fix Wompi Non hash: referencia desconocida retorna 200 ignored en lugar de lanzar PaymentGatewayException
        PaymentStatusResponse resp = paymentService.confirmarTransaccion(notif);
        assertNotNull(resp);
        assertEquals("desconocida", resp.referenciaExterna());
    }

    private TransaccionPago transaccion(Long reservaId, String referenciaExterna) {
        return TransaccionPago.builder()
                .reserva(reserva())
                .gateway(PaymentGatewayType.WOMPI)
                .estadoPasarela(PaymentStatus.CREATED)
                .referenciaExterna(referenciaExterna)
                .monto(new BigDecimal("300000.00"))
                .moneda("COP")
                .build();
    }
}
