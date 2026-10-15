package com.hotelcaciquebolivar.reservas.entity.notificacion;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.auth.User;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una notificación enviada a un usuario del sistema.
 * Las notificaciones informan sobre eventos relevantes como reservas,
 * pagos, check-in, check-out y alertas de clientes.
 *
 * <p>Tabla: {@code notificaciones}</p>
 */
@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    /** Identificador único de la notificación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario destinatario de la notificación. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /** Tipo de notificación según el enumerador {@link TipoNotificacion}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    /** Mensaje descriptivo de la notificación. Máximo 500 caracteres. */
    @Column(nullable = false, length = 500)
    private String mensaje;

    /** Origen o módulo del sistema que generó la notificación. Máximo 20 caracteres. */
    @Column(nullable = false, length = 20)
    private String origen;

    /** Nombre descriptivo del origen de la notificación. Máximo 120 caracteres. */
    @Column(length = 120)
    private String origenNombre;

    /** Detalles adicionales de la notificación en formato JSON. Almacenado como texto. */
    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detallesJson;

    /** Identificador de la entidad de referencia asociada a la notificación. Puede ser nulo. */
    @Column(name = "referencia_id")
    private Long referenciaId;

    /** Tipo de la entidad de referencia asociada. Máximo 20 caracteres. Puede ser nulo. */
    @Column(name = "referencia_tipo", length = 20)
    private String referenciaTipo;

    /** Indica si la notificación ha sido leída. Por defecto {@code false}. */
    @Column(nullable = false)
    private boolean leida = false;

    /** Fecha y hora de creación de la notificación. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha y hora en que la notificación fue leída. Puede ser nulo si no se ha leído. */
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
}
