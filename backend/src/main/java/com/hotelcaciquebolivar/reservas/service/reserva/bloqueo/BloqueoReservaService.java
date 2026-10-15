package com.hotelcaciquebolivar.reservas.service.reserva.bloqueo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.BloqueoReserva;

/**
 * Interfaz del servicio de bloqueo de reservas.
 * Define las operaciones para gestionar bloqueos de reserva que impiden la creación
 * de reservas en rangos de fechas específicos o de forma total.
 * Incluye funcionalidad para controlar el estado de las reservas web.
 */
public interface BloqueoReservaService {

    /**
     * Crea un nuevo bloqueo de reserva.
     *
     * @param request datos del bloqueo a crear
     * @return el bloqueo creado con su información completa
     */
    BloqueoReservaResponse crear(BloqueoReservaRequest request);

    /**
     * Lista todos los bloqueos de reserva activos.
     *
     * @return lista de bloqueos activos
     */
    List<BloqueoReservaResponse> listarTodos();

    /**
     * Busca un bloqueo de reserva por su identificador.
     *
     * @param id identificador del bloqueo
     * @return el bloqueo encontrado
     */
    BloqueoReservaResponse buscarPorId(Long id);

    /**
     * Actualiza un bloqueo de reserva existente.
     *
     * @param id      identificador del bloqueo a actualizar
     * @param request datos actualizados del bloqueo
     * @return el bloqueo actualizado
     */
    BloqueoReservaResponse actualizar(Long id, BloqueoReservaRequest request);

    /**
     * Desactiva un bloqueo de reserva (eliminación lógica).
     *
     * @param id identificador del bloqueo a desactivar
     */
    void desactivar(Long id);

    /**
     * Verifica si existe un bloqueo activo para una fecha específica.
     *
     * @param fecha fecha a verificar
     * @return true si existe un bloqueo para esa fecha
     */
    boolean existeBloqueoParaFecha(LocalDate fecha);

    /**
     * Verifica si existe un bloqueo activo para un rango de fechas.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return true si existe un bloqueo que cubre el rango especificado
     */
    boolean existeBloqueoParaRango(LocalDate desde, LocalDate hasta);

    /**
     * Busca un bloqueo activo que cubra un rango de fechas específico.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return optional con el bloqueo encontrado, vacío si no existe
     */
    Optional<BloqueoReserva> encontrarBloqueoParaRango(LocalDate desde, LocalDate hasta);

    /**
     * Verifica si existe un bloqueo total activo que impida todas las reservas.
     *
     * @return true si existe un bloqueo total activo
     */
    boolean existeBloqueoTotalActivo();

    /**
     * Obtiene el estado actual de las reservas web (habilitadas/deshabilitadas).
     *
     * @return true si las reservas web están habilitadas
     */
    boolean obtenerEstadoReservasWeb();

    /**
     * Cambia el estado de las reservas web (habilitar/deshabilitar).
     *
     * @param enabled true para habilitar, false para deshabilitar
     * @return el bloqueo actualizado con el nuevo estado
     */
    BloqueoReservaResponse toggleReservasWeb(boolean enabled);
}
