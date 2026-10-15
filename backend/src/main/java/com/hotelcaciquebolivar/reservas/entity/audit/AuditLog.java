package com.hotelcaciquebolivar.reservas.entity.audit;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que almacena registros de auditoría del sistema.
 * Registra cada acción relevante realizada sobre las entidades del dominio,
 * permitiendo el seguimiento y trazabilidad de operaciones críticas.
 *
 * <p>Tabla: {@code audit_logs}</p>
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    /** Identificador único del registro de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de acción realizada, según el enumerador {@link AuditAction}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    /** Nombre de la entidad sobre la que se realizó la acción. */
    @Column(nullable = false, length = 100)
    private String entity;

    /** Identificador de la entidad afectada por la acción. */
    @Column(nullable = false)
    private Long entityId;

    /** Correo electrónico del usuario que realizó la acción. Puede ser nulo si no hay sesión activa. */
    @Column(length = 120)
    private String usuarioEmail;

    /** Nombre de usuario del usuario que realizó la acción. Puede ser nulo. */
    @Column(length = 100)
    private String usuarioUsername;

    /** Detalle adicional de la acción realizada en formato texto libre. */
    @Column(columnDefinition = "TEXT")
    private String detalle;

    /** Fecha y hora en que se registró la acción. Se asigna automáticamente al persistir. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /** Callback JPA que establece la fecha del registro antes de persistir. */
    @PrePersist
    void prePersist() {
        this.fecha = LocalDateTime.now();
    }
}
