package com.hotelcaciquebolivar.reservas.controller.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.audit.AuditLogResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;

import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la consulta de registros de auditoría del sistema.
 * Permite listar logs de auditoría con filtros opcionales por tipo de entidad
 * y identificador de entidad.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    /**
     * Lista registros de auditoría con paginación y filtros opcionales.
     * Si se proporcionan entity y entityId, filtra por entidad específica e ID.
     * Si solo se proporciona entity, filtra por tipo de entidad.
     * Si no se proporcionan filtros, lista todos los registros.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 20, ordenado por fecha descendente)
     * @param entity   nombre de la entidad para filtrar (opcional)
     * @param entityId identificador de la entidad para filtrar (opcional, requiere entity)
     * @return página de registros de auditoría
     */
    @Operation(operationId = "listarAuditLogs")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> listar(
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) Long entityId) {

        PageResponse<AuditLogResponse> result;

        if (entity != null && entityId != null) {
            result = auditService.listarPorEntidadYId(entity, entityId, pageable);
        } else if (entity != null) {
            result = auditService.listarPorEntidad(entity, pageable);
        } else {
            result = auditService.listar(pageable);
        }

        return ResponseEntity.ok(result);
    }
}
