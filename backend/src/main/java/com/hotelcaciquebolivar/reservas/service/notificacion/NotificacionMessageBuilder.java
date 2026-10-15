package com.hotelcaciquebolivar.reservas.service.notificacion;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Componente constructor de mensajes de notificación que identifica al actor
 * (usuario o sistema) que realiza una acción en el sistema hotelero.
 * Proporciona información contextual como nombre, prefijo con emoji,
 * origen (ADMIN, RECEPCION, WEB, SISTEMA) y nombre del empleado asociado.
 */
@Component
@RequiredArgsConstructor
public class NotificacionMessageBuilder {

    private final UserRepository userRepository;

    /**
     * Registro que contiene la información del actor que realizó una acción.
     *
     * @param email email del actor (null si es el sistema)
     * @param nombre nombre completo del actor
     * @param prefijo prefijo con emoji para mostrar en notificaciones
     * @param origen origen del actor (ADMIN, RECEPCION, WEB, SISTEMA)
     * @param origenNombre nombre del empleado asociado al actor (si aplica)
     */
    public record ActorInfo(
        String email,
        String nombre,
        String prefijo,
        String origen,
        String origenNombre
    ) {}

    /**
     * Obtiene la información del actor actualmente autenticado en el sistema.
     * Si no hay usuario autenticado, retorna un actor del sistema.
     *
     * @return información del actor con sus datos y contexto
     */
    public ActorInfo obtenerActor() {
        String email = obtenerEmailActual();
        if (email == null) {
            return new ActorInfo(null, "Sistema", "\u2699\ufe0f Sistema", "SISTEMA", null);
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ActorInfo(email, email, "\u2699\ufe0f Sistema", "SISTEMA", null);
        }
        String nombre = nombreActor(user);
        String prefijo = prefijoOrigen(user);
        String origen = origenLabel(user);
        String origenNombre = user.getEmpleado() != null
                ? user.getEmpleado().getNombres() + " " + user.getEmpleado().getApellidos()
                : null;
        return new ActorInfo(email, nombre, prefijo, origen, origenNombre);
    }

    /**
     * Obtiene un actor que representa al sistema (sin usuario autenticado).
     *
     * @return información del actor sistema
     */
    public ActorInfo obtenerActorSistema() {
        return new ActorInfo(null, "Sistema", "\u2699\ufe0f Sistema", "SISTEMA", null);
    }

    /**
     * Obtiene el nombre del actor basándose en su tipo de usuario.
     * Prioriza el nombre del empleado, luego el del cliente, y finalmente el username.
     *
     * @param user usuario del sistema
     * @return nombre del actor
     */
    private String nombreActor(User user) {
        if (user.getEmpleado() != null) {
            return user.getEmpleado().getNombres() + " " + user.getEmpleado().getApellidos();
        }
        if (user.getCliente() != null) {
            return user.getCliente().getNombreCompleto() + " " + user.getCliente().getApellidoCompleto();
        }
        return user.getUsername();
    }

    /**
     * Obtiene el prefijo con emoji del actor basándose en su rol.
     *
     * @param user usuario del sistema
     * @return prefijo con emoji para notificaciones
     */
    private String prefijoOrigen(User user) {
        if (user.tieneRol(Rol.ADMIN)) return "\ud83d\udee1\ufe0f Administrador";
        if (user.tieneRol(Rol.RECEPCIONISTA)) return "\ud83d\udc64 Recepcionista";
        if (user.tieneRol(Rol.CLIENTE)) return "\ud83c\udf10 Cliente";
        return "\u2699\ufe0f Sistema";
    }

    /**
     * Obtiene la etiqueta del origen del actor basándose en su rol.
     *
     * @param user usuario del sistema
     * @return etiqueta del origen (ADMIN, RECEPCION, WEB, SISTEMA)
     */
    private String origenLabel(User user) {
        if (user.tieneRol(Rol.ADMIN)) return "ADMIN";
        if (user.tieneRol(Rol.RECEPCIONISTA)) return "RECEPCION";
        if (user.tieneRol(Rol.CLIENTE)) return "WEB";
        return "SISTEMA";
    }

    /**
     * Obtiene el email del usuario autenticado actualmente del contexto de seguridad.
     *
     * @return email del usuario actual, o null si no hay autenticación
     */
    private String obtenerEmailActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }
}
