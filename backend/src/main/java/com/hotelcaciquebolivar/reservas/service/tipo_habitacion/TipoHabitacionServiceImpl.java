package com.hotelcaciquebolivar.reservas.service.tipo_habitacion;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.TipoHabitacionPublicResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.mapper.TipoHabitacionMapper;
import com.hotelcaciquebolivar.reservas.repository.habitacion.TipoHabitacionRepository;

/**
 * Implementación del servicio de gestión de tipos de habitación.
 * Administra la creación, consulta, actualización y eliminación con validaciones
 * de unicidad de nombre, integridad de capacidades y prevención de eliminación
 * cuando existen habitaciones asociadas.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class TipoHabitacionServiceImpl implements TipoHabitacionService {

    private final TipoHabitacionRepository repository;
    private final TipoHabitacionMapper mapper;
    private final PageMapper pageMapper;

    public TipoHabitacionServiceImpl(
            TipoHabitacionRepository repository,
            TipoHabitacionMapper mapper,
            PageMapper pageMapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    /** {@inheritDoc} */
    @Override
    public TipoHabitacionResponse crear(TipoHabitacionRequest dto) {
        validarCreacion(dto);
        repository.findByNombre(dto.nombre())
                .ifPresent(t -> {
                    throw new ResourceAlreadyExistsException(
                            "TipoHabitacion", "nombre", dto.nombre());
                });
        TipoHabitacion tipo = mapper.toEntity(dto);
        return mapper.toResponse(repository.save(tipo));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TipoHabitacionResponse buscarPorId(Long id) {
        return mapper.toResponse(obtener(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TipoHabitacionResponse> listarTodos(Pageable pageable) {
        Page<TipoHabitacion> page = repository.findAll(pageable);
        return pageMapper.toPageResponse(page, mapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TipoHabitacionPublicResponse> listarTodosPublicos() {
        return repository.findAll().stream()
                .map(mapper::toPublicResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public TipoHabitacionResponse actualizar(Long id, TipoHabitacionUpdateRequest dto) {
        TipoHabitacion existente = obtener(id);

        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            repository.findByNombre(dto.nombre())
                    .filter(t -> !t.getId().equals(id))
                    .ifPresent(t -> {
                        throw new ResourceAlreadyExistsException(
                                "TipoHabitacion", "nombre", dto.nombre());
                    });
        }

        mapper.updateFromDto(dto, existente);
        validarCapacidad(existente);

        return mapper.toResponse(repository.save(existente));
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(Long id) {
        TipoHabitacion tipo = obtener(id);
        if (tipo.getHabitaciones() != null && !tipo.getHabitaciones().isEmpty()) {
            throw new ValidationException(
                    "tipoHabitacion",
                    "no se puede eliminar porque tiene habitaciones asociadas");
        }
        repository.delete(tipo);
    }

    /**
     * Obtiene un tipo de habitación por su identificador o lanza excepción si no existe.
     *
     * @param id identificador del tipo de habitación
     * @return la entidad TipoHabitacion
     * @throws ResourceNotFoundException si no se encuentra
     */
    private TipoHabitacion obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion", id));
    }

    /**
     * Valida los datos obligatorios para la creación de un tipo de habitación.
     * Verifica que nombre no esté vacío, capacidades mayores a cero,
     * capacidadBase ≤ capacidadMaxima, y precios mayores a cero.
     *
     * @param dto datos a validar
     * @throws ValidationException si algún dato no cumple las restricciones
     */
    private void validarCreacion(TipoHabitacionRequest dto) {
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new ValidationException("nombre", "es obligatorio");
        }
        if (dto.capacidadBase() == null || dto.capacidadBase() <= 0) {
            throw new ValidationException("capacidadBase", "debe ser mayor a 0");
        }
        if (dto.capacidadMaxima() == null || dto.capacidadMaxima() <= 0) {
            throw new ValidationException("capacidadMaxima", "debe ser mayor a 0");
        }
        if (dto.capacidadBase() > dto.capacidadMaxima()) {
            throw new ValidationException("capacidadBase", "no puede ser mayor a capacidadMaxima");
        }
        if (dto.precioBase() == null || dto.precioBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("precioBase", "debe ser mayor a 0");
        }
        if (dto.precioPersonaExtra() == null || dto.precioPersonaExtra().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("precioPersonaExtra", "debe ser mayor a 0");
        }
    }

    /**
     * Valida que la capacidadBase no sea mayor que la capacidadMaxima de un tipo de habitación.
     *
     * @param tipo tipo de habitación a validar
     * @throws ValidationException si capacidadBase > capacidadMaxima
     */
    private void validarCapacidad(TipoHabitacion tipo) {
        if (tipo.getCapacidadBase() > tipo.getCapacidadMaxima()) {
            throw new ValidationException("capacidadBase", "no puede ser mayor a capacidadMaxima");
        }
    }
}
