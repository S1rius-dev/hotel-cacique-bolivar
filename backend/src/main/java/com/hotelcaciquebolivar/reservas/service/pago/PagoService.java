package com.hotelcaciquebolivar.reservas.service.pago;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.pago.MetricasPagosResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoEventoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.SaldoPendiente;

/**
 * Interfaz del servicio de gestión de pagos del hotel.
 * Define las operaciones para registrar, consultar, reembolsar pagos,
 * así como obtener métricas y eventos del timeline de pagos.
 *
 * @author Hotel Cacique Bolívar
 */
public interface PagoService {

    /**
     * Registra un nuevo pago asociado a una reserva.
     * Valida que la reserva esté en estado hábil y que el monto no supere el saldo pendiente.
     * Para pagos en efectivo genera un código de recibo consecutivo.
     *
     * @param dto datos del pago a registrar
     * @param email correo del usuario que realiza el registro (para auditoría)
     * @return el pago registrado con su respuesta DTO
     * @throws ResourceNotFoundException si la reserva no existe
     * @throws ValidationException si la reserva está cancelada/finalizada o el monto supera el saldo
     */
    PagoResponse registrar(PagoRequest dto, String email);

    /**
     * Busca un pago por su identificador único.
     *
     * @param id identificador del pago
     * @return el pago encontrado como DTO
     * @throws ResourceNotFoundException si el pago no existe
     */
    PagoResponse buscarPorId(Long id);

    /**
     * Lista todos los pagos asociados a una reserva, ordenados por fecha de pago descendente.
     *
     * @param reservaId identificador de la reserva
     * @return lista de pagos de la reserva
     */
    List<PagoResponse> listarPorReserva(Long reservaId);

    /**
     * Reembolsa un pago previamente completado.
     * Marca el pago como reembolsado y si no quedan pagos activos,
     * revierte la reserva a estado PENDIENTE.
     *
     * @param id identificador del pago a reembolsar
     * @param email correo del usuario que realiza el reembolso (para auditoría)
     * @return el pago reembolsado como DTO
     * @throws ResourceNotFoundException si el pago no existe
     * @throws ValidationException si el pago no está en estado COMPLETADO o la reserva está cancelada
     */
    PagoResponse reembolsar(Long id, String email);

    /**
     * Consulta el saldo pendiente de pago de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @return objeto con el desglose de total, pagado y saldo pendiente
     * @throws ResourceNotFoundException si la reserva no existe
     */
    SaldoPendiente consultarSaldoPendiente(Long reservaId);

    /**
     * Lista pagos para el dashboard con filtros y paginación.
     * Combina pagos registrados y transacciones Wompi pendientes/rechazadas sin pago asociado.
     *
     * @param q texto de búsqueda (nombre cliente, código reserva, recibo, referencia, habitación)
     * @param canal filtro por canal de pago ("efectivo", "wompi", "todos" o null)
     * @param estado filtro por estado ("aprobada", "pendiente", "rechazada", "todos" o null)
     * @param pageable configuración de paginación
     * @return página de resultados del dashboard
     */
    Page<PagoDashboardResponse> listarDashboard(String q, String canal, String estado, Pageable pageable);

    /**
     * Calcula métricas consolidadas de pagos para el dashboard.
     * Incluye recaudado del día, totales por canal (Wompi/efectivo),
     * pendientes, cuadre de caja recepción y tienda.
     *
     * @return métricas consolidadas de pagos
     */
    MetricasPagosResponse metricas();

    /**
     * Lista los eventos auditables (timeline) de un pago específico.
     * Si no existen eventos persistidos, genera eventos sintéticos basados en el estado del pago.
     *
     * @param pagoId identificador del pago
     * @return lista de eventos del timeline ordenados cronológicamente
     * @throws ResourceNotFoundException si el pago no existe
     */
    List<PagoEventoResponse> listarEventos(Long pagoId);
}
