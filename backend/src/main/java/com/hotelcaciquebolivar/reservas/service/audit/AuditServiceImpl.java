package com.hotelcaciquebolivar.reservas.service.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.audit.AuditLogResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditLog;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.mapper.AuditLogMapper;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.audit.AuditLogRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de auditoría del sistema hotelero.
 * Registra acciones realizadas sobre entidades del sistema, incluyendo
 * información del usuario que realizó la acción, la entidad afectada,
 * el ID de la entidad y un detalle de la acción.
 * 
 * <p>Esta clase maneja la creación de registros de auditoría con
 * propagación transaccional independiente (REQUIRES_NEW) para garantizar
 * que los logs se persistan incluso si la transacción principal falla.</p>
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final AuditLogMapper auditLogMapper;

    /**
     * Registra una entrada de auditoría para una acción realizada sobre una entidad específica.
     * Utiliza el email del usuario autenticado actualmente.
     *
     * @param action acción realizada sobre la entidad
     * @param entity nombre de la entidad afectada
     * @param entityId identificador único de la entidad afectada
     * @param detalle descripción detallada de la acción realizada
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entity, Long entityId, String detalle) {
        log(action, entity, entityId, detalle, null);
    }

    /**
     * Registra una entrada de auditoría para una acción realizada sobre una entidad específica.
     * Permite especificar un email de usuario diferente al autenticado actualmente.
     *
     * @param action acción realizada sobre la entidad
     * @param entity nombre de la entidad afectada
     * @param entityId identificador único de la entidad afectada
     * @param detalle descripción detallada de la acción realizada
     * @param emailOverride email del usuario que realizó la acción (sobreescribe el usuario actual)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entity, Long entityId, String detalle, String emailOverride) {
        String usuarioEmail = emailOverride != null && !emailOverride.isBlank()
                ? emailOverride
                : obtenerEmailActual();
        String usuarioUsername = obtenerUsernameActual(usuarioEmail);

        AuditLog log = AuditLog.builder()
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .usuarioEmail(usuarioEmail)
                .usuarioUsername(usuarioUsername)
                .detalle(detalle)
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Lista todas las entradas de auditoría del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene el registro de auditoría
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listar(Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
        return pageMapper.toPageResponse(page);
    }

    /**
     * Lista las entradas de auditoría filtradas por tipo de entidad de forma paginada.
     *
     * @param entity nombre de la entidad para filtrar las entradas de auditoría
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las entradas de auditoría de la entidad especificada
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listarPorEntidad(String entity, Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository
                .findByEntityOrderByFechaDesc(entity, pageable).map(auditLogMapper::toResponse);
        return pageMapper.toPageResponse(page);
    }

    /**
     * Lista las entradas de auditoría filtradas por tipo de entidad e ID específico de forma paginada.
     *
     * @param entity nombre de la entidad para filtrar las entradas de auditoría
     * @param entityId identificador único de la entidad específica
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las entradas de auditoría de la entidad y ID especificados
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listarPorEntidadYId(String entity, Long entityId, Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository
                .findByEntityAndEntityIdOrderByFechaDesc(entity, entityId, pageable).map(auditLogMapper::toResponse);
        return pageMapper.toPageResponse(page);
    }

    /**
     * Obtiene el email del usuario autenticado actualmente del contexto de seguridad.
     * Si no hay usuario autenticado o es un usuario anónimo, retorna "SYSTEM".
     *
     * @return email del usuario actual o "SYSTEM" si no hay autenticación
     */
    private String obtenerEmailActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    /**
     * Obtiene el nombre de usuario (username) asociado al email proporcionado.
     * Maneja casos especiales como el sistema (SYSTEM) y actores virtuales
     * como WOMPI y PAGO_EN_LINEA.
     *
     * @param email email del usuario para buscar el username
     * @return el username asociado al email, o el email en mayúsculas si no se encuentra
     */
    private String obtenerUsernameActual(String email) {
        if (email == null || "SYSTEM".equals(email)) {
            return "SYSTEM";
        }
        // Actor virtual para pagos en línea: no existe en User, mantener literal
        if ("WOMPI".equalsIgnoreCase(email) || "PAGO_EN_LINEA".equalsIgnoreCase(email)) {
            return email.toUpperCase();
        }
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElse(email);
    }
}
