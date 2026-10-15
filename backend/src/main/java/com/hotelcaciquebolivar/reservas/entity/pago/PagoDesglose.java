package com.hotelcaciquebolivar.reservas.entity.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;

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
 * Línea de desglose que indica qué parte de un pago pertenece a cada caja.
 *
 * <p>Un pago se cobra una sola vez, pero se reparte entre RECEPCION y TIENDA
 * según el origen del dinero. Cada línea es inmutable una vez persistida y
 * suma exactamente el monto del pago, lo que permite asentar un movimiento de
 * caja por línea y auditar el reparto.</p>
 *
 * <p>Tabla: {@code pagos_desglose}</p>
 */
@Entity
@Table(name = "pagos_desglose", indexes = {
        @jakarta.persistence.Index(name = "idx_pago_desglose_pago", columnList = "pago_id"),
        @jakarta.persistence.Index(name = "idx_pago_desglose_origen", columnList = "origen"),
        @jakarta.persistence.Index(name = "idx_pago_desglose_consumo", columnList = "consumo_id"),
        @jakarta.persistence.Index(name = "idx_pago_desglose_pago_origen", columnList = "pago_id, origen")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDesglose {

    /** Identificador único de la línea de desglose. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pago al que pertenece esta línea. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    /** Caja destino del dinero de esta línea. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenCobro origen;

    /** Monto asignado a esta línea. Precisión 12, escala 2. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Descripción legible del concepto cobrado. Máximo 200 caracteres. */
    @Column(nullable = false, length = 200)
    private String concepto;

    /**
     * Consumo de tienda al que se atribuye esta línea, cuando aplica.
     * Permite repartir correctamente el cobro entre consumos cuando la reserva
     * se paga en varias cuotas. Es nulo en líneas de origen RECEPCION.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id")
    private Consumo consumo;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
