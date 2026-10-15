package com.hotelcaciquebolivar.reservas.entity.cliente;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
 * Entidad que representa un tipo de documento de identificación.
 * Define los tipos de identificación válidos para los clientes (cédula de ciudadanía,
 * cédula de extranjería, tarjeta de identidad, etc.).
 *
 * <p>Tabla: {@code tipos_identificacion}</p>
 */
@Entity
@Table(name = "tipos_identificacion")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class TipoIdentificacion {

    /** Identificador único del tipo de identificación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código abreviado del tipo de identificación. Único, máximo 10 caracteres. */
    @Column(length = 10, nullable = false, unique = true)
    private String codigo;

    /** Nombre completo del tipo de identificación. Máximo 100 caracteres. */
    @Column(length = 100, nullable = false)
    private String nombre;

    /** Indica si el tipo de identificación está activo. Por defecto {@code true}. */
    @Column(nullable = false)
    private Boolean activo = true;

    /** Lista de clientes que utilizan este tipo de identificación. Ignorado en JSON. */
    @JsonIgnore
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "tipoIdentificacion")
    private List<Cliente> clientes = new ArrayList<>();
}
