package com.hotelcaciquebolivar.reservas.service.habitacion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.LimpiarHabitacionesResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.HabitacionEstadoDiario;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz que define las operaciones de gestión de habitaciones del sistema hotelero.
 * Proporciona funcionalidades CRUD completas para habitaciones, incluyendo creación,
 * consulta, listado paginado, actualización, eliminación, consulta de estado diario,
 * y marcado de habitaciones como limpias.
 */
public interface HabitacionService {

    /**
     * Crea una nueva habitación en el sistema a partir de los datos proporcionados.
     *
     * @param dto objeto que contiene los datos necesarios para la creación de la habitación
     * @return la habitación creada con el ID asignado
     */
    HabitacionResponse crear(HabitacionRequest dto);

    /**
     * Busca una habitación por su identificador único.
     *
     * @param id identificador único de la habitación a buscar
     * @return la habitación encontrada
     */
    HabitacionResponse buscarPorId(Long id);

    /**
     * Actualiza los datos de una habitación existente identificada por su ID.
     *
     * @param id identificador único de la habitación a actualizar
     * @param dto objeto que contiene los nuevos datos de la habitación
     * @return la habitación actualizada con los nuevos valores
     */
    HabitacionResponse actualizar(Long id, HabitacionUpdateRequest dto);

    /**
     * Lista todas las habitaciones del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de habitaciones
     */
    PageResponse<HabitacionResponse> listarTodos(Pageable pageable);

    /**
     * Elimina una habitación del sistema por su identificador único.
     *
     * @param id identificador único de la habitación a eliminar
     */
    void eliminar(Long id);

    /**
     * Obtiene el estado diario de todas las habitaciones para una fecha específica.
     * Calcula el estado derivado de cada habitación basándose en las reservas activas.
     *
     * @param fecha fecha para la que se desea conocer el estado de las habitaciones
     * @return lista de estados diarios de las habitaciones
     */
    List<HabitacionEstadoDiario> obtenerEstadoDiario(LocalDate fecha);

    /**
     * Marca una habitación como limpia, cambiando su estado físico a DISPONIBLE.
     *
     * @param id identificador único de la habitación a marcar como limpia
     * @return la habitación actualizada con el nuevo estado
     */
    HabitacionResponse marcarComoLimpia(Long id);

    /**
     * Marca múltiples habitaciones como limpias de forma masiva.
     *
     * @param ids lista de identificadores únicos de las habitaciones a marcar como limpias
     * @return respuesta con la cantidad de habitaciones limpiadas exitosamente
     */
    LimpiarHabitacionesResponse limpiar(List<Long> ids);
}
