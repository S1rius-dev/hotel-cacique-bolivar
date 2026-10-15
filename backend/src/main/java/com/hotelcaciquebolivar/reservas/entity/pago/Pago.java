package com.hotelcaciquebolivar.reservas.entity.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un pago registrado en el sistema.
 * Cada pago está asociado a una reserva y contiene información sobre
 * el monto, método de pago, tipo y estado de la transacción.
 *
 * <p>Tabla: {@code pagos}</p>
 */
@Entity
@Table(
    name = "pagos",
    indexes = {
        @jakarta.persistence.Index(name = "idx_pago_reserva_estado", columnList = "reserva_id, estado"),
        @jakarta.persistence.Index(name = "idx_pago_fecha", columnList = "fecha_pago")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pago {

    /** Identificador único del pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código único del pago. Se genera automáticamente con formato PAG-AÑO-SUFijo. */
    @Column(name = "codigo_pago", unique = true, nullable = false, length = 20)
    private String codigoPago;

    /** Reserva a la que pertenece el pago. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    /** Monto del pago. Precisión 10, escala 2. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /** Tipo de pago según el enumerador {@link TipoPago}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPago tipoPago;

    /** Método de pago utilizado según el enumerador {@link MetodoPago}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodoPago;

    /** Estado del pago según el enumerador {@link EstadoPago}. Por defecto COMPLETADO. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estado = EstadoPago.COMPLETADO;

    /** Referencia externa de la transacción (ej: ID de pasarela de pago). Máximo 100 caracteres. */
    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    /** Observaciones adicionales del pago. Máximo 500 caracteres. */
    @Column(length = 500)
    private String observaciones;

    /** Fecha y hora en que se realizó el pago. */
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Reparto del monto del pago entre las cajas de destino.
     * Solo se llena para pagos en efectivo, que son los que generan movimiento
     * de caja. Para otros métodos de pago queda vacía.
     */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PagoDesglose> desglose = new ArrayList<>();

    /** Callback JPA que establece fechas y genera código de pago antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (codigoPago == null) {
            codigoPago = generarCodigoPago();
        }
    }

    /**
     * Genera un código de pago único con formato PAG-AÑO-SUFijo.
     *
     * @return código de pago generado
     */
    private String generarCodigoPago() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAG-" + LocalDate.now().getYear() + "-" + sufijo;
    }
}
