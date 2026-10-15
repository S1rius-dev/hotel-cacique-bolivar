package com.hotelcaciquebolivar.reservas.entity.producto;

import java.time.LocalDateTime;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.hotelcaciquebolivar.reservas.entity.auth.User;

/**
 * Entidad que representa un movimiento de inventario de un producto.
 * Registra cada entrada, salida, venta o ajuste de stock, manteniendo
 * un historial completo de movimientos con el stock resultante.
 *
 * <p>Tabla: {@code movimientos_inventario}</p>
 */
@Entity
@Table(name = "movimientos_inventario", indexes = {
        @jakarta.persistence.Index(name = "idx_mov_producto", columnList = "producto_id"),
        @jakarta.persistence.Index(name = "idx_mov_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    /** Identificador único del movimiento de inventario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Producto afectado por el movimiento. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Tipo de movimiento según el enumerador {@link TipoMovimiento}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    /** Cantidad de unidades movidas. */
    @Column(nullable = false)
    private Integer cantidad;

    /** Stock resultante del producto después del movimiento. */
    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    /** Motivo o descripción del movimiento. Máximo 300 caracteres. */
    @Column(length = 300)
    private String motivo;

    /** Usuario que registró el movimiento. Puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /** Identificador de la entidad de referencia asociada (ej: ID de consumo). Puede ser nulo. */
    @Column(name = "referencia_id")
    private Long referenciaId;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
