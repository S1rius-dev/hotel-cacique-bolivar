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
 * Entidad que representa un municipio de Colombia.
 * Se utiliza como referencia geográfica para los clientes y la expedición
 * de sus documentos de identidad. Los datos se cargan desde un archivo CSV
 * al iniciar la aplicación.
 *
 * <p>Tabla: {@code municipios}</p>
 */
@Entity
@Table(name = "municipios")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Municipio {

    /** Identificador único del municipio. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código DANE del municipio. */
    @Column(name = "codigo")
    private String codigo;

    /** Nombre del municipio. No puede ser nulo. */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /** Nombre del departamento al que pertenece el municipio. No puede ser nulo. */
    @Column(name = "departamento", nullable = false)
    private String departamento;

    /** Lista de clientes cuyo documento fue expedido en este municipio. Ignorado en JSON. */
    @JsonIgnore
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "municipioExpedicion")
    private List<Cliente> clientes = new ArrayList<>();
}
