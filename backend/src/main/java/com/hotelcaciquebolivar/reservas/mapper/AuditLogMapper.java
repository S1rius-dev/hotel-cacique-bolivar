package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;

import com.hotelcaciquebolivar.reservas.dto.audit.AuditLogResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditLog;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link AuditLog} y su DTO de respuesta {@link AuditLogResponse}.
 *
 * <p>Este mapper realiza un mapeo directo de todos los campos de la entidad de auditoria
 * al DTO de respuesta, sin ignorar ni transformar ningun campo.</p>
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Convierte una entidad {@link AuditLog} en un DTO de respuesta {@link AuditLogResponse}.
     *
     * @param auditLog la entidad de registro de auditoria a convertir.
     * @return el DTO de respuesta con los campos mapeados directamente.
     */
    AuditLogResponse toResponse(AuditLog auditLog);
}
