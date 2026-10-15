package com.hotelcaciquebolivar.reservas.controller.habitacion;

import java.util.List;

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

import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.PrecioTemporalResponse;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de precios temporales (temporadas) de habitaciones.
 * Permite crear, listar, buscar, actualizar y eliminar configuraciones de precios
 * por tipo de habitación y rango de fechas.
 */
@RestController
@RequestMapping("/api/precios-temporales")
@RequiredArgsConstructor
public class PrecioTemporalController {

    private final PrecioTemporalService service;

    /**
     * Crea un nuevo precio temporal en el sistema.
     *
     * @param dto datos del precio temporal a crear, validados con @Valid
     * @return ResponseEntity con el precio temporal creado y código 201 Created
     */
    @Operation(operationId = "crearPrecioTemporal")
    @PostMapping
    public ResponseEntity<PrecioTemporalResponse> crear(
            @Valid @RequestBody PrecioTemporalRequest dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(dto));
    }

    /**
     * Lista precios temporales, opcionalmente filtrados por tipo de habitación.
     *
     * @param tipoId identificador del tipo de habitación para filtrar (opcional)
     * @return lista de precios temporales que coinciden con el filtro
     */
    @Operation(operationId = "listarPreciosTemporales")
    @GetMapping
    public ResponseEntity<List<PrecioTemporalResponse>> listar(
            @RequestParam(required = false) Long tipoId) {
        return ResponseEntity.ok(service.listar(tipoId));
    }

    /**
     * Busca un precio temporal específico por su identificador.
     *
     * @param id identificador del precio temporal
     * @return el precio temporal encontrado
     */
    @Operation(operationId = "buscarPrecioTemporal")
    @GetMapping("/{id}")
    public ResponseEntity<PrecioTemporalResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * Actualiza un precio temporal existente.
     *
     * @param id  identificador del precio temporal a actualizar
     * @param dto datos actualizados del precio temporal, validados con @Valid
     * @return el precio temporal actualizado
     */
    @Operation(operationId = "actualizarPrecioTemporal")
    @PutMapping("/{id}")
    public ResponseEntity<PrecioTemporalResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PrecioTemporalRequest dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    /**
     * Elimina un precio temporal del sistema.
     *
     * @param id identificador del precio temporal a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarPrecioTemporal")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
