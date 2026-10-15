package com.hotelcaciquebolivar.reservas.controller.resena;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.resena.PublicResenasResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaRequest;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.resena.ResenaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de reseñas del hotel.
 * Permite crear, listar, buscar, actualizar y eliminar reseñas.
 * Incluye endpoint público para reseñas y filtrado por calificación.
 */
@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    /**
     * Crea una nueva reseña en el sistema.
     *
     * @param authentication objeto de autenticación del usuario actual (puede ser null)
     * @param dto            datos de la reseña a crear, validados con @Valid
     * @return ResponseEntity con la reseña creada y código 201 Created,
     *         incluyendo la URI del recurso creado en el header Location
     */
    @PostMapping
    public ResponseEntity<ResenaResponse> crearResena(
            Authentication authentication,
            @Valid @RequestBody ResenaRequest dto) {
        String email = authentication != null ? authentication.getName() : null;
        ResenaResponse nueva = resenaService.crear(dto, email);
        return ResponseEntity.created(
                URI.create("/api/resenas/" + nueva.id())
        ).body(nueva);
    }

    /**
     * Lista las reseñas públicas del hotel (endpoint público sin autenticación).
     *
     * @return respuesta con reseñas públicas y estadísticas
     */
    @GetMapping("/public")
    public ResponseEntity<PublicResenasResponse> listarPublicas() {
        return ResponseEntity.ok(resenaService.listarPublicas());
    }

    /**
     * Lista todas las reseñas con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 10)
     * @return página de reseñas
     */
    @GetMapping
    public ResponseEntity<PageResponse<ResenaResponse>> listarResenas(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(resenaService.listarTodos(pageable));
    }

    /**
     * Obtiene una reseña específica por su identificador.
     *
     * @param id identificador de la reseña
     * @return la reseña encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResenaResponse> obtenerResena(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.buscarPorId(id));
    }

    /**
     * Lista reseñas por calificación específica con paginación.
     *
     * @param calificación valor de calificación a filtrar (1-5)
     * @param pageable     parámetros de paginación (tamaño por defecto: 10)
     * @return página de reseñas con la calificación especificada
     */
    @GetMapping("/calificacion/{calificacion}")
    public ResponseEntity<PageResponse<ResenaResponse>> listarPorCalificacion(
            @PathVariable Integer calificacion,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(resenaService.listarPorCalificacion(calificacion, pageable));
    }

    /**
     * Actualiza parcialmente una reseña existente.
     *
     * @param id  identificador de la reseña a actualizar
     * @param dto datos de actualización parcial, validados con @Valid
     * @return la reseña actualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ResenaResponse> actualizarResena(
            @PathVariable Long id,
            @Valid @RequestBody ResenaUpdateRequest dto) {
        return ResponseEntity.ok(resenaService.actualizar(id, dto));
    }

    /**
     * Elimina una reseña del sistema.
     *
     * @param id identificador de la reseña a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
