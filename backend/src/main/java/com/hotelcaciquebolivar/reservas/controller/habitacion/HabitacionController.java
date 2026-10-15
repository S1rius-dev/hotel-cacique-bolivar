package com.hotelcaciquebolivar.reservas.controller.habitacion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.LimpiarHabitacionesRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.LimpiarHabitacionesResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.HabitacionEstadoDiario;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.habitacion.HabitacionService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de habitaciones del hotel.
 * Proporciona operaciones CRUD completas, consulta del estado diario
 * de habitaciones y gestión de limpieza de habitaciones.
 */
@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    /**
     * Crea una nueva habitación en el sistema.
     *
     * @param dto datos de la habitación a crear, validados con @Valid
     * @return ResponseEntity con la habitación creada y código 201 Created
     */
    @Operation(operationId = "crearHabitacion")
    @PostMapping
    public ResponseEntity<HabitacionResponse> crear(
            @Valid @RequestBody HabitacionRequest dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(habitacionService.crear(dto));
    }

    /**
     * Lista todas las habitaciones con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 15)
     * @return página de habitaciones
     */
    @Operation(operationId = "listarHabitaciones")
    @GetMapping
    public ResponseEntity<PageResponse<HabitacionResponse>> listar(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(habitacionService.listarTodos(pageable));
    }

    /**
     * Obtiene una habitación específica por su identificador.
     *
     * @param id identificador de la habitación
     * @return la habitación encontrada
     */
    @Operation(operationId = "obtenerHabitacion")
    @GetMapping("/{id}")
    public ResponseEntity<HabitacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.buscarPorId(id));
    }

    /**
     * Actualiza los datos de una habitación existente.
     *
     * @param id  identificador de la habitación a actualizar
     * @param dto datos actualizados de la habitación, validados con @Valid
     * @return la habitación actualizada
     */
    @Operation(operationId = "actualizarHabitacion")
    @PutMapping("/{id}")
    public ResponseEntity<HabitacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody HabitacionUpdateRequest dto
    ) {
        return ResponseEntity.ok(habitacionService.actualizar(id, dto));
    }

    /**
     * Elimina una habitación del sistema.
     *
     * @param id identificador de la habitación a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarHabitacion")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        habitacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el estado diario de todas las habitaciones para una fecha específica.
     *
     * @param fecha fecha para la cual se consulta el estado (formato ISO: YYYY-MM-DD)
     * @return lista de habitaciones con su estado para la fecha indicada
     */
    @Operation(operationId = "estadoDiarioHabitaciones")
    @GetMapping("/estado-diario")
    public ResponseEntity<List<HabitacionEstadoDiario>> estadoDiario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(habitacionService.obtenerEstadoDiario(fecha));
    }

    /**
     * Marca una habitación específica como limpia.
     *
     * @param id identificador de la habitación a marcar como limpia
     * @return la habitación actualizada
     */
    @Operation(operationId = "marcarHabitacionComoLimpia")
    @PutMapping("/{id}/limpiar")
    public ResponseEntity<HabitacionResponse> marcarComoLimpia(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.marcarComoLimpia(id));
    }

    /**
     * Marca múltiples habitaciones como limpias en una sola operación.
     *
     * @param dto solicitud que contiene la lista de identificadores de habitaciones a limpiar
     * @return respuesta con el resultado de la operación de limpieza
     */
    @Operation(operationId = "limpiarHabitaciones")
    @PostMapping("/limpiar")
    public ResponseEntity<LimpiarHabitacionesResponse> limpiar(
            @Valid @RequestBody LimpiarHabitacionesRequest dto) {
        return ResponseEntity.ok(habitacionService.limpiar(dto.habitacionesIds()));
    }
}
