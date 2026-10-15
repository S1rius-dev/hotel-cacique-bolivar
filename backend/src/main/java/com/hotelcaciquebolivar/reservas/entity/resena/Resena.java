package com.hotelcaciquebolivar.reservas.entity.resena;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una reseña o evaluación dejada por un cliente
 * sobre su experiencia en el hotel. Incluye una calificación numérica
 * y un comentario de texto.
 *
 * <p>Tabla: {@code resenas}</p>
 */
@Entity
@Table(name = "resenas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resena {

    /** Identificador único de la reseña. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Nombre del autor de la reseña. Máximo 100 caracteres. */
    @Column(name = "name", length = 100)
    private String name;

    /** Ubicación del autor de la reseña. Máximo 255 caracteres. */
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    /** URL de la foto de perfil del autor. Máximo 500 caracteres. */
    @Column(name = "picture_url", length = 500)
    private String pictureUrl;

    /** Comentario o texto de la reseña. Máximo 1000 caracteres. */
    @Column(length = 1000, nullable = false)
    private String comentario;

    /** Calificación numérica de la reseña. Mínimo 1, máximo 5. */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer calificacion;

    /** Cliente que dejó la reseña. Relación many-to-one. Puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Fecha y hora de creación de la reseña. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
