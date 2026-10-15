package com.hotelcaciquebolivar.reservas.service.reserva.bloqueo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.reserva.BloqueoReserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.BloqueoReservaMapper;
import com.hotelcaciquebolivar.reservas.repository.reserva.BloqueoReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de bloqueo de reservas.
 * Gestiona la creación, edición, eliminación y consulta de bloqueos de reserva
 * que impiden la creación de reservas en rangos de fechas específicos o de forma total.
 * Valida rangos de fechas, registra auditoría y proporciona funcionalidad para
 * habilitar/deshabilitar las reservas web.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BloqueoReservaServiceImpl implements BloqueoReservaService {

    private final BloqueoReservaRepository repository;
    private final BloqueoReservaMapper mapper;
    private final AuditService auditService;

    /**
     * {@inheritDoc}
     * Valida el rango de fechas antes de guardar. Registra evento de auditoría.
     */
    @Override
    public BloqueoReservaResponse crear(BloqueoReservaRequest request) {
        BloqueoReserva bloqueo = mapper.toEntity(request);
        validarRango(bloqueo);
        bloqueo = repository.save(bloqueo);
        auditService.log(AuditAction.CREATE, "BloqueoReserva", bloqueo.getId(),
                construirDetalle(bloqueo, "Bloqueo de reservas web creado"));
        return mapper.toResponse(bloqueo);
    }

    /**
     * {@inheritDoc}
     * Retorna los bloqueos ordenados por fecha de inicio (ascendente) y luego por ID (descendente).
     */
    @Override
    @Transactional(readOnly = true)
    public List<BloqueoReservaResponse> listarTodos() {
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing(BloqueoReserva::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(BloqueoReserva::getId, Comparator.reverseOrder()))
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     * Lanza ResourceNotFoundException si no se encuentra el bloqueo.
     */
    @Override
    @Transactional(readOnly = true)
    public BloqueoReservaResponse buscarPorId(Long id) {
        BloqueoReserva bloqueo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloqueoReserva", id));
        return mapper.toResponse(bloqueo);
    }

    /**
     * {@inheritDoc}
     * Valida el rango de fechas después de la actualización. Registra evento de auditoría.
     */
    @Override
    public BloqueoReservaResponse actualizar(Long id, BloqueoReservaRequest request) {
        BloqueoReserva bloqueo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloqueoReserva", id));
        mapper.updateFromDto(request, bloqueo);
        validarRango(bloqueo);
        bloqueo = repository.save(bloqueo);
        auditService.log(AuditAction.UPDATE, "BloqueoReserva", bloqueo.getId(),
                construirDetalle(bloqueo, "Bloqueo de reservas web actualizado"));
        return mapper.toResponse(bloqueo);
    }

    /**
     * {@inheritDoc}
     * Realiza eliminación lógica del bloqueo. Registra evento de auditoría.
     */
    @Override
    public void desactivar(Long id) {
        BloqueoReserva bloqueo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloqueoReserva", id));
        bloqueo.desactivar();
        repository.save(bloqueo);
        auditService.log(AuditAction.CANCELLED, "BloqueoReserva", id,
                construirDetalle(bloqueo, "Bloqueo de reservas web desactivado"));
    }

    /**
     * {@inheritDoc}
     * Primero verifica si existe un bloqueo total activo. Si no existe, busca bloqueos
     * que intersecten con la fecha especificada.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeBloqueoParaFecha(LocalDate fecha) {
        List<BloqueoReserva> bloqueoTotal = repository.findBloqueoTotalActivo();
        if (!bloqueoTotal.isEmpty()) {
            return true;
        }
        List<BloqueoReserva> intersectantes = repository.findActivosQueIntersectan(fecha, fecha);
        return !intersectantes.isEmpty();
    }

    /**
     * {@inheritDoc}
     * Primero verifica si existe un bloqueo total activo. Si no existe, busca bloqueos
     * que intersecten con el rango de fechas especificado.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeBloqueoParaRango(LocalDate desde, LocalDate hasta) {
        List<BloqueoReserva> bloqueoTotal = repository.findBloqueoTotalActivo();
        if (!bloqueoTotal.isEmpty()) {
            return true;
        }
List<BloqueoReserva> intersectantes = repository.findActivosQueIntersectan(desde, hasta);
        return !intersectantes.isEmpty();
    }

    /**
     * {@inheritDoc}
     * Primero verifica si existe un bloqueo total activo. Si existe, retorna el primer
     * bloqueo total. Si no, busca bloqueos que intersecten con el rango de fechas.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BloqueoReserva> encontrarBloqueoParaRango(LocalDate desde, LocalDate hasta) {
        List<BloqueoReserva> bloqueoTotal = repository.findBloqueoTotalActivo();
        if (!bloqueoTotal.isEmpty()) {
            return Optional.of(bloqueoTotal.get(0));
        }
        return repository.findActivosQueIntersectan(desde, hasta).stream().findFirst();
    }

    /**
     * {@inheritDoc}
     * Verifica si existe al menos un bloqueo total activo en el sistema.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeBloqueoTotalActivo() {
        return !repository.findBloqueoTotalActivo().isEmpty();
    }

    /**
     * {@inheritDoc}
     * Las reservas web están habilitadas cuando NO existe un bloqueo total activo.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean obtenerEstadoReservasWeb() {
        return !existeBloqueoTotalActivo();
    }

    /**
     * {@inheritDoc}
     * Si se deshabilitan las reservas y ya existe un bloqueo total activo, retorna ese bloqueo.
     * Si no existe, crea uno nuevo con fechas nulas y motivo predeterminado.
     * Si se habilitan, busca el bloqueo total activo y lo desactiva.
     *
     * @param enabled true para habilitar, false para deshabilitar
     * @return el bloqueo modificado, o null si se habilitan y no había bloqueo activo
     */
    @Override
    public BloqueoReservaResponse toggleReservasWeb(boolean enabled) {
        List<BloqueoReserva> bloquesTotales = repository.findBloqueoTotalActivo();

        if (!enabled) {
            if (!bloquesTotales.isEmpty()) {
                return mapper.toResponse(bloquesTotales.get(0));
            }
            BloqueoReserva bloqueo = BloqueoReserva.builder()
                    .fechaInicio(null)
                    .fechaFin(null)
                    .motivo("Reservas web deshabilitadas")
                    .activo(true)
                    .build();
            bloqueo = repository.save(bloqueo);
            auditService.log(AuditAction.CREATE, "BloqueoReserva", bloqueo.getId(),
                    "Bloqueo total de reservas web creado");
            return mapper.toResponse(bloqueo);
        }

        if (bloquesTotales.isEmpty()) {
            return null;
        }

        BloqueoReserva bloqueo = bloquesTotales.get(0);
        bloqueo.desactivar();
        bloqueo = repository.save(bloqueo);
        auditService.log(AuditAction.CANCELLED, "BloqueoReserva", bloqueo.getId(),
                "Bloqueo total de reservas web desactivado");
        return mapper.toResponse(bloqueo);
    }

    /**
     * Construye el detalle para el registro de auditoría de un bloqueo.
     * Incluye el alcance (total o rango de fechas) y el motivo si está presente.
     *
     * @param bloqueo el bloqueo para el que se construye el detalle
     * @param accion  la acción realizada sobre el bloqueo
     * @return el texto descriptivo del detalle para auditoría
     */
    private String construirDetalle(BloqueoReserva bloqueo, String accion) {
        StringBuilder detalle = new StringBuilder(accion);
        if (bloqueo.esBloqueoTotal()) {
            detalle.append("\nAlcance: en general (sin rango)");
        } else {
            detalle.append("\nFechas: ").append(bloqueo.getFechaInicio())
                    .append(" → ").append(bloqueo.getFechaFin());
        }
        if (bloqueo.getMotivo() != null && !bloqueo.getMotivo().isBlank()) {
            detalle.append("\nMotivo: ").append(bloqueo.getMotivo());
        }
        return detalle.toString();
    }

    /**
     * Valida las fechas de un bloqueo de reserva.
     * Verifica que la fecha de fin no sea anterior a la fecha de inicio,
     * y que no se active un bloqueo cuyas fechas ya han finalizado.
     *
     * @param bloqueo el bloqueo a validar
     * @throws ValidationException si las fechas no son válidas
     */
    private void validarRango(BloqueoReserva bloqueo) {
        if (bloqueo.getFechaInicio() != null && bloqueo.getFechaFin() != null) {
            if (bloqueo.getFechaFin().isBefore(bloqueo.getFechaInicio())) {
                throw new ValidationException(
                        "fechaFin",
                        "debe ser posterior o igual a la fecha de inicio");
            }
            if (Boolean.TRUE.equals(bloqueo.getActivo())
                    && bloqueo.getFechaFin().isBefore(LocalDate.now())) {
                throw new ValidationException(
                        "activo",
                        "No se puede activar un bloqueo cuyas fechas ya han finalizado");
            }
        }
    }
}