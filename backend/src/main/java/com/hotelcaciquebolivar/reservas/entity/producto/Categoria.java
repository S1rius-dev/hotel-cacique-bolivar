package com.hotelcaciquebolivar.reservas.entity.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una categoría de productos del hotel.
 * Se utiliza para organizar los productos en grupos (bebidas, alimentos, higiene, etc.).
 *
 * <p>Tabla: {@code categorias}</p>
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    /** Identificador único de la categoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la categoría. Único en el sistema. Máximo 80 caracteres. */
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;
}
