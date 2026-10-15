package com.hotelcaciquebolivar.reservas.entity.habitacion;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que define los tipos de habitación disponibles en el hotel. Cada tipo
 * especifica capacidades, precios base y precios por persona extra. Las
 * habitaciones físicas se asocian a un tipo específico.
 *
 * <p>
 * Tabla: {@code tipos_habitacion}</p>
 */
@Entity
@Table(name = "tipos_habitacion")
@BatchSize(size = 20)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class TipoHabitacion {

    /**
     * Identificador único del tipo de habitación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nombre del tipo de habitación. Único en el sistema (ejemplo: Simple,
     * Doble, Suite, Familiar).
     */
    @Column(nullable = false, unique = true)
    private String nombre;

    /**
     * Descripción del tipo de habitación. Máximo 255 caracteres.
     */
    @Column(length = 255)
    private String descripcion;

    /**
     * Capacidad base de personas para este tipo de habitación.
     */
    @Column(nullable = false)
    private int capacidadBase;

    /**
     * Capacidad máxima de personas permitida para este tipo de habitación.
     */
    @Column(nullable = false)
    private int capacidadMaxima;

    /**
     * Precio base por noche para este tipo de habitación.
     */
    @Column(nullable = false)
    private BigDecimal precioBase;

    /**
     * Precio con IVA por noche (valor impuesto almacenado).
     */
    @Column(name = "precio_con_iva", precision = 10, scale = 2)
    private BigDecimal precioConIva;

    /**
     * Precio adicional por cada persona extra que supere la capacidad base.
     */
    @Column(nullable = false)
    private BigDecimal precioPersonaExtra;

    /**
     * Lista de habitaciones físicas de este tipo. Relación uno-a-muchos con
     * cascade.
     */
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habitacion> habitaciones;

    /**
     * Valida que un precio dado no sea menor al precio base del tipo de
     * habitación.
     *
     * @param precio el precio a validar
     * @throws IllegalStateException si el precio es menor al precio base
     */
    public void validarPrecio(BigDecimal precio) {
        if (precio != null && precio.compareTo(this.precioBase) < 0) {
            throw new IllegalStateException(
                    "El precio por noche ($" + precio + ") no puede ser menor al precio base ($" + this.precioBase + ")"
            );
        }
    }
}
