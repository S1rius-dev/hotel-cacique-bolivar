package com.hotelcaciquebolivar.reservas.service.audit;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.audit.AuditLogResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;

/**
 * Interfaz que define las operaciones de registro de auditoría del sistema hotelero.
 * Proporciona funcionalidades para registrar acciones sobre entidades y consultar
 * el historial de auditoría de forma paginada, filtrable por entidad y/o ID de entidad.
 */
public interface AuditService {

    /**
     * Registra una entrada de auditoría para una acción realizada sobre una entidad específica.
     * Utiliza el email del usuario autenticado actualmente.
     *
     * @param action acción realizada sobre la entidad
     * @param entity nombre de la entidad afectada
     * @param entityId identificador único de la entidad afectada
     * @param detalle descripción detallada de la acción realizada
     */
    void log(AuditAction action, String entity, Long entityId, String detalle);

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
    void log(AuditAction action, String entity, Long entityId, String detalle, String emailOverride);

    /**
     * Lista todas las entradas de auditoría del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene el registro de auditoría
     */
    PageResponse<AuditLogResponse> listar(Pageable pageable);

    /**
     * Lista las entradas de auditoría filtradas por tipo de entidad de forma paginada.
     *
     * @param entity nombre de la entidad para filtrar las entradas de auditoría
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las entradas de auditoría de la entidad especificada
     */
    PageResponse<AuditLogResponse> listarPorEntidad(String entity, Pageable pageable);

    /**
     * Lista las entradas de auditoría filtradas por tipo de entidad e ID específico de forma paginada.
     *
     * @param entity nombre de la entidad para filtrar las entradas de auditoría
     * @param entityId identificador único de la entidad específica
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las entradas de auditoría de la entidad y ID especificados
     */
    PageResponse<AuditLogResponse> listarPorEntidadYId(String entity, Long entityId, Pageable pageable);
}
