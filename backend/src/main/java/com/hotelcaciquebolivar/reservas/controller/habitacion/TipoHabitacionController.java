package com.hotelcaciquebolivar.reservas.controller.habitacion;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.TipoHabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.tipo_habitacion.TipoHabitacionService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de tipos de habitación del hotel.
 * Proporciona operaciones CRUD completas para definir y administrar
 * las categorías de habitaciones (ej: individual, doble, suite).
 */
@RestController
@RequestMapping("/api/tipos-habitacion")
@RequiredArgsConstructor
public class TipoHabitacionController {

    private final TipoHabitacionService service;

    /**
     * Crea un nuevo tipo de habitación en el sistema.
     *
     * @param dto datos del tipo de habitación a crear, validados con @Valid
     * @return ResponseEntity con el tipo de habitación creado y código 201 Created
     */
    @Operation(operationId = "crearTipoHabitacion")
    @PostMapping
    public ResponseEntity<TipoHabitacionResponse> crear(
            @Valid @RequestBody TipoHabitacionRequest dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(dto));
    }

    /**
     * Lista todos los tipos de habitación con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 14)
     * @return página de tipos de habitación
     */
    @Operation(operationId = "listarTiposHabitacion")
    @GetMapping
    public ResponseEntity<PageResponse<TipoHabitacionResponse>> listar(
            @PageableDefault(size = 14) Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    /**
     * Busca un tipo de habitación específico por su identificador.
     *
     * @param id identificador del tipo de habitación
     * @return el tipo de habitación encontrado
     */
    @Operation(operationId = "buscarTipoHabitacion")
    @GetMapping("/{id}")
    public ResponseEntity<TipoHabitacionResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * Actualiza un tipo de habitación existente.
     *
     * @param id  identificador del tipo de habitación a actualizar
     * @param dto datos actualizados del tipo de habitación, validados con @Valid
     * @return el tipo de habitación actualizado
     */
    @Operation(operationId = "actualizarTipoHabitacion")
    @PutMapping("/{id}")
    public ResponseEntity<TipoHabitacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoHabitacionUpdateRequest dto
    ) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    /**
     * Elimina un tipo de habitación del sistema.
     *
     * @param id identificador del tipo de habitación a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarTipoHabitacion")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
