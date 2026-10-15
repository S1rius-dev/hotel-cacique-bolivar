package com.hotelcaciquebolivar.reservas.service.tipo_habitacion;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.TipoHabitacionPublicResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz del servicio de gestión de tipos de habitación del hotel.
 * Administra la creación, consulta, actualización y eliminación de tipos de habitación,
 * incluyendo validación de capacidades, precios y unicidad de nombres.
 * Expone una vista pública para el frontend de reservas de clientes.
 *
 * @author Hotel Cacique Bolívar
 */
public interface TipoHabitacionService {

    /**
     * Crea un nuevo tipo de habitación en el sistema.
     * Valida que el nombre no esté duplicado, que la capacidadBase no exceda
     * la capacidadMaxima y que los precios sean mayores a cero.
     *
     * @param dto datos del tipo de habitación a crear
     * @return el tipo de habitación creado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException si el nombre ya existe
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException         si falla alguna validación
     */
    TipoHabitacionResponse crear(TipoHabitacionRequest dto);

    /**
     * Busca un tipo de habitación por su identificador único.
     *
     * @param id identificador del tipo de habitación
     * @return el tipo de habitación encontrado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    TipoHabitacionResponse buscarPorId(Long id);

    /**
     * Lista todos los tipos de habitación de forma paginada.
     *
     * @param pageable configuración de paginación
     * @return página con los tipos de habitación encontrados
     */
    PageResponse<TipoHabitacionResponse> listarTodos(Pageable pageable);

    /**
     * Lista todos los tipos de habitación en formato público (sin datos internos).
     * Utilizado por el frontend de reservas de clientes para mostrar las opciones disponibles.
     *
     * @return lista de tipos de habitación en formato público
     */
    List<TipoHabitacionPublicResponse> listarTodosPublicos();

    /**
     * Actualiza un tipo de habitación existente.
     * Valida unicidad de nombre si se modifica y que capacidadBase no exceda capacidadMaxima.
     *
     * @param id  identificador del tipo de habitación a actualizar
     * @param dto datos actualizados
     * @return el tipo de habitación actualizado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException       si no se encuentra
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException si el nombre ya existe en otro registro
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException         si capacidadBase > capacidadMaxima
     */
    TipoHabitacionResponse actualizar(Long id, TipoHabitacionUpdateRequest dto);

    /**
     * Elimina un tipo de habitación por su identificador.
     * No permite eliminar si tiene habitaciones asociadas.
     *
     * @param id identificador del tipo de habitación a eliminar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException     si tiene habitaciones asociadas
     */
    void eliminar(Long id);
}