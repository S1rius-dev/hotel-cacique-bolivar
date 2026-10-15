package com.hotelcaciquebolivar.reservas.controller.user;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.user.UserRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.dto.user.UserUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de usuarios del sistema.
 * Proporciona operaciones CRUD completas para el alta, consulta,
 * actualización y baja de usuarios del sistema.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param dto datos del usuario a crear, validados con @Valid
     * @return ResponseEntity con el usuario creado y código 201 Created,
     *         incluyendo la URI del recurso creado en el header Location
     */
    @PostMapping
    public ResponseEntity<UserResponse> crearUsuario(@Valid @RequestBody UserRequest dto) {
        UserResponse creado = userService.crearUsuario(dto);
        return ResponseEntity.created(
                URI.create("/api/users/" + creado.id())
        ).body(creado);
    }

    /**
     * Lista todos los usuarios con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 14, ordenado por id descendente)
     * @return página de usuarios
     */
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> listarUsuarios(
            @PageableDefault(size = 14, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.listarUsuarios(pageable));
    }

    /**
     * Obtiene un usuario específico por su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPorId(id));
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id  identificador del usuario a actualizar
     * @param dto datos actualizados del usuario, validados con @Valid
     * @return el usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest dto) {
        UserResponse actualizado = userService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param id identificador del usuario a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        userService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

}
