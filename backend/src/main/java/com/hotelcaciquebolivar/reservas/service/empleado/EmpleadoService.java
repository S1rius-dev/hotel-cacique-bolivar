package com.hotelcaciquebolivar.reservas.service.empleado;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoRequest;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoResponse;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz que define las operaciones de gestión de empleados del sistema hotelero.
 * Proporciona funcionalidades CRUD completas para empleados, incluyendo creación,
 * consulta, listado paginado, actualización y eliminación.
 */
public interface EmpleadoService {

    /**
     * Crea un nuevo empleado en el sistema a partir de los datos proporcionados.
     *
     * @param empleadoDTO objeto que contiene los datos necesarios para la creación del empleado
     * @return el empleado creado con el ID asignado
     */
    EmpleadoResponse crear(EmpleadoRequest empleadoDTO);

    /**
     * Busca un empleado por su identificador único.
     *
     * @param id identificador único del empleado a buscar
     * @return el empleado encontrado
     */
    EmpleadoResponse buscarPorId(Long id);

    /**
     * Lista todos los empleados del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de empleados
     */
    PageResponse<EmpleadoResponse> listarTodos(Pageable pageable);

    /**
     * Actualiza los datos de un empleado existente identificado por su ID.
     *
     * @param id identificador único del empleado a actualizar
     * @param empleadoDTO objeto que contiene los nuevos datos del empleado
     * @return el empleado actualizado con los nuevos valores
     */
    EmpleadoResponse actualizar(Long id, EmpleadoUpdateRequest empleadoDTO);

    /**
     * Elimina un empleado del sistema por su identificador único.
     *
     * @param id identificador único del empleado a eliminar
     */
    void eliminar(Long id);
}
