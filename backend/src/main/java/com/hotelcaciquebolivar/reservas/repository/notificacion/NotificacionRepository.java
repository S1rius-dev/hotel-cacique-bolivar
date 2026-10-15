package com.hotelcaciquebolivar.reservas.repository.notificacion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.notificacion.Notificacion;

/**
 * Repositorio para la gestión de notificaciones de usuarios.
 * Permite consultar notificaciones por usuario y contar las no leídas.
 */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Obtiene las notificaciones de un usuario específico, ordenadas por fecha de creación descendente.
     *
     * @param usuarioId identificador del usuario
     * @param pageable paginación de resultados
     * @return página de notificaciones del usuario
     */
    Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    /**
     * Cuenta el número de notificaciones no leídas de un usuario específico.
     *
     * @param usuarioId identificador del usuario
     * @return cantidad de notificaciones no leídas
     */
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}
