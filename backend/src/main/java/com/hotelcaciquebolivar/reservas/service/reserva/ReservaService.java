package com.hotelcaciquebolivar.reservas.service.reserva;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.reserva.ActualizarDetalleRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleCheckInRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRapidaRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;

/**
 * Interfaz principal del servicio de reservas.
 * Define el contrato para la gestión completa del ciclo de vida de reservas del hotel,
 * incluyendo creación, modificación, check-in/check-out, disponibilidad, precios,
 * extensiones, ajustes y operaciones de reserva rápida.
 */
public interface ReservaService {

    /**
     * Crea una nueva reserva en el sistema.
     *
     * @param dto datos de la reserva a crear
     * @return la reserva creada con su información completa
     */
    ReservaResponse crear(ReservaRequest dto);

    /**
     * Busca una reserva por su identificador único.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada con su información completa
     */
    ReservaResponse buscarPorId(Long id);

    /**
     * Actualiza la información de una reserva existente.
     *
     * @param id  identificador de la reserva a actualizar
     * @param dto datos actualizados de la reserva
     * @return la reserva actualizada con su información completa
     */
    ReservaResponse actualizar(Long id, ReservaUpdateRequest dto);

    /**
     * Lista todas las reservas con filtros y paginación.
     *
     * @param codigo        filtro por código de reserva (parcial, insensible a mayúsculas)
     * @param cliente       filtro por nombre del cliente (parcial, insensible a mayúsculas)
     * @param estado        filtro por estado de la reserva
     * @param origen        filtro por origen de la reserva
     * @param checkInDesde  filtro por fecha de check-in desde
     * @param checkInHasta  filtro por fecha de check-in hasta
     * @param checkOutDesde filtro por fecha de check-out desde
     * @param checkOutHasta filtro por fecha de check-out hasta
     * @param pageable      configuración de paginación
     * @return página de reservas que cumplen con los filtros especificados
     */
    PageResponse<ReservaResponse> listarTodos(
            String codigo, String cliente, EstadoReserva estado,
            OrigenReserva origen,
            LocalDate checkInDesde, LocalDate checkInHasta,
            LocalDate checkOutDesde, LocalDate checkOutHasta,
            Pageable pageable);

    /**
     * Elimina una reserva del sistema.
     *
     * @param id identificador de la reserva a eliminar
     */
    void eliminar(Long id);

    /**
     * Consulta la disponibilidad de habitaciones para un rango de fechas específico.
     *
     * @param checkIn  fecha de check-in
     * @param checkOut fecha de check-out
     * @return lista de habitaciones disponibles con su información
     */
    List<DisponibilidadResponse> consultarDisponibilidad(LocalDate checkIn, LocalDate checkOut);

    /**
     * Realiza el check-in de una reserva, cambiando su estado a EN_CURSO.
     *
     * @param reservaId identificador de la reserva
     * @return la reserva con su estado actualizado
     */
    ReservaResponse realizarCheckIn(Long reservaId);

    /**
     * Realiza el check-out de una reserva, cambiando su estado a FINALIZADA.
     *
     * @param reservaId identificador de la reserva
     * @return la reserva con su estado actualizado
     */
    ReservaResponse realizarCheckOut(Long reservaId);

    /**
     * Crea una reserva rápida con habitación asignada automáticamente.
     *
     * @param dto datos de la reserva rápida
     * @return la reserva creada con habitación asignada
     */
    ReservaResponse crearReservaRapida(ReservaRapidaRequest dto);

    /**
     * Realiza el check-in de un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle de reserva
     * @param dto       datos del check-in del detalle
     * @return la reserva con el detalle actualizado
     */
    ReservaResponse checkInPorDetalle(Long reservaId, Long detalleId, DetalleCheckInRequest dto);

    /**
     * Cambia el modo de IVA para un detalle de reserva (con/sin IVA).
     * Permite intercambiar entre precio con IVA y precio sin IVA sobre el mismo snapshot.
     *
     * @param reservaId  identificador de la reserva
     * @param detalleId  identificador del detalle de reserva
     * @param incluyeIva true si aplica precio con IVA incluido
     * @return la reserva actualizada
     */
    ReservaResponse togglePrecioModo(Long reservaId, Long detalleId, boolean incluyeIva);

    /**
     * Realiza el check-out de un detalle espec��fico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle de reserva
     * @return la reserva con el detalle actualizado
     */
    ReservaResponse checkOutPorDetalle(Long reservaId, Long detalleId);

    /**
     * Extiende la estadía de una reserva modificando la fecha de check-out.
     *
     * @param reservaId       identificador de la reserva
     * @param nuevaFechaCheckOut nueva fecha de check-out
     * @return la reserva con la fecha actualizada
     */
    ReservaResponse extenderEstadia(Long reservaId, LocalDate nuevaFechaCheckOut);

    /**
     * Agrega huéspedes a un detalle específico de una reserva.
     *
     * @param reservaId    identificador de la reserva
     * @param detalleId    identificador del detalle de reserva
     * @param huespedesIds lista de identificadores de huéspedes a agregar
     * @return la reserva con los huéspedes agregados
     */
    ReservaResponse agregarHuespedes(Long reservaId, Long detalleId, List<Long> huespedesIds);

    /**
     * Remueve un huésped de un detalle específico de una reserva.
     *
     * @param reservaId        identificador de la reserva
     * @param detalleId        identificador del detalle de reserva
     * @param detalleHuespedId identificador del detalle-huésped a remover
     * @return la reserva con el huésped removido
     */
    ReservaResponse removerHuesped(Long reservaId, Long detalleId, Long detalleHuespedId);

    /**
     * Remueve un detalle de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle a remover
     * @return la reserva sin el detalle removido
     */
    ReservaResponse removerDetalle(Long reservaId, Long detalleId);

    /**
     * Agrega un nuevo detalle a una reserva existente.
     *
     * @param reservaId identificador de la reserva
     * @param dto       datos del nuevo detalle
     * @return la reserva con el detalle agregado
     */
    ReservaResponse agregarDetalle(Long reservaId, DetalleReservaRequest dto);

    /**
     * Actualiza un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle a actualizar
     * @param dto       datos actualizados del detalle
     * @return la reserva con el detalle actualizado
     */
    ReservaResponse actualizarDetalle(Long reservaId, Long detalleId, ActualizarDetalleRequest dto);

    /**
     * Procesa automáticamente los detalles de reserva cuya fecha de check-out ha vencido.
     * Método programado que ejecuta el check-out automático de huéspedes que no hicieron
     * check-out a tiempo.
     */
    void procesarDetallesVencidos();

    // Ajustes
    /**
     * Agrega un ajuste (cargo extra o descuento) a una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param dto       datos del ajuste a agregar
     * @return el ajuste creado
     */
    AjusteResponse agregarAjuste(Long reservaId, AjusteRequest dto);

    /**
     * Edita un ajuste existente de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId  identificador del ajuste a editar
     * @param dto       datos actualizados del ajuste
     * @return el ajuste actualizado
     */
    AjusteResponse editarAjuste(Long reservaId, Long ajusteId, AjusteRequest dto);

    /**
     * Elimina un ajuste de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId  identificador del ajuste a eliminar
     */
    void eliminarAjuste(Long reservaId, Long ajusteId);
}
