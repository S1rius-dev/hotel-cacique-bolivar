package com.hotelcaciquebolivar.reservas.service.reserva.detalles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.ActualizarDetalleRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.reserva.crud.ReservaCrudServiceImpl;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.DetallePayload;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservaDetalleServiceImpl implements ReservaDetalleService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaMapper reservaMapper;
    private final AuditService auditService;
    private final com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService precioTemporalService;
    private final com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService reservaPricingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ReservaResponse agregarHuespedes(Long reservaId, Long detalleId, List<Long> huespedesIds) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReserva.FINALIZADA
                || reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado",
                    "no se pueden agregar huéspedes a una reserva " + reserva.getEstado());
        }

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        for (Long clienteId : huespedesIds) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
            detalle.agregarHuesped(cliente);
        }

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Huéspedes agregados — %s\nHabitación: %s\nIDs: %s",
                        guardada.getCodigoReserva(), detalle.getHabitacion().getNumero(),
                        huespedesIds));

        return reservaMapper.toResponse(guardada);
    }

    @Override
    public ReservaResponse removerHuesped(Long reservaId, Long detalleId, Long detalleHuespedId) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReserva.FINALIZADA
                || reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado",
                    "no se pueden remover huéspedes de una reserva " + reserva.getEstado());
        }

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        DetalleReservaHuesped huesped = detalle.getHuespedes().stream()
                .filter(h -> h.getId().equals(detalleHuespedId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReservaHuesped", detalleHuespedId));

        detalle.removerHuesped(huesped);

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Huésped removido — %s\nHabitación: %s\nCliente ID: %d",
                        guardada.getCodigoReserva(), detalle.getHabitacion().getNumero(),
                        huesped.getCliente().getId()));

        return reservaMapper.toResponse(guardada);
    }

    @Override
    public ReservaResponse removerDetalle(Long reservaId, Long detalleId) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ValidationException("estado",
                    "solo se puede remover una habitación de reservas pendientes o confirmadas");
        }

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        if (reserva.getDetalles().size() <= 1) {
            throw new ValidationException("detalles",
                    "la reserva debe tener al menos una habitación");
        }

        detalle.cancelar();

        Habitacion habitacion = detalle.getHabitacion();
        if (habitacion.getEstado() == EstadoHabitacion.RESERVADA) {
            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        }

        reserva.setTotal(reservaPricingService.calcularTotalReserva(reserva));

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Habitación removida — %s\nHabitación: %s (cancelada)\nNuevo total: $%s",
                        guardada.getCodigoReserva(), habitacion.getNumero(), guardada.getTotal()));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_DETALLE_ELIMINADO,
                new DetallePayload(
                        guardada.getId(),
                        detalleId,
                        habitacion.getNumero(),
                        "CANCELADA")));

        return reservaMapper.toResponse(guardada);
    }

    @Override
    public ReservaResponse agregarDetalle(Long reservaId, DetalleReservaRequest dto) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ValidationException("estado",
                    "solo se puede agregar habitaciones a reservas pendientes o confirmadas");
        }

        BigDecimal precio = dto.precioPorNoche();
        if (precio == null) {
            Habitacion habitacion = habitacionRepository.findById(dto.habitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Habitacion", dto.habitacionId()));
            precio = habitacion.getTipo().getPrecioBase();
        }

        DetalleReservaRequest nuevo = new DetalleReservaRequest(
                dto.habitacionId(), precio, dto.cantidadOcupantes(), dto.observaciones(),
                dto.precioIncluyeIva());

        List<DetalleReserva> nuevos = construirDetalle(
                List.of(nuevo), reserva.getFechaCheckIn(), reserva.getFechaCheckOut());

        DetalleReserva detalle = nuevos.get(0);
        detalle.setReserva(reserva);
        reservaPricingService.congelarPreciosDetalle(detalle, reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
        reserva.agregarDetalle(detalle);
        reserva.setTotal(reservaPricingService.calcularTotalReserva(reserva));

        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Habitación agregada — %s\nHabitación: %s\nPrecio/noche: $%s",
                        guardada.getCodigoReserva(), detalle.getHabitacion().getNumero(),
                        precio));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.RESERVA_DETALLE_AGREGADO,
                new DetallePayload(
                        guardada.getId(),
                        detalle.getId(),
                        detalle.getHabitacion().getNumero(),
                        detalle.getEstado().name())));

        return reservaMapper.toResponse(guardada);
    }

    @Override
    public ReservaResponse actualizarDetalle(Long reservaId, Long detalleId, ActualizarDetalleRequest dto) {
        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado() == EstadoReserva.FINALIZADA
                || reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.NO_SHOW) {
            throw new ValidationException("estado",
                    "no se puede modificar una habitación de una reserva " + reserva.getEstado());
        }

        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DetalleReserva", detalleId));

        if (detalle.getEstado() == EstadoDetalleReserva.CANCELADA
                || detalle.getEstado() == EstadoDetalleReserva.LIBERADA) {
            throw new ValidationException("estado",
                    "no se puede modificar un detalle en estado " + detalle.getEstado());
        }

        if (dto.cantidadOcupantes() != null) {
            if (detalle.getHuespedes() != null
                    && dto.cantidadOcupantes() < detalle.getHuespedes().size()) {
                throw new ValidationException("cantidadOcupantes",
                        String.format("no puede ser menor a los huéspedes ya registrados (%d)",
                                detalle.getHuespedes().size()));
            }
            ReservaCrudServiceImpl.validarCapacidad(dto.cantidadOcupantes(), detalle.getHabitacion());
            detalle.setCantidadOcupantes(dto.cantidadOcupantes());
            detalle.validarCapacidad();
        }

        if (dto.observaciones() != null) {
            detalle.setObservaciones(dto.observaciones());
        }

        reserva.setTotal(reservaPricingService.calcularTotalReserva(reserva));
        Reserva guardada = reservaRepository.save(reserva);

        auditService.log(AuditAction.UPDATE, "Reserva", guardada.getId(),
                String.format("Habitación actualizada — %s\nHabitación: %s",
                        guardada.getCodigoReserva(), detalle.getHabitacion().getNumero()));

        return reservaMapper.toResponse(guardada);
    }

    // ====== PRIVADOS ======
    private Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }

    private List<DetalleReserva> construirDetalle(
            List<DetalleReservaRequest> detallesDto,
            LocalDate checkIn,
            LocalDate checkOut) {

        LocalDate queryCheckOut = checkIn.equals(checkOut) ? checkOut.plusDays(1) : checkOut;
        return detallesDto.stream().map(d -> {
            com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion habitacion
                    = habitacionRepository.findDisponiblePorIdConLock(
                            d.habitacionId(), checkIn, queryCheckOut)
                            .orElseThrow(() -> new com.hotelcaciquebolivar.reservas.exception.reserva.RoomNotAvailableException(
                            habitacionRepository.findById(d.habitacionId())
                                    .map(com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion::getNumero)
                                    .orElse(String.valueOf(d.habitacionId())),
                            checkIn, checkOut));

            ReservaCrudServiceImpl.validarPrecio(d.precioPorNoche(), habitacion);
            ReservaCrudServiceImpl.validarCapacidad(d.cantidadOcupantes(), habitacion);

            DetalleReserva detalle = DetalleReserva.builder()
                    .habitacion(habitacion)
                    .precioPorNoche(d.precioPorNoche())
                    .cantidadOcupantes(d.cantidadOcupantes())
                    .observaciones(d.observaciones())
                    .precioUsadoIncluyeIva(d.precioIncluyeIva())
                    .build();

            detalle.validarCapacidad();
            return detalle;

        }).toList();
    }
}
