package com.hotelcaciquebolivar.reservas.entity.caja;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una caja registradora del hotel.
 * Cada caja tiene un nombre único y puede estar activa o inactiva.
 * Los turnos y movimientos de caja están asociados a una caja específica.
 *
 * <p>Tabla: {@code cajas}</p>
 */
@Entity
@Table(name = "cajas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    /** Identificador único de la caja. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre único de la caja. Máximo 50 caracteres. */
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    /** Indica si la caja está activa y disponible para operaciones. Por defecto {@code true}. */
    @Column(nullable = false)
    private Boolean activo = true;

    /** Fecha y hora de creación del registro. Se asigna automáticamente al persistir. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Callback JPA que establece la fecha de creación y el estado activo antes de persistir. */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
