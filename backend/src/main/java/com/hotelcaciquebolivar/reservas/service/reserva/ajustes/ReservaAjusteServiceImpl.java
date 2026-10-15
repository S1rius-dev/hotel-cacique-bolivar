package com.hotelcaciquebolivar.reservas.service.reserva.ajustes;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.reserva.AjusteReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.TipoAjuste;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.AjusteReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.reserva.AjusteReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.AjustePayload;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de ajustes de reserva.
 * Gestiona la adición, edición y eliminación de ajustes (cargos extra o descuentos)
 * asociados a una reserva. Valida que la reserva esté en un estado editable,
 * recalcula el total de la reserva después de cada cambio y registra eventos
 * de auditoría y notificaciones SSE.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReservaAjusteServiceImpl implements ReservaAjusteService {

    private final ReservaRepository reservaRepository;
    private final AjusteReservaRepository ajusteReservaRepository;
    private final AjusteReservaMapper ajusteReservaMapper;
    private final ReservaPricingService reservaPricingService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     * Valida que la reserva esté en estado editable, que no exista otro ajuste de cortesía
     * activo si el tipo es CORTESIA, y que los datos del ajuste sean válidos.
     * Después de guardar, recalcula el total de la reserva y publica eventos de auditoría y SSE.
     */
    @Override
    public AjusteResponse agregarAjuste(Long reservaId, AjusteRequest dto) {
        Reserva reserva = obtenerReserva(reservaId);
        validarReservaEditable(reserva);
        validarAjuste(dto);

        if (dto.tipoAjuste() == TipoAjuste.CORTESIA) {
            boolean yaTieneCortesia = reserva.getAjustes().stream()
                    .anyMatch(a -> a.getActivo() && a.getTipoAjuste() == TipoAjuste.CORTESIA);
            if (yaTieneCortesia) {
                throw new ValidationException("tipoAjuste",
                        "ya existe un ajuste de cortesía activo en esta reserva");
            }
        }

        AjusteReserva ajuste = ajusteReservaMapper.toEntity(dto);
        ajuste.setReserva(reserva);

        ajuste = ajusteReservaRepository.save(ajuste);
        recalcularTotal(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", reservaId,
                String.format("Ajuste agregado — %s\nTipo: %s / %s — $%s",
                        reserva.getCodigoReserva(), ajuste.getTipoAjuste(),
                        ajuste.getModoCalculo(),
                        ajuste.getValor() != null ? ajuste.getValor() : "CORTESIA"));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.AJUSTE_AGREGADO,
                new AjustePayload(
                        ajuste.getId(),
                        reservaId,
                        ajuste.getTipoAjuste().name(),
                        ajuste.getConcepto(),
                        ajuste.getValor())));

        return ajusteReservaMapper.toResponse(ajuste);
    }

    /**
     * {@inheritDoc}
     * Valida que la reserva esté en estado editable y que el ajuste pertenezca a la reserva
     * indicada. Actualiza los datos del ajuste, recalcula el total y registra auditoría.
     */
    @Override
    public AjusteResponse editarAjuste(Long reservaId, Long ajusteId, AjusteRequest dto) {
        Reserva reserva = obtenerReserva(reservaId);
        validarReservaEditable(reserva);

        AjusteReserva ajuste = ajusteReservaRepository.findById(ajusteId)
                .orElseThrow(() -> new ResourceNotFoundException("AjusteReserva", ajusteId));

        if (!ajuste.getReserva().getId().equals(reservaId)) {
            throw new ValidationException("ajuste", "el ajuste no pertenece a la reserva indicada");
        }

        ajusteReservaMapper.updateFromDto(dto, ajuste);

        ajuste = ajusteReservaRepository.save(ajuste);
        recalcularTotal(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", reservaId,
                String.format("Ajuste editado — %s\nID ajuste: %d — %s",
                        reserva.getCodigoReserva(), ajuste.getId(), ajuste.getConcepto()));

        return ajusteReservaMapper.toResponse(ajuste);
    }

    /**
     * {@inheritDoc}
     * Realiza una eliminación lógica (soft delete) del ajuste, marcándolo como inactivo.
     * Valida que la reserva esté en estado editable y que el ajuste pertenezca a la reserva.
     * Recalcula el total de la reserva y registra auditoría.
     */
    @Override
    public void eliminarAjuste(Long reservaId, Long ajusteId) {
        Reserva reserva = obtenerReserva(reservaId);
        validarReservaEditable(reserva);

        AjusteReserva ajuste = ajusteReservaRepository.findById(ajusteId)
                .orElseThrow(() -> new ResourceNotFoundException("AjusteReserva", ajusteId));

        if (!ajuste.getReserva().getId().equals(reservaId)) {
            throw new ValidationException("ajuste", "el ajuste no pertenece a la reserva indicada");
        }

        ajuste.desactivar();
        ajusteReservaRepository.save(ajuste);
        recalcularTotal(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", reservaId,
                String.format("Ajuste eliminado (soft) — %s\nID ajuste: %d — %s",
                        reserva.getCodigoReserva(), ajuste.getId(), ajuste.getConcepto()));
    }

    /**
     * Valida que una reserva esté en un estado que permita modificaciones.
     * No se permiten ajustes en reservas FINALIZADA, CANCELADA o NO_SHOW.
     *
     * @param reserva la reserva a validar
     * @throws ValidationException si la reserva no está en un estado editable
     */
    private void validarReservaEditable(Reserva reserva) {
        EstadoReserva est = reserva.getEstado();
        if (est == EstadoReserva.FINALIZADA
                || est == EstadoReserva.CANCELADA
                || est == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado",
                    "no se pueden modificar ajustes en una reserva " + est);
        }
    }

    /**
     * Valida los datos de un ajuste según su tipo y modo de cálculo.
     * Para modo TOTAL, el valor es requerido y no puede ser negativo.
     * Para tipo CARGO o DESCUENTO, el valor es requerido y no puede ser negativo.
     *
     * @param dto datos del ajuste a validar
     * @throws ValidationException si los datos del ajuste no son válidos
     */
    private void validarAjuste(AjusteRequest dto) {
        if (dto.modoCalculo() == ModoCalculo.TOTAL) {
            if (dto.valor() == null) {
                throw new ValidationException("valor", "es requerido cuando modoCalculo es TOTAL");
            }
            if (dto.valor().signum() < 0) {
                throw new ValidationException("valor", "no puede ser negativo para modo TOTAL");
            }
            return;
        }
        if (dto.tipoAjuste() == TipoAjuste.CARGO || dto.tipoAjuste() == TipoAjuste.DESCUENTO) {
            if (dto.valor() == null) {
                throw new ValidationException("valor",
                        "es requerido para ajustes de tipo " + dto.tipoAjuste());
            }
            if (dto.valor().signum() < 0) {
                throw new ValidationException("valor",
                        "no puede ser negativo para ajustes de tipo " + dto.tipoAjuste());
            }
        }
    }

    /**
     * Recalcula el total de una reserva después de modificar sus ajustes.
     * Utiliza el servicio de precios para calcular el desglose y actualiza el total.
     * Valida que el total final no sea negativo.
     *
     * @param reserva la reserva cuyo total se va a recalcular
     * @throws ValidationException si el total final resulta negativo
     */
    private void recalcularTotal(Reserva reserva) {
        // Ajustes no modifican precios congelados, solo desglose
        var desglose = reservaPricingService.calcularDesglose(reserva);
        if (desglose.totalFinal() != null && desglose.totalFinal().signum() < 0) {
            throw new ValidationException("total", "el total final no puede ser negativo");
        }
        // Si hay un ajuste TOTAL, último valor gana automáticamente en calcularDesglose
        reserva.setTotal(desglose.totalFinal());
        reservaRepository.save(reserva);
    }

    /**
     * Obtiene una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada
     * @throws ResourceNotFoundException si no se encuentra la reserva
     */
    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }
}
