package com.hotelcaciquebolivar.reservas.entity.empleado;

import java.time.LocalDate;

import com.hotelcaciquebolivar.reservas.entity.auth.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa un empleado del hotel.
 * Almacena la información personal y laboral del empleado, incluyendo
 * su cargo, fecha de_ingreso y vínculo con un usuario del sistema.
 *
 * <p>Tabla: {@code empleados}</p>
 */
@Entity
@Table(name = "empleados")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    /** Identificador único del empleado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Nombres del empleado. */
    private String nombres;

    /** Apellidos del empleado. */
    private String apellidos;

    /** Número de teléfono del empleado. */
    private String telefono;

    /** Cargo del empleado según el enumerador {@link Cargo}. */
    @Enumerated(EnumType.STRING)
    private Cargo cargo;

    /** Fecha de_ingreso del empleado al hotel. */
    private LocalDate fechaIngreso;

    /** Indica si el empleado está activo. Por defecto {@code true}. */
    private Boolean activo = true;

    /** Usuario del sistema vinculado al empleado. Relación uno-a-uno. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User usuario;
}
