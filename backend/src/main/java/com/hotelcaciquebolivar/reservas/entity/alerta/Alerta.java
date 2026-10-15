package com.hotelcaciquebolivar.reservas.entity.alerta;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una alerta asociada a un cliente del hotel.
 * Las alertas se utilizan para registrar situaciones relevantes como
 * problemas económicos, hurtos, comportamiento conflictivo, fumadores
 * o daños a instalaciones.
 *
 * <p>Tabla: {@code alertas}</p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alerta {

    /** Identificador único de la alerta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente al que pertenece la alerta. Relación many-to-one con la entidad Cliente. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Tipo de alerta según la clasificación del enumerador {@link TipoAlerta}. */
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;

    /** Mensaje descriptivo de la alerta. Máximo 255 caracteres. */
    @Column(nullable = false, length = 255)
    private String mensaje;

    /** Estado de la alerta: {@code true} si está activa, {@code false} si fue atendida o desactivada. */
    @Column(nullable = false)
    private Boolean estado;

    /** Fecha y hora de creación de la alerta. Se asigna automáticamente al persistir. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Callback JPA que establece la fecha de creación antes de persistir la entidad. */
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // getters / setters
}
