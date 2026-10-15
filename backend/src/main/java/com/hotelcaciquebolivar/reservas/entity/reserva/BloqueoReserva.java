package com.hotelcaciquebolivar.reservas.entity.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un bloqueo de reserva para una habitación.
 * Los bloqueos impiden que se realicen reservas en un periodo específico,
 * ya sea por mantenimiento, eventos especiales u otras razones.
 *
 * <p>Tabla: {@code bloqueos_reserva}</p>
 */
@Entity
@Table(name = "bloqueos_reserva")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BloqueoReserva {

    /** Identificador único del bloqueo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Fecha de inicio del bloqueo. Puede ser nula para bloqueos totales. */
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    /** Fecha de fin del bloqueo. Puede ser nula para bloqueos totales. */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    /** Motivo del bloqueo. Máximo 255 caracteres. */
    @Column(length = 255)
    private String motivo;

    /** Indica si el bloqueo está activo. Por defecto {@code true}. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Desactiva el bloqueo. */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Verifica si el bloqueo es total (sin fechas específicas).
     *
     * @return {@code true} si ambas fechas son nulas
     */
    public boolean esBloqueoTotal() {
        return fechaInicio == null && fechaFin == null;
    }

    /**
     * Verifica si el bloqueo intersecta con un rango de fechas dado.
     * Un bloqueo total siempre intersecta con cualquier rango.
     *
     * @param desde fecha de inicio del rango a evaluar
     * @param hasta fecha de fin del rango a evaluar
     * @return {@code true} si hay intersección
     */
    public boolean intersectaCon(LocalDate desde, LocalDate hasta) {
        if (esBloqueoTotal()) {
            return true;
        }
        if (desde == null || hasta == null) {
            return false;
        }
        return !desde.isAfter(fechaFin) && !hasta.isBefore(fechaInicio);
    }
}