package com.hotelcaciquebolivar.reservas.service.notificacion;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.hotelcaciquebolivar.reservas.dto.notificacion.CantidadNoLeidasResponse;
import com.hotelcaciquebolivar.reservas.dto.notificacion.NotificacionResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.notificacion.Notificacion;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.repository.notificacion.NotificacionRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void notificarATodos(TipoNotificacion tipo, String mensaje,
            String origen, String origenNombre,
            Map<String, Object> detalles,
            String referenciaTipo, Long referenciaId,
            List<Rol> roles, String emailExcluido) {
        Long usuarioExcluidoId = null;
        if (emailExcluido != null) {
            usuarioExcluidoId = userRepository.findByEmail(emailExcluido)
                    .map(User::getId)
                    .orElse(null);
        }
        Long excludeId = usuarioExcluidoId;
        List<User> usuarios = userRepository.findByRolesInAndActivoTrue(roles);

        String detallesJson = null;
        if (detalles != null) {
            try {
                detallesJson = objectMapper.writeValueAsString(detalles);
            } catch (Exception e) {
                log.warn("Error serializando detalles de notificación: {}", e.getMessage());
            }
        }

        for (User usuario : usuarios) {
            if (excludeId != null && usuario.getId().equals(excludeId)) {
                continue;
            }
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(usuario);
            notificacion.setTipo(tipo);
            notificacion.setMensaje(mensaje);
            notificacion.setOrigen(origen);
            notificacion.setOrigenNombre(origenNombre);
            notificacion.setDetallesJson(detallesJson);
            notificacion.setReferenciaTipo(referenciaTipo);
            notificacion.setReferenciaId(referenciaId);
            notificacion.setLeida(false);
            notificacion.setFechaCreacion(LocalDateTime.now());
            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return notificacionRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CantidadNoLeidasResponse contarNoLeidas(Long usuarioId) {
        long cantidad = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
        return new CantidadNoLeidasResponse(cantidad);
    }

    @Override
    public void marcarLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", notificacionId));
        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("La notificación no pertenece al usuario");
        }
        notificacion.setLeida(true);
        notificacion.setFechaLectura(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }

    @Override
    public void marcarTodasLeidas(Long usuarioId) {
        Page<Notificacion> page = notificacionRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, Pageable.unpaged());
        for (Notificacion n : page.getContent()) {
            if (!n.isLeida()) {
                n.setLeida(true);
                n.setFechaLectura(LocalDateTime.now());
            }
        }
        notificacionRepository.saveAll(page.getContent());
    }

    @SuppressWarnings("unchecked")
    private NotificacionResponse toResponse(Notificacion n) {
        Map<String, Object> detalles = null;
        if (n.getDetallesJson() != null) {
            try {
                detalles = objectMapper.readValue(n.getDetallesJson(), Map.class);
            } catch (Exception e) {
                log.warn("Error deserializando detalles de notificación: {}", e.getMessage());
            }
        }
        return new NotificacionResponse(
                n.getId(),
                n.getUsuario().getId(),
                n.getTipo(),
                n.getMensaje(),
                n.getOrigen(),
                n.getOrigenNombre(),
                detalles != null ? detalles : Collections.emptyMap(),
                n.getReferenciaId(),
                n.getReferenciaTipo(),
                n.isLeida(),
                n.getFechaCreacion(),
                n.getFechaLectura());
    }
}
