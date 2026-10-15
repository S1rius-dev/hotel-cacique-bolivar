package com.hotelcaciquebolivar.reservas.controller.reserva.customer;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.CustomerReservationResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.DisponibilidadPorTipoResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.TipoHabitacionPublicResponse;
import com.hotelcaciquebolivar.reservas.service.reserva.customer.CustomerReservationService;
import com.hotelcaciquebolivar.reservas.service.tipo_habitacion.TipoHabitacionService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/**
 * Controlador REST público para reservas de clientes (huéspedes).
 * Permite a los clientes consultar tipos de habitación, disponibilidad,
 * precios efectivos y crear reservas sin necesidad de autenticación.
 * Endpoint público para el sitio web del hotel.
 */
@RestController
@RequestMapping("/api/public/reservas")
public class CustomerReservationController {

    private final CustomerReservationService customerReservationService;
    private final TipoHabitacionService tipoHabitacionService;

    /**
     * Constructor que inyecta los servicios necesarios.
     *
     * @param customerReservationService servicio de reservas de clientes
     * @param tipoHabitacionService      servicio de tipos de habitación
     */
    public CustomerReservationController(
            CustomerReservationService customerReservationService,
            TipoHabitacionService tipoHabitacionService) {
        this.customerReservationService = customerReservationService;
        this.tipoHabitacionService = tipoHabitacionService;
    }

    /**
     * Lista todos los tipos de habitación disponibles para el público.
     *
     * @return lista de tipos de habitación públicos
     */
    @Operation(operationId = "listarTiposHabitacionPublic")
    @GetMapping("/tipos-habitacion")
    public ResponseEntity<List<TipoHabitacionPublicResponse>> listarTiposHabitacion() {
        return ResponseEntity.ok(tipoHabitacionService.listarTodosPublicos());
    }

    /**
     * Consulta la disponibilidad de habitaciones por tipo para un rango de fechas.
     *
     * @param checkIn  fecha de check-in (formato ISO: YYYY-MM-DD)
     * @param checkOut fecha de check-out (formato ISO: YYYY-MM-DD)
     * @return lista de disponibilidad por tipo de habitación
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadPorTipoResponse>> consultarDisponibilidadPublic(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(
                customerReservationService.consultarDisponibilidadPorTipo(checkIn, checkOut));
    }

    /**
     * Consulta el precio efectivo para un tipo de habitación en un rango de fechas.
     * Considera precios temporales y descuentos aplicables.
     *
     * @param id       identificador del tipo de habitación
     * @param checkIn  fecha de check-in (formato ISO: YYYY-MM-DD)
     * @param checkOut fecha de check-out (formato ISO: YYYY-MM-DD)
     * @return precio efectivo calculado para el tipo y rango de fechas
     */
    @GetMapping("/tipos/{id}/precio")
    public ResponseEntity<com.hotelcaciquebolivar.reservas.dto.reserva.customer.PrecioEfectivoResponse> consultarPrecioEfectivo(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(customerReservationService.consultarPrecioEfectivo(id, checkIn, checkOut));
    }

    /**
     * Crea una nueva reserva desde el sitio web público.
     *
     * @param request datos de la reserva a crear, validados con @Valid
     * @return ResponseEntity con la reserva creada, código 201 Created
     */
    @Operation(operationId = "crearReservaPublica")
    @PostMapping
    public ResponseEntity<CustomerReservationResponse> crear(
            @Valid @RequestBody CustomerReservationRequest request) {
        CustomerReservationResponse response = customerReservationService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
