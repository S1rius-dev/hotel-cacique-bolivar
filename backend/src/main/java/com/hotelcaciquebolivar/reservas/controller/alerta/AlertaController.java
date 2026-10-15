package com.hotelcaciquebolivar.reservas.controller.alerta;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaRequest;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaResponse;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.alerta.AlertaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de alertas del sistema.
 * Proporciona operaciones CRUD completas para alertas, incluyendo
 * creación, lectura, actualización y eliminación.
 */
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    /**
     * Crea una nueva alerta en el sistema.
     *
     * @param alertaDTO datos de la alerta a crear, validados con @Valid
     * @return ResponseEntity con la alerta creada y código 201 Created,
     *         incluyendo la URI del recurso creado en el header Location
     */
    @PostMapping
    public ResponseEntity<AlertaResponse> crearAlerta(
            @Valid @RequestBody AlertaRequest alertaDTO) {
        AlertaResponse nueva = alertaService.crear(alertaDTO);
        return ResponseEntity.created(
                URI.create("/api/alertas/" + nueva.id())
        ).body(nueva);
    }

    /**
     * Lista todas las alertas del sistema con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 14)
     * @return página de alertas
     */
    @GetMapping
    public ResponseEntity<PageResponse<AlertaResponse>> listarAlertas(
            @PageableDefault(size = 14) Pageable pageable) {
        return ResponseEntity.ok(alertaService.listarTodos(pageable));
    }

    /**
     * Obtiene una alerta específica por su identificador.
     *
     * @param id identificador de la alerta
     * @return la alerta encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> obtenerAlerta(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.buscarPorId(id));
    }

    /**
     * Lista todas las alertas asociadas a un cliente específico con paginación.
     *
     * @param clienteId identificador del cliente
     * @param pageable  parámetros de paginación (tamaño por defecto: 14)
     * @return página de alertas del cliente especificado
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<PageResponse<AlertaResponse>> listarAlertasPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 14) Pageable pageable) {
        return ResponseEntity.ok(alertaService.listarPorCliente(clienteId, pageable));
    }

    /**
     * Actualiza parcialmente una alerta existente.
     *
     * @param id  identificador de la alerta a actualizar
     * @param dto datos de actualización parcial, validados con @Valid
     * @return la alerta actualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AlertaResponse> actualizarAlerta(
            @PathVariable Long id,
            @Valid @RequestBody AlertaUpdateRequest dto) {
        return ResponseEntity.ok(alertaService.actualizar(id, dto));
    }

    /**
     * Elimina una alerta del sistema.
     *
     * @param id identificador de la alerta a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlerta(@PathVariable Long id) {
        alertaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}