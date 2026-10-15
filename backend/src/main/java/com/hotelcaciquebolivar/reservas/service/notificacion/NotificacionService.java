package com.hotelcaciquebolivar.reservas.service.notificacion;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.notificacion.CantidadNoLeidasResponse;
import com.hotelcaciquebolivar.reservas.dto.notificacion.NotificacionResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;

public interface NotificacionService {

    void notificarATodos(TipoNotificacion tipo, String mensaje,
            String origen, String origenNombre,
            Map<String, Object> detalles,
            String referenciaTipo, Long referenciaId,
            List<Rol> roles, String emailExcluido);

    Page<NotificacionResponse> listarPorUsuario(Long usuarioId, Pageable pageable);

    CantidadNoLeidasResponse contarNoLeidas(Long usuarioId);

    void marcarLeida(Long notificacionId, Long usuarioId);

    void marcarTodasLeidas(Long usuarioId);
}
