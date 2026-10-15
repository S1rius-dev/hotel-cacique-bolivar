package com.hotelcaciquebolivar.reservas.entity.pqrs;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una PQRS (Petición, Queja, Reclamo o Sugerencia).
 * Gestiona las solicitudes de los clientes con un ciclo de vida que incluye
 * estados como abierta, en revisión, respondida, cerrada y rechazada.
 *
 * <p>Tabla: {@code pqrs}</p>
 */
@Entity
@Table(name = "pqrs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pqrs {

    /** Identificador único de la PQRS. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código único de la PQRS. Formato: PQRS-AÑO-SECUENCIA. */
    @Column(name = "codigo", unique = true, nullable = false, length = 30)
    private String codigo;

    /** Tipo de PQRS según el enumerador {@link TipoPqrs}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoPqrs tipo;

    /** Prioridad de la PQRS según el enumerador {@link Prioridad}. Por defecto MEDIA. */
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 15)
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA;

    /** Estado de la PQRS según el enumerador {@link EstadosPqrs}. Por defecto ABIERTA. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadosPqrs estado = EstadosPqrs.ABIERTA;

    /** Asunto o título de la PQRS. Máximo 200 caracteres. */
    @Column(name = "asunto", length = 200)
    private String asunto;

    /** Descripción detallada de la PQRS. Máximo 2000 caracteres. */
    @Column(name = "descripcion", nullable = false, length = 2000)
    private String descripcion;

    /** Respuesta a la PQRS. Máximo 2000 caracteres. Puede ser nula si no se ha respondido. */
    @Column(name = "respuesta", length = 2000)
    private String respuesta;

    /** Fecha y hora de creación de la PQRS. No se puede actualizar. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha y hora de la última actualización de la PQRS. */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /** Usuario que creó la PQRS. Puede ser nulo si es de un cliente no registrado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /** Reserva asociada a la PQRS. Puede ser nula si no está vinculada a una reserva. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    /** Correo electrónico de contacto del solicitante. Máximo 120 caracteres. */
    @Column(name = "email_contacto", length = 120)
    private String emailContacto;

    /** Teléfono de contacto del solicitante. Máximo 20 caracteres. */
    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    /** Nombre completo del solicitante. Máximo 150 caracteres. */
    @Column(name = "nombre_contacto", length = 150)
    private String nombreContacto;

    /** Fecha y hora en que se respondió la PQRS. Puede ser nula. */
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /** Callback JPA que actualiza la fecha de actualización antes de actualizar. */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Cambia el estado de la PQRS a EN_REVISION.
     * Solo se puede realizar si el estado actual es ABIERTA.
     *
     * @throws IllegalStateException si el estado no es ABIERTA
     */
    public void abrirRevision() {
        if (estado != EstadosPqrs.ABIERTA) {
            throw new IllegalStateException(
                    "Solo se puede revisar una PQRS en estado ABIERTA"
            );
        }
        this.estado = EstadosPqrs.EN_REVISION;
    }

    /**
     * Responde la PQRS con el texto proporcionado.
     * Solo se puede realizar si el estado actual es EN_REVISION.
     *
     * @param respuesta el texto de la respuesta
     * @throws IllegalStateException si el estado no es EN_REVISION
     */
    public void responder(String respuesta) {
        if (estado != EstadosPqrs.EN_REVISION) {
            throw new IllegalStateException(
                    "Solo se puede responder una PQRS en estado EN_REVISION"
            );
        }
        this.respuesta = respuesta;
        this.estado = EstadosPqrs.RESPONDIDA;
        this.fechaRespuesta = LocalDateTime.now();
    }

    /**
     * Cierra la PQRS.
     * No se puede cerrar una PQRS que ya esté cerrada o rechazada.
     *
     * @throws IllegalStateException si la PQRS ya está cerrada o rechazada
     */
    public void cerrar() {
        if (estado == EstadosPqrs.CERRADA || estado == EstadosPqrs.RECHAZADA) {
            throw new IllegalStateException(
                    "No se puede cerrar una PQRS ya " + estado.name().toLowerCase()
            );
        }
        this.estado = EstadosPqrs.CERRADA;
    }

    /**
     * Rechaza la PQRS con el motivo proporcionado.
     * Solo se puede rechazar una PQRS en estado ABIERTA o EN_REVISION.
     *
     * @param motivo el motivo del rechazo
     * @throws IllegalStateException si el estado no es ABIERTA o EN_REVISION
     */
    public void rechazar(String motivo) {
        if (estado != EstadosPqrs.EN_REVISION && estado != EstadosPqrs.ABIERTA) {
            throw new IllegalStateException(
                    "Solo se puede rechazar una PQRS en estado ABIERTA o EN_REVISION"
            );
        }
        this.respuesta = motivo;
        this.estado = EstadosPqrs.RECHAZADA;
        this.fechaRespuesta = LocalDateTime.now();
    }

    /**
     * Reabre una PQRS cerrada o rechazada.
     * Limpia la respuesta y la fecha de respuesta.
     *
     * @throws IllegalStateException si la PQRS no está cerrada o rechazada
     */
    public void reabrir() {
        if (estado != EstadosPqrs.CERRADA && estado != EstadosPqrs.RECHAZADA) {
            throw new IllegalStateException(
                    "Solo se puede reabrir una PQRS cerrada o rechazada"
            );
        }
        this.estado = EstadosPqrs.ABIERTA;
        this.respuesta = null;
        this.fechaRespuesta = null;
    }

    /**
     * Asigna una prioridad a la PQRS.
     *
     * @param prioridad la prioridad a asignar. Si es nula, no se realiza ningún cambio.
     */
    public void asignarPrioridad(Prioridad prioridad) {
        if (prioridad == null) {
            return;
        }
        this.prioridad = prioridad;
    }
}
