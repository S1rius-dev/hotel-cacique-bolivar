package com.hotelcaciquebolivar.reservas.service.registro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.registro.HuespedRegistroDTO;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.registro.HuespedRegistroRepository;
import com.hotelcaciquebolivar.reservas.repository.registro.HuespedRegistroSpecification;

/**
 * Implementación del servicio de consulta de registros de huéspedes.
 * Construye especificaciones dinámicas (Specification pattern) para filtrar
 * los registros de alojamiento por fechas, habitación, tipo, búsqueda y estado.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional(readOnly = true)
public class HuespedRegistroServiceImpl implements HuespedRegistroService {

    private final HuespedRegistroRepository repository;
    private final PageMapper pageMapper;

    public HuespedRegistroServiceImpl(HuespedRegistroRepository repository, PageMapper pageMapper) {
        this.repository = repository;
        this.pageMapper = pageMapper;
    }

    /** {@inheritDoc} */
    @Override
    public PageResponse<HuespedRegistroDTO> consultarRegistros(
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado, Pageable pageable) {

        Specification<DetalleReservaHuesped> spec = buildSpecification(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        Page<DetalleReservaHuesped> page = repository.findAll(spec, pageable);
        return pageMapper.toPageResponse(page, this::toDTO);
    }

    /** {@inheritDoc} */
    @Override
    public List<HuespedRegistroDTO> consultarTodosParaExport(
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) {

        Specification<DetalleReservaHuesped> spec = buildSpecification(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        return repository.findAll(spec).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Construye una especificación dinámicamente concatenando filtros opcionales.
     * Cada filtro se agrega solo si su parámetro correspondiente no es null ni vacío.
     *
     * @param fechaDesde      fecha de inicio del filtro de check-in
     * @param fechaHasta      fecha de fin del filtro de check-in
     * @param habitacion      número de habitación
     * @param tipoHabitacionId identificador del tipo de habitación
     * @param busqueda        texto de búsqueda
     * @param estado          estado del detalle
     * @return especificación compuesta con todos los filtros aplicables
     */
    private Specification<DetalleReservaHuesped> buildSpecification(
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) {

        Specification<DetalleReservaHuesped> spec = (root, query, cb) -> cb.conjunction();

        if (fechaDesde != null || fechaHasta != null) {
            spec = spec.and(HuespedRegistroSpecification.fechaCheckInEntre(fechaDesde, fechaHasta));
        }
        if (habitacion != null && !habitacion.isBlank()) {
            spec = spec.and(HuespedRegistroSpecification.numeroHabitacionIgual(habitacion));
        }
        if (tipoHabitacionId != null) {
            spec = spec.and(HuespedRegistroSpecification.tipoHabitacionIgual(tipoHabitacionId));
        }
        if (busqueda != null && !busqueda.isBlank()) {
            spec = spec.and(HuespedRegistroSpecification.busquedaHuesped(busqueda));
        }
        if (estado != null && !estado.isBlank()) {
            spec = spec.and(HuespedRegistroSpecification.estadoDetalleIgual(estado));
        }

        return spec;
    }

    /**
     * Convierte una entidad DetalleReservaHuesped a su DTO de respuesta.
     * Extrae datos del detalle de reserva, habitación, reserva y cliente asociados.
     *
     * @param huesped entidad de huésped del registro
     * @return el DTO con los datos formateados
     */
    private HuespedRegistroDTO toDTO(DetalleReservaHuesped huesped) {
        var detalle = huesped.getDetalleReserva();
        var habitacion = detalle.getHabitacion();
        var reserva = detalle.getReserva();
        var cliente = huesped.getCliente();

        return new HuespedRegistroDTO(
                huesped.getId(),
                habitacion.getNumero(),
                habitacion.getTipo().getNombre(),
                cliente.getCedula(),
                cliente.getNombreCompleto(),
                cliente.getApellidoCompleto(),
                reserva.getCodigoReserva(),
                reserva.getFechaCheckIn(),
                reserva.getFechaCheckOut(),
                detalle.getEstado(),
                reserva.getId()
        );
    }
}
