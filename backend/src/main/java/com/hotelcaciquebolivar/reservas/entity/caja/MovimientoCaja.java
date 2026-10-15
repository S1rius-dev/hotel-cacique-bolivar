package com.hotelcaciquebolivar.reservas.entity.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.pago.MetodoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un movimiento de caja.
 * Cada movimiento puede ser un ingreso o un gasto, y está asociado a un
 * método de pago y opcionalmente a un pago del sistema.
 * Si hay un turno activo, se asocia al mismo; si no, queda con turno nulo.
 *
 * <p>Tabla: {@code movimientos_caja}</p>
 */
@Entity
@Table(name = "movimientos_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCaja {

    /** Identificador único del movimiento de caja. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Turno de caja al que pertenece el movimiento. Puede ser nulo si el movimiento ocurrió sin turno activo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    /** Caja registradora a la que pertenece el movimiento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    /** Tipo de movimiento: ingreso o gasto, según {@link TipoMovimientoCaja}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoCaja tipo;

    /** Concepto o descripción del movimiento. Máximo 200 caracteres. */
    @Column(nullable = false, length = 200)
    private String concepto;

    /** Monto del movimiento. Precisión 12, escala 2. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Método de pago utilizado para el movimiento. */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    /** Pago asociado al movimiento. Puede ser nulo para movimientos no ligados a un pago. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    /** Usuario que registró el movimiento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /** Fecha y hora del movimiento. Se asigna automáticamente si no se proporciona. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Observaciones adicionales del movimiento. Máximo 500 caracteres. */
    @Column(length = 500)
    private String observaciones;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Callback JPA que establece las fechas antes de persistir. */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
