package com.hotelcaciquebolivar.reservas.controller.pago;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.pago.MetricasPagosResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoDashboardResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoEventoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.SaldoPendiente;
import com.hotelcaciquebolivar.reservas.service.pago.PagoService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de pagos del hotel.
 * Permite registrar pagos, consultar pagos por reserva, reembolsar,
 * consultar saldo pendiente, listar pagos en dashboard y obtener métricas.
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /**
     * Registra un nuevo pago en el sistema.
     *
     * @param authentication objeto de autenticación del usuario que registra el pago
     * @param dto            datos del pago a registrar, validados con @Valid
     * @return el pago registrado
     */
    @Operation(operationId = "registrarPago")
    @PostMapping
    public ResponseEntity<PagoResponse> registrarPago(
            Authentication authentication,
            @Valid @RequestBody PagoRequest dto) {
        return ResponseEntity.ok(pagoService.registrar(dto, authentication.getName()));
    }

    /**
     * Obtiene un pago específico por su identificador.
     *
     * @param id identificador del pago
     * @return el pago encontrado
     */
    @Operation(operationId = "obtenerPago")
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPago(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    /**
     * Lista todos los pagos asociados a una reserva específica.
     *
     * @param reservaId identificador de la reserva
     * @return lista de pagos de la reserva
     */
    @Operation(operationId = "listarPagosPorReserva")
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<PagoResponse>> listarPagosPorReserva(@PathVariable Long reservaId) {
        return ResponseEntity.ok(pagoService.listarPorReserva(reservaId));
    }

    /**
     * Realiza el reembolso de un pago específico.
     *
     * @param authentication objeto de autenticación del usuario que realiza el reembolso
     * @param id             identificador del pago a reembolsar
     * @return el pago con estado reembolsado
     */
    @Operation(operationId = "reembolsarPago")
    @PostMapping("/{id}/reembolsar")
    public ResponseEntity<PagoResponse> reembolsarPago(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.reembolsar(id, authentication.getName()));
    }

    /**
     * Consulta el saldo pendiente de pago para una reserva específica.
     *
     * @param reservaId identificador de la reserva
     * @return saldo pendiente de la reserva
     */
    @Operation(operationId = "consultarSaldoPago")
    @GetMapping("/reserva/{reservaId}/saldo")
    public ResponseEntity<SaldoPendiente> consultarSaldo(@PathVariable Long reservaId) {
        return ResponseEntity.ok(pagoService.consultarSaldoPendiente(reservaId));
    }

    /**
     * Lista pagos en el dashboard con filtros opcionales y paginación.
     *
     * @param q       término de búsqueda general (opcional)
     * @param canal   filtro por canal de pago (opcional)
     * @param estado  filtro por estado del pago (opcional)
     * @param pageable parámetros de paginación
     * @return página de pagos para el dashboard
     */
    @Operation(operationId = "listarPagosDashboard")
    @GetMapping
    public ResponseEntity<Page<PagoDashboardResponse>> listarDashboard(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return ResponseEntity.ok(pagoService.listarDashboard(q, canal, estado, pageable));
    }

    /**
     * Obtiene métricas generales de pagos del sistema.
     *
     * @return métricas de pagos
     */
    @Operation(operationId = "metricasPagos")
    @GetMapping("/metricas")
    public ResponseEntity<MetricasPagosResponse> metricas() {
        return ResponseEntity.ok(pagoService.metricas());
    }

    /**
     * Lista los eventos asociados a un pago específico.
     *
     * @param pagoId identificador del pago
     * @return lista de eventos del pago
     */
    @Operation(operationId = "listarEventosPago")
    @GetMapping("/{pagoId}/eventos")
    public ResponseEntity<List<PagoEventoResponse>> eventos(@PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoService.listarEventos(pagoId));
    }
}
