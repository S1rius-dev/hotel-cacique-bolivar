package com.hotelcaciquebolivar.reservas.service.precio_temporal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.PrecioTemporal;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PrecioTemporalMapper;
import com.hotelcaciquebolivar.reservas.repository.habitacion.PrecioTemporalRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.TipoHabitacionRepository;

/**
 * Implementación del servicio de precios temporales para habitaciones.
 * Gestiona rangos de fechas con precios especiales por tipo de habitación,
 * validando solapamientos de fechas y resolviendo el precio vigente para cada día
 * del rango de una reserva.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class PrecioTemporalServiceImpl implements PrecioTemporalService {

    private final PrecioTemporalRepository repository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final PrecioTemporalMapper mapper;

    public PrecioTemporalServiceImpl(
            PrecioTemporalRepository repository,
            TipoHabitacionRepository tipoHabitacionRepository,
            PrecioTemporalMapper mapper) {
        this.repository = repository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.mapper = mapper;
    }

    /** {@inheritDoc} */
    @Override
    public PrecioTemporalResponse crear(PrecioTemporalRequest dto) {
        validar(dto, null);
        TipoHabitacion tipo = obtenerTipo(dto.tipoHabitacionId());
        PrecioTemporal entidad = mapper.toEntity(dto);
        entidad.setTipoHabitacion(tipo);
        return mapper.toResponse(repository.save(entidad));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PrecioTemporalResponse buscarPorId(Long id) {
        return mapper.toResponse(obtener(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<PrecioTemporalResponse> listar(Long tipoId) {
        List<PrecioTemporal> precios;
        if (tipoId != null) {
            precios = repository.findByTipoHabitacionIdOrderByFechaInicio(tipoId);
        } else {
            precios = repository.findAll();
        }
        return precios.stream().map(mapper::toResponse).toList();
    }

    /** {@inheritDoc} */
    @Override
    public PrecioTemporalResponse actualizar(Long id, PrecioTemporalRequest dto) {
        PrecioTemporal existente = obtener(id);
        validar(dto, id);
        if (!existente.getTipoHabitacion().getId().equals(dto.tipoHabitacionId())) {
            existente.setTipoHabitacion(obtenerTipo(dto.tipoHabitacionId()));
        }
        mapper.updateFromDto(dto, existente);
        return mapper.toResponse(repository.save(existente));
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolverPrecioNoche(TipoHabitacion tipo, LocalDate fecha) {
        if (tipo == null || fecha == null) {
            return tipo != null ? tipo.getPrecioBase() : BigDecimal.ZERO;
        }
        return repository.findTopByTipoHabitacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByIdDesc(
                tipo.getId(), fecha, fecha)
                .map(PrecioTemporal::getPrecioNoche)
                .orElseGet(tipo::getPrecioBase);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolverPrecioNocheConIva(TipoHabitacion tipo, LocalDate fecha) {
        if (tipo == null || fecha == null) {
            return BigDecimal.ZERO;
        }
        return repository.findTopByTipoHabitacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByIdDesc(
                tipo.getId(), fecha, fecha)
                .map(pt -> pt.getPrecioNocheIva() != null
                        ? pt.getPrecioNocheIva()
                        : (tipo.getPrecioConIva() != null ? tipo.getPrecioConIva() : pt.getPrecioNoche()))
                .orElseGet(() -> tipo.getPrecioConIva() != null ? tipo.getPrecioConIva() : tipo.getPrecioBase());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolverPrecioPersonaExtra(TipoHabitacion tipo, LocalDate fecha) {
        if (tipo == null || fecha == null) {
            return tipo != null ? tipo.getPrecioPersonaExtra() : BigDecimal.ZERO;
        }
        return repository.findTopByTipoHabitacionIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByIdDesc(
                tipo.getId(), fecha, fecha)
                .map(PrecioTemporal::getPrecioPersonaExtra)
                .orElseGet(tipo::getPrecioPersonaExtra);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación optimizada: carga todos los precios temporales activos en el rango
     * y resuelve cada noche contra esa lista, evitando consultas individuales por día.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalNoches(
            TipoHabitacion tipo,
            BigDecimal precioNocheFallback,
            LocalDate checkIn,
            LocalDate checkOut) {
        if (tipo == null || checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            return BigDecimal.ZERO;
        }
        LocalDate ultimaNoche = checkOut.minusDays(1);
        List<PrecioTemporal> rangos = repository.findActivosEnRango(tipo.getId(), checkIn, ultimaNoche);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate fecha = checkIn; fecha.isBefore(checkOut); fecha = fecha.plusDays(1)) {
            total = total.add(resolverNocheEnRangos(rangos, fecha)
                    .orElse(precioNocheFallback != null ? precioNocheFallback : tipo.getPrecioBase()));
        }
        return total;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación optimizada: carga todos los precios temporales activos en el rango
     * y resuelve cada noche contra esa lista, multiplicando por personasExtras.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPersonasExtra(
            TipoHabitacion tipo,
            BigDecimal precioPersonaExtraFallback,
            int personasExtras,
            LocalDate checkIn,
            LocalDate checkOut) {
        if (tipo == null || personasExtras <= 0 || checkIn == null || checkOut == null
                || checkOut.isBefore(checkIn)) {
            return BigDecimal.ZERO;
        }
        LocalDate ultimaNoche = checkOut.minusDays(1);
        List<PrecioTemporal> rangos = repository.findActivosEnRango(tipo.getId(), checkIn, ultimaNoche);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate fecha = checkIn; fecha.isBefore(checkOut); fecha = fecha.plusDays(1)) {
            BigDecimal precioExtra = resolverExtraEnRangos(rangos, fecha)
                    .orElse(precioPersonaExtraFallback != null
                            ? precioPersonaExtraFallback : tipo.getPrecioPersonaExtra());
            total = total.add(precioExtra.multiply(BigDecimal.valueOf(personasExtras)));
        }
        return total;
    }

    /**
     * Resuelve el precio por noche entre los rangos de precios temporales cargados.
     *
     * @param rangos lista de precios temporales activos en el rango de la reserva
     * @param fecha  fecha a evaluar
     * @return el precio por noche si existe un rango que cubra la fecha; empty en caso contrario
     */
    private java.util.Optional<BigDecimal> resolverNocheEnRangos(
            List<PrecioTemporal> rangos, LocalDate fecha) {
        return rangos.stream()
                .filter(r -> r.cubre(fecha))
                .findFirst()
                .map(PrecioTemporal::getPrecioNoche);
    }

    /**
     * Resuelve el precio por persona extra entre los rangos de precios temporales cargados.
     *
     * @param rangos lista de precios temporales activos en el rango de la reserva
     * @param fecha  fecha a evaluar
     * @return el precio por persona extra si existe un rango que cubra la fecha; empty en caso contrario
     */
    private java.util.Optional<BigDecimal> resolverExtraEnRangos(
            List<PrecioTemporal> rangos, LocalDate fecha) {
        return rangos.stream()
                .filter(r -> r.cubre(fecha))
                .findFirst()
                .map(PrecioTemporal::getPrecioPersonaExtra);
    }

    /**
     * Obtiene un precio temporal por su identificador o lanza excepción si no existe.
     *
     * @param id identificador del precio temporal
     * @return la entidad PrecioTemporal
     * @throws ResourceNotFoundException si no se encuentra
     */
    private PrecioTemporal obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PrecioTemporal", id));
    }

    /**
     * Obtiene un tipo de habitación por su identificador o lanza excepción si no existe.
     *
     * @param tipoId identificador del tipo de habitación
     * @return la entidad TipoHabitacion
     * @throws ResourceNotFoundException si no se encuentra
     */
    private TipoHabitacion obtenerTipo(Long tipoId) {
        return tipoHabitacionRepository.findById(tipoId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion", tipoId));
    }

    /**
     * Valida la integridad de los datos de un precio temporal.
     * Verifica que fechaFin no sea anterior a fechaInicio y que no existan
     * solapamientos con registros existentes para el mismo tipo de habitación.
     *
     * @param dto datos del precio temporal a validar
     * @param id  identificador del registro (null al crear); se excluye de la validación de solapamiento
     * @throws ValidationException si las fechas son inválidas o existen solapamientos
     */
    private void validar(PrecioTemporalRequest dto, Long id) {
        if (dto.fechaFin().isBefore(dto.fechaInicio())) {
            throw new ValidationException("fechaFin", "no puede ser anterior a fechaInicio");
        }
        if (repository.existeSolapamiento(
                id != null ? id : -1L,
                dto.tipoHabitacionId(),
                dto.fechaInicio(),
                dto.fechaFin())) {
            throw new ValidationException("fechas",
                    "ya existe un precio temporal que se solapa con el rango indicado");
        }
    }
}