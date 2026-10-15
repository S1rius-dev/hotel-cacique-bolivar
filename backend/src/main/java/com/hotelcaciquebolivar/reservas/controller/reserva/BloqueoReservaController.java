package com.hotelcaciquebolivar.reservas.controller.reserva;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.BloqueoReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.WebReservationsStatusResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.WebReservationsToggleRequest;
import com.hotelcaciquebolivar.reservas.service.reserva.bloqueo.BloqueoReservaService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de bloqueos de reserva y estado de reservas web.
 * Permite crear, listar, buscar, actualizar y desactivar bloqueos de reserva.
 * También gestiona el estado de habilitación de reservas desde el sitio web.
 */
@RestController
@RequestMapping("/api/bloqueos")
public class BloqueoReservaController {

    private final BloqueoReservaService service;

    /**
     * Constructor que inyecta el servicio de bloqueos de reserva.
     *
     * @param service servicio de gestión de bloqueos de reserva
     */
    public BloqueoReservaController(BloqueoReservaService service) {
        this.service = service;
    }

    /**
     * Crea un nuevo bloqueo de reserva en el sistema.
     *
     * @param request datos del bloqueo a crear, validados con @Valid
     * @return ResponseEntity con el bloqueo creado y código 201 Created
     */
    @Operation(operationId = "crearBloqueo")
    @PostMapping
    public ResponseEntity<BloqueoReservaResponse> crear(@Valid @RequestBody BloqueoReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    /**
     * Lista todos los bloqueos de reserva activos.
     *
     * @return lista de bloqueos de reserva
     */
    @Operation(operationId = "listarBloqueos")
    @GetMapping
    public ResponseEntity<List<BloqueoReservaResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    /**
     * Busca un bloqueo de reserva específico por su identificador.
     *
     * @param id identificador del bloqueo
     * @return el bloqueo encontrado
     */
    @Operation(operationId = "buscarBloqueoPorId")
    @GetMapping("/{id}")
    public ResponseEntity<BloqueoReservaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * Actualiza un bloqueo de reserva existente.
     *
     * @param id      identificador del bloqueo a actualizar
     * @param request datos actualizados del bloqueo, validados con @Valid
     * @return el bloqueo actualizado
     */
    @Operation(operationId = "actualizarBloqueo")
    @PutMapping("/{id}")
    public ResponseEntity<BloqueoReservaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BloqueoReservaRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    /**
     * Desactiva un bloqueo de reserva existente.
     *
     * @param id identificador del bloqueo a desactivar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si existe un bloqueo de reserva para una fecha específica.
     *
     * @param fecha fecha a verificar (formato ISO: YYYY-MM-DD)
     * @return true si existe bloqueo para la fecha, false en caso contrario
     */
    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificarBloqueo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.existeBloqueoParaFecha(fecha));
    }

    /**
     * Obtiene el estado actual de habilitación de reservas web.
     *
     * @return estado de reservas web (habilitado/deshabilitado)
     */
    @GetMapping("/web-status")
    public ResponseEntity<WebReservationsStatusResponse> obtenerEstadoReservasWeb() {
        boolean enabled = service.obtenerEstadoReservasWeb();
        return ResponseEntity.ok(new WebReservationsStatusResponse(enabled));
    }

    /**
     * Cambia el estado de habilitación de reservas web.
     *
     * @param request datos con el nuevo estado de habilitación, validados con @Valid
     * @return el bloqueo de reserva actualizado con el nuevo estado
     */
    @PutMapping("/web-status")
    public ResponseEntity<BloqueoReservaResponse> toggleReservasWeb(
            @Valid @RequestBody WebReservationsToggleRequest request) {
        BloqueoReservaResponse response = service.toggleReservasWeb(request.enabled());
        return ResponseEntity.ok(response);
    }
}
