package com.hotelcaciquebolivar.reservas.service.resena;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.resena.PublicResenasResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaRequest;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz del servicio de gestión de reseñas del hotel.
 * Permite crear, consultar, actualizar y eliminar reseñas de clientes,
 * así como listar reseñas públicas (últimas 12) y filtrar por calificación o cliente.
 *
 * @author Hotel Cacique Bolívar
 */
public interface ResenaService {

    /**
     * Crea una nueva reseña en el sistema.
     * Asocia la reseña al usuario si se proporciona email, completando nombre
     * e imagen de perfil desde el usuario si no se proporcionaron en el DTO.
     *
     * @param dto   datos de la reseña a crear
     * @param email email del usuario autenticado; puede ser null
     * @return la reseña creada
     */
    ResenaResponse crear(ResenaRequest dto, String email);

    /**
     * Busca una reseña por su identificador único.
     *
     * @param id identificador de la reseña
     * @return la reseña encontrada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    ResenaResponse buscarPorId(Long id);

    /**
     * Lista las últimas 12 reseñas públicas ordenadas por fecha de creación descendente.
     * Utilizado para mostrar reseñas destacadas en la página pública del hotel.
     *
     * @return respuesta con la lista de reseñas públicas
     */
    PublicResenasResponse listarPublicas();

    /**
     * Lista todas las reseñas de forma paginada, ordenadas por fecha de creación descendente.
     *
     * @param pageable configuración de paginación
     * @return página con las reseñas encontradas
     */
    PageResponse<ResenaResponse> listarTodos(Pageable pageable);

    /**
     * Lista reseñas filtradas por calificación de forma paginada.
     *
     * @param calificacion valor de calificación para filtrar (1-5)
     * @param pageable     configuración de paginación
     * @return página con las reseñas que coincidan con la calificación
     */
    PageResponse<ResenaResponse> listarPorCalificacion(Integer calificacion, Pageable pageable);

    /**
     * Lista reseñas asociadas a un cliente específico de forma paginada.
     *
     * @param clienteId identificador del cliente propietario
     * @param pageable  configuración de paginación
     * @return página con las reseñas del cliente
     */
    PageResponse<ResenaResponse> listarPorCliente(Long clienteId, Pageable pageable);

    /**
     * Actualiza una reseña existente.
     *
     * @param id  identificador de la reseña a actualizar
     * @param dto datos actualizados
     * @return la reseña actualizada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    ResenaResponse actualizar(Long id, ResenaUpdateRequest dto);

    /**
     * Elimina una reseña del sistema por su identificador.
     *
     * @param id identificador de la reseña a eliminar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    void eliminar(Long id);
}
