package com.hotelcaciquebolivar.reservas.entity.consumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.producto.MovimientoInventario;
import com.hotelcaciquebolivar.reservas.entity.producto.Producto;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un consumo de producto asociado a una reserva.
 * Registra los productos consumidos por un huésped durante su estancia,
 * incluyendo cantidad, precio unitario y monto total.
 *
 * <p>Tabla: {@code consumos_reserva}</p>
 */
@Entity
@Table(name = "consumos_reserva", indexes = {
        @jakarta.persistence.Index(name = "idx_consumo_reserva", columnList = "reserva_id"),
        @jakarta.persistence.Index(name = "idx_consumo_detalle", columnList = "detalle_reserva_id"),
        @jakarta.persistence.Index(name = "idx_consumo_producto", columnList = "producto_id"),
        @jakarta.persistence.Index(name = "idx_consumo_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumo {

    /** Identificador único del consumo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reserva a la que pertenece el consumo. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    /** Detalle de reserva específico al que se asocia el consumo. Puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_reserva_id")
    private DetalleReserva detalleReserva;

    /** Producto consumido. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Movimiento de inventario generado por el consumo. Relación uno-a-uno. Puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_inventario_id", unique = true)
    private MovimientoInventario movimientoInventario;

    /** Cantidad del producto consumido. */
    @Column(nullable = false)
    private Integer cantidad;

    /** Precio unitario del producto al momento del consumo. Precisión 12, escala 2. */
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** Monto total del consumo (precio unitario × cantidad). Precisión 12, escala 2. */
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    /** Estado del consumo según el enumerador {@link EstadoConsumo}. Por defecto PENDIENTE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoConsumo estado = EstadoConsumo.PENDIENTE;

    /** Usuario que registró el consumo. Puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /** Observaciones adicionales del consumo. Máximo 500 caracteres. */
    @Column(length = 500)
    private String observaciones;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Callback JPA que establece fechas, calcula monto total y estado antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (montoTotal == null && precioUnitario != null && cantidad != null) {
            montoTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        if (estado == null) estado = EstadoConsumo.PENDIENTE;
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
