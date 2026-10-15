package com.hotelcaciquebolivar.reservas.dto.audit;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;

/**
 * DTO de respuesta que representa un registro de auditoría del sistema.
 * Contiene información sobre la acción realizada, la entidad afectada y el usuario que ejecutó la acción.
 */
public record AuditLogResponse(
        Long id,
        AuditAction action,
        String entity,
        Long entityId,
        String usuarioEmail,
        String usuarioUsername,
        String detalle,
        LocalDateTime fecha) {

}
