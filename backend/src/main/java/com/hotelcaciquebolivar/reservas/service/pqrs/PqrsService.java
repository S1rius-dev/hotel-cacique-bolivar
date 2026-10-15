package com.hotelcaciquebolivar.reservas.service.pqrs;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsResponse;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsStateChangeRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.pqrs.EstadosPqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;

/**
 * Interfaz del servicio de gestión de PQRS (Peticiones, Quejas, Reclamos, Sugerencias).
 * Define las operaciones CRUD y de consulta para el ciclo de vida completo de las PQRS,
 * incluyendo creación, consulta, cambio de estado, actualización y eliminación.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PqrsService {

    /**
     * Crea una nueva PQRS en el sistema.
     * Asocia la PQRS al usuario identificado por el email (si es proporcionado) y a una reserva
     * (si se especifica un reservaId). Genera un código único, asigna prioridad según el tipo
     * y envía notificaciones y eventos SSE.
     *
     * @param dto   datos de la PQRS a crear
     * @param email email del usuario autenticado que genera la PQRS; puede ser null
     * @return la PQRS creada con su código generado y estado inicial
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si el reservaId especificado no existe
     */
    PqrsResponse crear(PqrsRequest dto, String email);

    /**
     * Busca una PQRS por su identificador único.
     *
     * @param id identificador de la PQRS
     * @return la PQRS encontrada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra la PQRS
     */
    PqrsResponse buscarPorId(Long id);

    /**
     * Busca una PQRS por su código único (formato PQRS-yyyyMMdd-00000).
     *
     * @param codigo código único de la PQRS
     * @return la PQRS encontrada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra una PQRS con el código especificado
     */
    PqrsResponse buscarPorCodigo(String codigo);

    /**
     * Lista todas las PQRS de forma paginada.
     *
     * @param pageable configuración de paginación
     * @return página con las PQRS encontradas
     */
    PageResponse<PqrsResponse> listarTodos(Pageable pageable);

    /**
     * Lista las PQRS asociadas a un usuario específico de forma paginada.
     *
     * @param usuarioId identificador del usuario propietario
     * @param pageable  configuración de paginación
     * @return página con las PQRS del usuario
     */
    PageResponse<PqrsResponse> listarPorUsuario(Long usuarioId, Pageable pageable);

    /**
     * Lista las PQRS filtradas por estado de forma paginada.
     *
     * @param estado   estado de la PQRS para filtrar
     * @param pageable configuración de paginación
     * @return página con las PQRS en el estado especificado
     */
    PageResponse<PqrsResponse> listarPorEstado(EstadosPqrs estado, Pageable pageable);

    /**
     * Lista las PQRS filtradas por tipo de forma paginada.
     *
     * @param tipo     tipo de PQRS para filtrar
     * @param pageable configuración de paginación
     * @return página con las PQRS del tipo especificado
     */
    PageResponse<PqrsResponse> listarPorTipo(TipoPqrs tipo, Pageable pageable);

    /**
     * Cambia el estado de una PQRS según la máquina de estados definida.
     * Valida las transiciones permitidas: ABIERTA → EN_REVISION → RESPONDIDA,
     * CERRADA/RECHAZADA → ABIERTA, etc. Al pasar a RESPONDIDA envía correo electrónico
     * al contacto y genera notificaciones y eventos SSE.
     *
     * @param id  identificador de la PQRS
     * @param dto datos del cambio de estado, incluyendo el nuevo estado y respuesta opcional
     * @return la PQRS con el estado actualizado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra la PQRS
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException     si la transición de estado no es válida o falta la respuesta requerida
     */
    PqrsResponse cambiarEstado(Long id, PqrsStateChangeRequest dto);

    /**
     * Actualiza los campos editables de una PQRS existente.
     * No permite modificar PQRS en estado CERRADA o RECHAZADA.
     * Si se cambia el tipo, recalcula la prioridad automáticamente.
     *
     * @param id  identificador de la PQRS a actualizar
     * @param dto datos actualizados
     * @return la PQRS actualizada
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra la PQRS
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException     si la PQRS está en estado CERRADA o RECHAZADA
     */
    PqrsResponse actualizar(Long id, PqrsUpdateRequest dto);

    /**
     * Elimina una PQRS del sistema por su identificador.
     *
     * @param id identificador de la PQRS a eliminar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra la PQRS
     */
    void eliminar(Long id);
}
