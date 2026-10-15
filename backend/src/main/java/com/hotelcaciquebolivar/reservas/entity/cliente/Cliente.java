package com.hotelcaciquebolivar.reservas.entity.cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotelcaciquebolivar.reservas.entity.alerta.Alerta;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un cliente del hotel.
 * Almacena la información personal del cliente, incluyendo datos de identificación,
 * contacto y relaciones con reservas, alertas y un usuario del sistema.
 *
 * <p>Tabla: {@code clientes}</p>
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    /** Identificador único del cliente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de cédula o documento de identidad del cliente. Único, máximo 15 caracteres. */
    @Column(name = "cedula", unique = true, length = 15)
    private String cedula;

    /** Tipo de identificación del cliente. Relación many-to-one con {@link TipoIdentificacion}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_identificacion_id", nullable = false)
    private TipoIdentificacion tipoIdentificacion;

    /** Nombre completo del cliente. Máximo 30 caracteres. */
    @Column(name = "nombre_completo", length = 30)
    private String nombreCompleto;

    /** Apellido completo del cliente. Máximo 30 caracteres. */
    @Column(name = "apellido_completo", length = 30)
    private String apellidoCompleto;

    /** Número de teléfono del cliente. Máximo 10 caracteres. */
    @Column(length = 10)
    private String telefono;

    /** Municipio de expedición del documento de identidad del cliente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_cedula_codigo", nullable = false)
    private Municipio municipioExpedicion;

    /** Observaciones generales sobre el cliente. */
    @Column(name = "observaciones")
    private String observaciones;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Indica si el cliente está activo en el sistema. Por defecto {@code true}. Solo lectura en JSON. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /** Lista de reservas asociadas al cliente como titular. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "titularReserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas;

    /** Usuario del sistema vinculado al cliente. Relación uno-a-uno. Puede ser nulo. */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_id", unique = true)
    private User usuario;

    /** Lista de alertas asociadas al cliente. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Alerta> alertas = new ArrayList<>();

    // @OneToMany(mappedBy = "cliente")
    // private List<PQRS> pqrs;

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Callback JPA que establece la fecha de creación y el estado activo antes de persistir. */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.activo == null) {
            this.activo = true;
        }
    }
}
