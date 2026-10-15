package com.hotelcaciquebolivar.reservas.entity.habitacion;

import java.util.List;

import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa una habitación del hotel. Cada habitación tiene un
 * número único, un tipo asociado, un estado actual y una lista de detalles de
 * reserva que registran su historial de ocupación.
 *
 * <p>
 * Tabla: {@code habitaciones}</p>
 */
@Entity
@Table(name = "habitaciones")
@BatchSize(size = 20)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {

    /**
     * Identificador único de la habitación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Número de la habitación. Único en el sistema (ejemplo: 101, 202A).
     */
    @Column(nullable = false, unique = true)
    private String numero;

    /**
     * Tipo de habitación asociado. Relación many-to-one con
     * {@link TipoHabitacion}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoHabitacion tipo;

    /**
     * Lista de detalles de reserva asociados a esta habitación. Relación
     * uno-a-muchos con cascade.
     */
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReserva> detalles;

    /**
     * Estado actual de la habitación según el enumerador
     * {@link EstadoHabitacion}.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoHabitacion estado;

    /**
     * Descripción o notas adicionales de la habitación. Máximo 255 caracteres.
     */
    @Column(length = 255)
    private String descripcion;

    // Lombok generará constructores, getters y setters
}
