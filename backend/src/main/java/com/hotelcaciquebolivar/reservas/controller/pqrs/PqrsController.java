package com.hotelcaciquebolivar.reservas.controller.pqrs;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsResponse;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsStateChangeRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.pqrs.EstadosPqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;
import com.hotelcaciquebolivar.reservas.service.pqrs.PqrsService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de PQRS (Peticiones, Quejas, Reclamos y Sugerencias).
 * Permite crear, consultar, listar, actualizar, cambiar estado y eliminar PQRS.
 * Soporta filtrado por usuario, estado y tipo de PQRS.
 */
@RestController
@RequestMapping("/api/pqrs")
@RequiredArgsConstructor
public class PqrsController {

    private final PqrsService pqrsService;

    /**
     * Crea una nueva PQRS en el sistema.
     *
     * @param authentication objeto de autenticación del usuario actual (puede ser null)
     * @param dto            datos de la PQRS a crear, validados con @Valid
     * @return ResponseEntity con la PQRS creada, código 201 Created
     */
    @Operation(operationId = "crearPqrs")
    @PostMapping
    public ResponseEntity<PqrsResponse> crear(
            Authentication authentication,
            @Valid @RequestBody PqrsRequest dto) {
        String email = authentication != null ? authentication.getName() : null;
        PqrsResponse response = pqrsService.crear(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca una PQRS específica por su identificador.
     *
     * @param id identificador de la PQRS
     * @return la PQRS encontrada
     */
    @Operation(operationId = "buscarPqrsPorId")
    @GetMapping("/{id}")
    public ResponseEntity<PqrsResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pqrsService.buscarPorId(id));
    }

    /**
     * Busca una PQRS por su código único de seguimiento.
     *
     * @param codigo código único de la PQRS
     * @return la PQRS encontrada
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PqrsResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(pqrsService.buscarPorCodigo(codigo));
    }

    /**
     * Lista todas las PQRS con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 15, ordenado por fecha de creación descendente)
     * @return página de PQRS
     */
    @GetMapping
    public ResponseEntity<PageResponse<PqrsResponse>> listarTodos(
            @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pqrsService.listarTodos(pageable));
    }

    /**
     * Lista las PQRS de un usuario específico con paginación.
     *
     * @param usuarioId identificador del usuario
     * @param pageable  parámetros de paginación (tamaño por defecto: 15, ordenado por fecha de creación descendente)
     * @return página de PQRS del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PageResponse<PqrsResponse>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pqrsService.listarPorUsuario(usuarioId, pageable));
    }

    /**
     * Lista las PQRS por estado específico con paginación.
     *
     * @param estado   estado de las PQRS a filtrar
     * @param pageable parámetros de paginación (tamaño por defecto: 15, ordenado por fecha de creación descendente)
     * @return página de PQRS con el estado especificado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<PageResponse<PqrsResponse>> listarPorEstado(
            @PathVariable EstadosPqrs estado,
            @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pqrsService.listarPorEstado(estado, pageable));
    }

    /**
     * Lista las PQRS por tipo específico con paginación.
     *
     * @param tipo     tipo de PQRS a filtrar (PETICION, QUEJA, RECLAMO, SUGERENCIA)
     * @param pageable parámetros de paginación (tamaño por defecto: 15, ordenado por fecha de creación descendente)
     * @return página de PQRS del tipo especificado
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<PageResponse<PqrsResponse>> listarPorTipo(
            @PathVariable TipoPqrs tipo,
            @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pqrsService.listarPorTipo(tipo, pageable));
    }

    /**
     * Cambia el estado de una PQRS existente.
     *
     * @param id  identificador de la PQRS
     * @param dto datos del cambio de estado, validados con @Valid
     * @return la PQRS con el estado actualizado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PqrsResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody PqrsStateChangeRequest dto) {
        return ResponseEntity.ok(pqrsService.cambiarEstado(id, dto));
    }

    /**
     * Actualiza los datos de una PQRS existente.
     *
     * @param id  identificador de la PQRS a actualizar
     * @param dto datos actualizados de la PQRS, validados con @Valid
     * @return la PQRS actualizada
     */
    @Operation(operationId = "actualizarPqrs")
    @PutMapping("/{id}")
    public ResponseEntity<PqrsResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PqrsUpdateRequest dto) {
        return ResponseEntity.ok(pqrsService.actualizar(id, dto));
    }

    /**
     * Elimina una PQRS del sistema.
     *
     * @param id identificador de la PQRS a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarPqrs")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pqrsService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
