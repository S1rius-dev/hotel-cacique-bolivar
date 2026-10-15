package com.hotelcaciquebolivar.reservas.service.alerta;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaRequest;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaResponse;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz que define las operaciones de negocio para la gestión de alertas del sistema hotelero.
 * Proporciona funcionalidades CRUD para alertas asociadas a clientes, incluyendo
 * creación, consulta, listado paginado, actualización y eliminación.
 */
public interface AlertaService {

    /**
     * Crea una nueva alerta en el sistema a partir de los datos proporcionados.
     *
     * @param dto objeto que contiene los datos necesarios para la creación de la alerta
     * @return la alerta creada con el ID asignado
     */
    AlertaResponse crear(AlertaRequest dto);

    /**
     * Busca una alerta por su identificador único.
     *
     * @param id identificador único de la alerta a buscar
     * @return la alerta encontrada
     */
    AlertaResponse buscarPorId(Long id);

    /**
     * Lista todas las alertas del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de alertas
     */
    PageResponse<AlertaResponse> listarTodos(Pageable pageable);

    /**
     * Lista todas las alertas asociadas a un cliente específico de forma paginada.
     *
     * @param clienteId identificador único del cliente
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las alertas del cliente especificado
     */
    PageResponse<AlertaResponse> listarPorCliente(Long clienteId, Pageable pageable);

    /**
     * Actualiza los datos de una alerta existente identificada por su ID.
     *
     * @param id identificador único de la alerta a actualizar
     * @param dto objeto que contiene los nuevos datos de la alerta
     * @return la alerta actualizada
     */
    AlertaResponse actualizar(Long id, AlertaUpdateRequest dto);

    /**
     * Elimina una alerta del sistema por su identificador único.
     *
     * @param id identificador único de la alerta a eliminar
     */
    void eliminar(Long id);
}