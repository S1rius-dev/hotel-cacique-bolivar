package com.hotelcaciquebolivar.reservas.service.cliente;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClientePublicResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz que define las operaciones de gestión de clientes del sistema hotelero.
 * Proporciona funcionalidades CRUD completas para clientes, incluyendo creación,
 * consulta, listado con filtros, búsqueda rápida, actualización, eliminación,
 * y vinculación de clientes con cuentas de usuario.
 */
public interface ClienteService {

    /**
     * Crea un nuevo cliente en el sistema a partir de los datos proporcionados.
     *
     * @param clienteDTO objeto que contiene los datos necesarios para la creación del cliente
     * @return el cliente creado con el ID asignado
     */
    ClienteResponse crear(ClienteRequest clienteDTO);

    /**
     * Busca un cliente por su identificador único.
     *
     * @param id identificador único del cliente a buscar
     * @return el cliente encontrado
     */
    ClienteResponse buscarPorId(Long id);

    /**
     * Lista todos los clientes del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de clientes
     */
    PageResponse<ClienteResponse> listarTodos(Pageable pageable);

    /**
     * Lista los clientes del sistema con filtros opcionales de forma paginada.
     *
     * @param nombre nombre o apellido del cliente para filtrar (parcial, case-insensitive)
     * @param cedula número de cédula del cliente para filtrar (parcial)
     * @param telefono número de teléfono del cliente para filtrar (parcial)
     * @param tipoIdentificacionId identificador del tipo de identificación para filtrar
     * @param activo estado del cliente para filtrar (true/false/null para todos)
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene los clientes que coinciden con los filtros
     */
    PageResponse<ClienteResponse> listarConFiltros(
            String nombre, String cedula, String telefono,
            Long tipoIdentificacionId, Boolean activo, Pageable pageable);

    /**
     * Realiza una búsqueda rápida de clientes por nombre, cédula o teléfono.
     * Retorna un número limitado de resultados.
     *
     * @param q término de búsqueda general
     * @param limit cantidad máxima de resultados a retornar
     * @return lista de clientes que coinciden con la búsqueda
     */
    List<ClienteResponse> buscarRapido(String q, int limit);

    /**
     * Actualiza los datos de un cliente existente identificado por su ID.
     *
     * @param id identificador único del cliente a actualizar
     * @param clienteDTO objeto que contiene los nuevos datos del cliente
     * @return el cliente actualizado con los nuevos valores
     */
    ClienteResponse actualizar(Long id, ClienteUpdateRequest clienteDTO);

    /**
     * Elimina un cliente del sistema por su identificador único.
     *
     * @param id identificador único del cliente a eliminar
     */
    void eliminar(Long id);

    /**
     * Crea un perfil de cliente vinculado a una cuenta de usuario existente.
     * Utilizado para que los usuarios CLIENTE creen su propio perfil de cliente.
     *
     * @param dto objeto que contiene los datos del cliente a crear
     * @param email email del usuario autenticado que crea el perfil
     * @return respuesta pública con los datos del cliente creado
     */
    ClientePublicResponse crearMiPerfil(ClienteRequest dto, String email);

    /**
     * Busca un cliente por tipo de identificación y número de cédula.
     *
     * @param tipoIdentificacionId identificador del tipo de identificación
     * @param cedula número de cédula del cliente
     * @return el cliente encontrado
     */
    ClienteResponse buscarPorTipoYCedula(Long tipoIdentificacionId, String cedula);

    /**
     * Vincula un cliente existente con una cuenta de usuario.
     * Utilizado para asociar un cliente previamente registrado con una cuenta de usuario.
     *
     * @param clienteId identificador único del cliente a vincular
     * @param email email del usuario al que se vinculará el cliente
     * @return respuesta pública con los datos del cliente vinculado
     */
    ClientePublicResponse vincularCliente(Long clienteId, String email);
}
