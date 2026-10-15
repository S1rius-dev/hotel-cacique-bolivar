package com.hotelcaciquebolivar.reservas.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.config.reservations.ReservationProperties;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.CheckoutWidgetConfig;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentIntentRequest;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentStatusResponse;
import com.hotelcaciquebolivar.reservas.dto.payment.PaymentWebhookNotification;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentStatus;
import com.hotelcaciquebolivar.reservas.entity.pago.TipoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.repository.pago.TransaccionPagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.payment.pago.PagoCompensacionService;

/**
 * Implementación del servicio de pagos en línea.
 * Gestiona el ciclo de vida de transacciones de pago: creación de checkout,
 * confirmación vía webhook, verificación en pasarela y cancelación.
 * Integra con la fábrica de pasarelas, el calculador de anticipos y el servicio de compensación.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentGatewayFactory gatewayFactory;
    private final TransaccionPagoRepository transaccionRepository;
    private final ReservaRepository reservaRepository;
    private final AnticipoCalculator anticipoCalculator;
    private final PagoCompensacionService pagoCompensacionService;
    private final PaymentReferenceGeneratorFactory referenceGeneratorFactory;
    private final PaymentProperties properties;
    private final ReservationProperties reservationProperties;

    public PaymentServiceImpl(
            PaymentGatewayFactory gatewayFactory,
            TransaccionPagoRepository transaccionRepository,
            ReservaRepository reservaRepository,
            AnticipoCalculator anticipoCalculator,
            PagoCompensacionService pagoCompensacionService,
            PaymentReferenceGeneratorFactory referenceGeneratorFactory,
            PaymentProperties properties,
            ReservationProperties reservationProperties) {
        this.gatewayFactory = gatewayFactory;
        this.transaccionRepository = transaccionRepository;
        this.reservaRepository = reservaRepository;
        this.anticipoCalculator = anticipoCalculator;
        this.pagoCompensacionService = pagoCompensacionService;
        this.referenceGeneratorFactory = referenceGeneratorFactory;
        this.properties = properties;
        this.reservationProperties = reservationProperties;
    }

    private long ttlMinutes() {
        return reservationProperties.getPaymentExpirationMinutes();
    }

    /**
     * {@inheritDoc}
     * Reutiliza transacciones CREATED no expiradas (TTL configurable).
     * Si la transacción existente expiró, la marca como EXPIRED y genera nueva referencia.
     * Registra la transacción en estado CREATED con la configuración del widget Wompi.
     */
    @Override
    @Transactional
    public CheckoutResponse iniciarCheckout(CheckoutRequest request, String email) {
        Reserva reserva = reservaRepository.findByIdWithLock(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", request.reservaId()));

        if (reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ValidationException("reserva",
                    "no se puede iniciar un checkout en reservas canceladas o finalizadas");
        }

        BigDecimal anticipo = anticipoCalculator.calcularAnticipo(reserva);
        if (anticipo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("monto",
                    "el anticipo calculado no es v\u00e1lido");
        }

        MetodoPago metodo = request.metodoPago() != null
                ? request.metodoPago()
                : MetodoPago.OTRO;

        TransaccionPago existente = transaccionRepository
                .findTopByReservaIdAndEstadoPasarelaOrderByCreatedAtDesc(
                        reserva.getId(), PaymentStatus.CREATED)
                .orElse(null);

        if (existente != null) {
            // TTL configurable: si el CREATED expiró, no reusar — crear nueva referencia
            boolean expirado = existente.getCreatedAt() != null
                    && existente.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(ttlMinutes()));
            if (!expirado) {
                PaymentIntentRequest intent = new PaymentIntentRequest(
                        properties.getGateway(),
                        existente.getMonto(),
                        properties.getMoneda(),
                        "Anticipo reserva " + reserva.getCodigoReserva(),
                        existente.getReferenciaExterna(),
                        existente.getIdempotenciaKey(),
                        null,
                        email);
                CheckoutWidgetConfig widget = gatewayFactory.activa().prepararCheckout(intent);
                log.info("[Checkout] REUSO reservaId={} referencia={} amountInCents={} signature={} publicKey={}",
                        reserva.getId(), widget.referenciaExterna(), widget.amountInCents(), widget.signatureIntegrity(), widget.publicKey());
                return new CheckoutResponse(
                        existente.getId(),
                        existente.getReferenciaExterna(),
                        existente.getGateway(),
                        existente.getMonto(),
                        widget.amountInCents(),
                        existente.getMoneda(),
                        existente.getEstadoPasarela(),
                        null,
                        null,
                        widget.publicKey(),
                        widget.signatureIntegrity(),
                        widget.redirectUrl(),
                        reserva.getFechaExpiracionPago(),
                        calcularSegundosRestantes(reserva));
            }
            log.info("Transacción CREATED {} expirada (> {} min), generando nueva referencia para reserva {}",
                    existente.getReferenciaExterna(), ttlMinutes(), reserva.getId());
            // marcar la anterior como expirada para no reusarla
            existente.setEstadoPasarela(PaymentStatus.EXPIRED);
            transaccionRepository.save(existente);
        }

        String idempotenciaKey = UUID.randomUUID().toString();
        String referencia = referenceGeneratorFactory.para(properties.getGateway()).generar(
                reserva.getId(), reserva.getCodigoReserva());

        PaymentIntentRequest intent = new PaymentIntentRequest(
                properties.getGateway(),
                anticipo,
                properties.getMoneda(),
                "Anticipo reserva " + reserva.getCodigoReserva(),
                referencia,
                idempotenciaKey,
                null,
                email);

        CheckoutWidgetConfig widget = gatewayFactory.activa().prepararCheckout(intent);

        // DEBUG: log genérico sin acoplar a Wompi
        log.info("[Checkout] gateway={} reservaId={} codigo={} referencia={} monto={} amountInCents={} moneda={} publicKey={} redirectUrl={} signature={}",
                properties.getGateway(), reserva.getId(), reserva.getCodigoReserva(), widget.referenciaExterna(), widget.monto(), widget.amountInCents(),
                widget.moneda(), widget.publicKey(), widget.redirectUrl(), widget.signatureIntegrity());

        TransaccionPago transaccion = TransaccionPago.builder()
                .reserva(reserva)
                .gateway(properties.getGateway())
                .estadoPasarela(PaymentStatus.CREATED)
                .referenciaExterna(widget.referenciaExterna())
                .monto(anticipo)
                .moneda(properties.getMoneda())
                .tipoPago(TipoPago.ANTICIPO)
                .metodoPago(metodo)
                .idempotenciaKey(idempotenciaKey)
                .build();

        TransaccionPago guardada = transaccionRepository.save(transaccion);

        return new CheckoutResponse(
                guardada.getId(),
                guardada.getReferenciaExterna(),
                guardada.getGateway(),
                guardada.getMonto(),
                widget.amountInCents(),
                guardada.getMoneda(),
                guardada.getEstadoPasarela(),
                null,
                null,
                widget.publicKey(),
                widget.signatureIntegrity(),
                widget.redirectUrl(),
                reserva.getFechaExpiracionPago(),
                calcularSegundosRestantes(reserva));
    }

    /**
     * {@inheritDoc}
     * Si la referencia es desconocida retorna 200 ignorado para evitar reintentos de la pasarela.
     * Actualiza el método de pago desde la fuente de la pasarela cuando está disponible.
     */
    @Override
    @Transactional
    public PaymentStatusResponse confirmarTransaccion(PaymentWebhookNotification notificacion) {
        var opcional = transaccionRepository
                .findByReferenciaExternaAndGateway(
                        notificacion.referenciaExterna(), notificacion.gateway());
        if (opcional.isEmpty()) {
            // FIX: Wompi "Non hash response body" cuando referencia no existe: retornar 200 ignored en lugar de 502
            // evita reintentos infinitos y permite diagnosticar sin romper webhook — NO crear TransaccionPago huérfana
            String payloadPreview = notificacion.payloadCrudo() != null
                    ? notificacion.payloadCrudo().substring(0, Math.min(1200, notificacion.payloadCrudo().length()))
                    : "null";
            log.warn("[Webhook] referencia desconocida {} gateway={} estado={} metodo={} — payload ignorado (retorna 200 ok para que la pasarela no reintente). "
                    + "Verifica que el checkout creó la TransaccionPago con esa referencia. Payload preview: {}",
                    notificacion.referenciaExterna(), notificacion.gateway(),
                    notificacion.estado(), notificacion.metodoPago(), payloadPreview);
            // Retornamos respuesta sintética para no romper el contrato — sin crear datos de negocio
            return new PaymentStatusResponse(
                    null, notificacion.referenciaExterna(), null, null,
                    java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                    java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                    notificacion.referenciaExterna(), null, null, 0L);
        }
        TransaccionPago transaccion = opcional.get();

        transaccion.setEstadoPasarela(notificacion.estado());
        transaccion.setPayloadNotificacion(notificacion.payloadCrudo());
        transaccion.setFirmaRecibida(notificacion.firma());
        // Fuente de verdad del método de pago: si la pasarela informa método real, sobrescribe preferencia del frontend
        if (notificacion.metodoPago() != null && notificacion.metodoPago() != transaccion.getMetodoPago()) {
            log.info("Actualizando metodoPago de transacción {} de {} a {} (fuente {} webhook)",
                    notificacion.referenciaExterna(), transaccion.getMetodoPago(), notificacion.metodoPago(), notificacion.gateway());
            transaccion.setMetodoPago(notificacion.metodoPago());
        }

        if (notificacion.estado() == PaymentStatus.APPROVED && transaccion.getPago() == null) {
            PagoResponse pago = pagoCompensacionService.registrarPagoOnline(transaccion);
            transaccion.setReferenciaInterna(pago.codigoPago());
            log.info("Webhook APPROVED: reserva {} confirmada, pago {}", transaccion.getReserva().getId(), pago.codigoPago());
        } else if (notificacion.estado() == PaymentStatus.PENDING) {
            log.info("Webhook PENDING para referencia {} — reserva {} queda PENDIENTE", notificacion.referenciaExterna(), transaccion.getReserva().getId());
        } else if (notificacion.estado() == PaymentStatus.DECLINED
                || notificacion.estado() == PaymentStatus.VOIDED
                || notificacion.estado() == PaymentStatus.EXPIRED
                || notificacion.estado() == PaymentStatus.ERROR
                || notificacion.estado() == PaymentStatus.REJECTED) {
            // No cancelar reserva aquí — permitir reintento hasta TTL configurable; el job expirador se encargará
            log.warn("Webhook {} para referencia {} — reserva {} queda PENDIENTE_PAGO hasta TTL {} min (permite reintento)",
                    notificacion.estado(), notificacion.referenciaExterna(), transaccion.getReserva().getId(), ttlMinutes());
        }

        transaccionRepository.save(transaccion);
        return toStatusResponse(transaccion);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse consultarStatus(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));
        return transaccionRepository
                .findByReservaIdOrderByCreatedAtDesc(reservaId)
                .stream()
                .findFirst()
                .map(this::toStatusResponse)
                .orElseGet(() -> new PaymentStatusResponse(
                        reserva.getId(),
                        reserva.getCodigoReserva(),
                        null,
                        null,
                        BigDecimal.ZERO,
                        reserva.getTotal() != null ? reserva.getTotal() : BigDecimal.ZERO,
                        reserva.getTotalPagado(),
                        reserva.getSaldoPendiente(),
                        null,
                        null,
                        reserva.getFechaExpiracionPago(),
                        calcularSegundosRestantes(reserva)));
    }

    /**
     * {@inheritDoc}
     * Consulta por referencia en la pasarela activa; si falla retorna estado local sin error.
     * Sincroniza método de pago real desde la pasarela.
     */
    @Override
    @Transactional
    public PaymentStatusResponse verificarEnWompi(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        TransaccionPago transaccion = transaccionRepository
                .findByReservaIdOrderByCreatedAtDesc(reservaId)
                .stream().findFirst().orElse(null);

        if (transaccion == null) {
            return new PaymentStatusResponse(
                    reserva.getId(), reserva.getCodigoReserva(), null, null,
                    BigDecimal.ZERO,
                    reserva.getTotal() != null ? reserva.getTotal() : BigDecimal.ZERO,
                    reserva.getTotalPagado(), reserva.getSaldoPendiente(), null, null,
                    reserva.getFechaExpiracionPago(), calcularSegundosRestantes(reserva));
        }

        // Si ya está APPROVED/CREATED con pago, devolver directo
        if (transaccion.getEstadoPasarela() == PaymentStatus.APPROVED
                || transaccion.getPago() != null) {
            return toStatusResponse(transaccion);
        }

        try {
            PaymentGateway gateway = gatewayFactory.porTipo(transaccion.getGateway());
            // Intentar consultar por referencia; si falla por ID vs referencia, loggear sin romper
            PaymentStatus estadoRemoto = null;
            try {
                estadoRemoto = gateway.consultarTransaccion(transaccion.getReferenciaExterna());
            } catch (Exception ex) {
                log.warn("No se pudo consultar pasarela {} por referencia {} (reserva {}): {} — devolviendo estado local {}",
                        transaccion.getGateway(), transaccion.getReferenciaExterna(), reservaId, ex.getMessage(), transaccion.getEstadoPasarela());
                return toStatusResponse(transaccion);
            }

            // Sincronizar método de pago real desde la pasarela si está disponible (fuente de verdad)
            try {
                MetodoPago metodoRemoto = gateway.consultarMetodoPago(transaccion.getReferenciaExterna());
                if (metodoRemoto != null && metodoRemoto != transaccion.getMetodoPago()) {
                    log.info("Verificación {} actualiza metodoPago {} -> {} para referencia {}",
                            transaccion.getGateway(), transaccion.getMetodoPago(), metodoRemoto, transaccion.getReferenciaExterna());
                    transaccion.setMetodoPago(metodoRemoto);
                }
            } catch (Exception ex) {
                log.debug("No se pudo consultar metodoPago para {}: {}", transaccion.getReferenciaExterna(), ex.getMessage());
            }

            if (estadoRemoto != null && estadoRemoto != transaccion.getEstadoPasarela()) {
                log.info("Verificación {}: referencia {} cambió {} -> {} (reserva {})",
                        transaccion.getGateway(), transaccion.getReferenciaExterna(), transaccion.getEstadoPasarela(), estadoRemoto, reservaId);
                transaccion.setEstadoPasarela(estadoRemoto);
                if (estadoRemoto == PaymentStatus.APPROVED && transaccion.getPago() == null) {
                    PagoResponse pago = pagoCompensacionService.registrarPagoOnline(transaccion);
                    transaccion.setReferenciaInterna(pago.codigoPago());
                    log.info("Verificación promovió a APPROVED: reserva {} confirmada", reservaId);
                }
                transaccionRepository.save(transaccion);
            } else if (transaccion.getMetodoPago() != null) {
                // Guardar cambio de método aunque estado no haya cambiado
                transaccionRepository.save(transaccion);
            }
        } catch (Exception ex) {
            log.warn("Error en verificarEnWompi para reserva {}: {}", reservaId, ex.getMessage());
        }

        return toStatusResponse(transaccion);
    }

    /**
     * {@inheritDoc}
     * Marca la transacción como VOIDED. No permite cancelar transacciones compensadas o aprobadas.
     */
    @Override
    @Transactional
    public void cancelarTransaccion(Long transaccionId) {
        TransaccionPago transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new ResourceNotFoundException("TransaccionPago", transaccionId));
        if (transaccion.getPago() != null) {
            throw new ValidationException("estado",
                    "no se puede cancelar una transacci\u00f3n ya compensada como pago");
        }
        if (transaccion.getEstadoPasarela() == PaymentStatus.APPROVED) {
            throw new ValidationException("estado",
                    "no se puede cancelar una transacci\u00f3n aprobada");
        }
        transaccion.setEstadoPasarela(PaymentStatus.VOIDED);
        transaccionRepository.save(transaccion);
    }

    private PaymentStatusResponse toStatusResponse(TransaccionPago transaccion) {
        Reserva reserva = transaccion.getReserva();
        BigDecimal monto = transaccion.getMonto() != null ? transaccion.getMonto() : BigDecimal.ZERO;
        return new PaymentStatusResponse(
                reserva.getId(),
                reserva.getCodigoReserva(),
                transaccion.getEstadoPasarela(),
                transaccion.getPago() != null ? transaccion.getPago().getEstado() : null,
                monto,
                reserva.getTotal() != null ? reserva.getTotal() : BigDecimal.ZERO,
                reserva.getTotalPagado(),
                reserva.getSaldoPendiente(),
                transaccion.getReferenciaExterna(),
                transaccion.getReferenciaInterna(),
                reserva.getFechaExpiracionPago(),
                calcularSegundosRestantes(reserva));
    }

    private Long calcularSegundosRestantes(Reserva reserva) {
        if (reserva.getFechaExpiracionPago() == null) return null;
        long secs = java.time.Duration.between(LocalDateTime.now(), reserva.getFechaExpiracionPago()).getSeconds();
        return Math.max(0, secs);
    }
}
