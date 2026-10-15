package com.hotelcaciquebolivar.reservas.entity.caja;

import java.math.BigDecimal;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un turno de caja en el hotel.
 * Un turno corresponde a un periodo de operación (mañana, tarde o noche)
 * durante el cual un usuario administra una caja registradora.
 *
 * <p>Tabla: {@code turnos}</p>
 */
@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    /** Identificador único del turno. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Caja registradora asociada al turno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    /** Usuario que abrió el turno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_apertura_id", nullable = false)
    private User usuarioApertura;

    /** Usuario que cerró el turno. Puede ser nulo si el turno está abierto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private User usuarioCierre;

    /** Fecha y hora de apertura del turno. */
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    /** Fecha y hora de cierre del turno. Puede ser nulo si el turno está abierto. */
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    /** Monto inicial en efectivo con el que se inicia el turno. Precisión 12, escala 2. */
    @Column(name = "fondo_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal fondoInicial;

    /** Monto final contado físicamente al cerrar el turno. Precisión 12, escala 2. */
    @Column(name = "contado_final", precision = 12, scale = 2)
    private BigDecimal contadoFinal;

    /** Monto esperado al cierre según los movimientos registrados. Precisión 12, escala 2. */
    @Column(name = "esperado_cierre", precision = 12, scale = 2)
    private BigDecimal esperadoCierre;

    /** Diferencia entre el monto contado y el monto esperado. Precisión 12, escala 2. */
    @Column(name = "diferencia", precision = 12, scale = 2)
    private BigDecimal diferencia;

    /** Tipo de turno según el enumerador {@link TipoTurno}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_turno", nullable = false, length = 20)
    private TipoTurno tipoTurno;

    /** Estado del turno según el enumerador {@link EstadoTurno}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    /** Observaciones o notas al cierre del turno. Máximo 500 caracteres. */
    @Column(name = "observaciones_cierre", length = 500)
    private String observacionesCierre;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
