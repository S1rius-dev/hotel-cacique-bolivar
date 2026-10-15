package com.hotelcaciquebolivar.reservas.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.notificacion.CantidadNoLeidasResponse;
import com.hotelcaciquebolivar.reservas.dto.notificacion.NotificacionResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de notificaciones administrativas.
 * Proporciona endpoints para listar, contar no leídas y marcar notificaciones como leídas
 * para el usuario autenticado.
 */
@RestController
@RequestMapping("/api/admin/notificaciones")
@RequiredArgsConstructor
public class NotificacionAdminController {

    private final NotificacionService notificacionService;
    private final UserRepository userRepository;

    /**
     * Lista todas las notificaciones del usuario autenticado con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 20)
     * @param auth     objeto de autenticación del usuario actual
     * @return página de notificaciones del usuario
     */
    @Operation(operationId = "listarNotificaciones")
    @GetMapping
    public ResponseEntity<Page<NotificacionResponse>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication auth) {
        User usuario = obtenerUsuario(auth);
        return ResponseEntity.ok(
                notificacionService.listarPorUsuario(usuario.getId(), pageable));
    }

    /**
     * Cuenta la cantidad de notificaciones no leídas del usuario autenticado.
     *
     * @param auth objeto de autenticación del usuario actual
     * @return cantidad de notificaciones no leídas
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<CantidadNoLeidasResponse> contarNoLeidas(Authentication auth) {
        User usuario = obtenerUsuario(auth);
        return ResponseEntity.ok(notificacionService.contarNoLeidas(usuario.getId()));
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param id   identificador de la notificación a marcar como leída
     * @param auth objeto de autenticación del usuario actual
     * @return respuesta vacía con código 200 OK
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(
            @PathVariable Long id,
            Authentication auth) {
        User usuario = obtenerUsuario(auth);
        notificacionService.marcarLeida(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     *
     * @param auth objeto de autenticación del usuario actual
     * @return respuesta vacía con código 200 OK
     */
    @PutMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas(Authentication auth) {
        User usuario = obtenerUsuario(auth);
        notificacionService.marcarTodasLeidas(usuario.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Método auxiliar para obtener el usuario actual a partir de la autenticación.
     *
     * @param auth objeto de autenticación del usuario actual
     * @return entidad de usuario correspondiente al email del autenticado
     * @throws ResourceNotFoundException si no se encuentra usuario con el email proporcionado
     */
    private User obtenerUsuario(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", auth.getName()));
    }
}
