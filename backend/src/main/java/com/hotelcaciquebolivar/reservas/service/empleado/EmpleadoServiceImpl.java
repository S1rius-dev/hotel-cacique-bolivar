package com.hotelcaciquebolivar.reservas.service.empleado;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoRequest;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoResponse;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.empleado.Empleado;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.EmpleadoMapper;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.empleado.EmpleadoRepository;

/**
 * Implementación del servicio de gestión de empleados del sistema hotelero.
 * Maneja la creación, consulta, listado, actualización y eliminación de empleados,
 * incluyendo validación de campos obligatorios y formato de teléfono.
 */
@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;
    private final PageMapper pageMapper;

    /**
     * Constructor que inyecta todas las dependencias necesarias para el servicio de empleados.
     *
     * @param empleadoRepository repositorio de acceso a datos de empleados
     * @param empleadoMapper mapper para conversión entre entidades y DTOs de empleados
     * @param pageMapper mapper para conversión de páginas a respuestas paginadas
     */
    public EmpleadoServiceImpl(
            EmpleadoRepository empleadoRepository,
            EmpleadoMapper empleadoMapper,
            PageMapper pageMapper
    ) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.pageMapper = pageMapper;
    }

    /* =====================================================
     * CREAR
     * ===================================================== */
    /**
     * Crea un nuevo empleado en el sistema a partir de los datos proporcionados.
     * Valida los campos obligatorios y establece el estado como activo.
     *
     * @param dto objeto que contiene los datos necesarios para la creación del empleado
     * @return el empleado creado con el ID asignado
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    @Override
    public EmpleadoResponse crear(EmpleadoRequest dto) {

        validarCampos(dto);

        Empleado empleado = empleadoMapper.toEntity(dto);

        empleado.setActivo(true);

        Empleado guardado = empleadoRepository.save(empleado);

        return empleadoMapper.toAdminResponse(guardado);
    }

    /* =====================================================
     * BUSCAR POR ID
     * ===================================================== */
    /**
     * Busca un empleado por su identificador único.
     *
     * @param id identificador único del empleado a buscar
     * @return el empleado encontrado
     * @throws ResourceNotFoundException si el empleado con el ID especificado no existe
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse buscarPorId(Long id) {

        Empleado empleado = obtenerEmpleado(id);

        return empleadoMapper.toAdminResponse(empleado);
    }

    /* =====================================================
     * LISTAR
     * ===================================================== */
    /**
     * Lista todos los empleados del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de empleados
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmpleadoResponse> listarTodos(Pageable pageable) {

        Page<Empleado> page = empleadoRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, empleadoMapper::toAdminResponse);
    }

    /* =====================================================
     * ACTUALIZAR
     * ===================================================== */
    /**
     * Actualiza los datos de un empleado existente identificado por su ID.
     * Valida los campos obligatorios antes de actualizar.
     *
     * @param id identificador único del empleado a actualizar
     * @param dto objeto que contiene los nuevos datos del empleado
     * @return el empleado actualizado con los nuevos valores
     * @throws ResourceNotFoundException si el empleado con el ID especificado no existe
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    @Override
    public EmpleadoResponse actualizar(Long id, EmpleadoUpdateRequest dto) {

        Empleado empleado = obtenerEmpleado(id);

        validarCampos(dto);

        empleadoMapper.updateFromAdminDto(dto, empleado);

        Empleado actualizado = empleadoRepository.save(empleado);

        return empleadoMapper.toAdminResponse(actualizado);
    }

    /* =====================================================
     * ELIMINAR
     * ===================================================== */
    /**
     * Elimina un empleado del sistema por su identificador único.
     *
     * @param id identificador único del empleado a eliminar
     * @throws ResourceNotFoundException si el empleado con el ID especificado no existe
     */
    @Override
    public void eliminar(Long id) {

        Empleado empleado = obtenerEmpleado(id);

        empleadoRepository.delete(empleado);
    }

    /* =====================================================
     * MÉTODOS PRIVADOS
     * ===================================================== */
    /**
     * Busca un empleado por su ID y lanza una excepción si no existe.
     *
     * @param id identificador único del empleado
     * @return el empleado encontrado
     * @throws ResourceNotFoundException si el empleado con el ID especificado no existe
     */
    private Empleado obtenerEmpleado(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Empleado", id)
                );
    }

    /**
     * Valida los campos de un DTO de creación de empleado.
     *
     * @param dto objeto que contiene los datos del empleado a validar
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    private void validarCampos(EmpleadoRequest dto) {
        validarCampos(dto.nombres(), dto.apellidos(), dto.cargo(), dto.fechaIngreso(), dto.telefono(), dto.usuarioId());
    }

    /**
     * Valida los campos de un DTO de actualización de empleado.
     *
     * @param dto objeto que contiene los datos del empleado a validar
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    private void validarCampos(EmpleadoUpdateRequest dto) {
        validarCampos(dto.nombres(), dto.apellidos(), dto.cargo(), dto.fechaIngreso(), dto.telefono(), dto.usuarioId());
    }

    /**
     * Valida los campos obligatorios de un empleado: nombres, apellidos, cargo,
     * fecha de ingreso, teléfono (formato) y usuarioId.
     *
     * @param nombres nombres del empleado
     * @param apellidos apellidos del empleado
     * @param cargo cargo del empleado
     * @param fechaIngreso fecha de ingreso del empleado
     * @param telefono teléfono del empleado (formato: 10 dígitos)
     * @param usuarioId identificador del usuario asociado
     * @throws ValidationException si algún campo no cumple con los requisitos
     */
    private void validarCampos(String nombres, String apellidos, String cargo, LocalDate fechaIngreso, String telefono, Long usuarioId) {

        if (nombres == null || nombres.trim().length() < 3) {
            throw new ValidationException("nombres", "mínimo 3 caracteres");
        }

        if (apellidos == null || apellidos.trim().length() < 3) {
            throw new ValidationException("apellidos", "mínimo 3 caracteres");
        }

        if (cargo == null) {
            throw new ValidationException("cargo", "es obligatorio");
        }

        if (fechaIngreso == null) {
            throw new ValidationException("fechaIngreso", "es obligatoria");
        }

        if (telefono != null && !telefono.matches("^[0-9]{10}$")) {
            throw new ValidationException("telefono", "debe tener 10 dígitos");
        }

        if (usuarioId == null) {
            throw new ValidationException("usuarioId", "es obligatorio");
        }
    }
}
