package com.hotelcaciquebolivar.reservas.entity.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una transacción de pago procesada por una pasarela de pago.
 * Almacena los detalles completos de la interacción con el gateway de pago,
 * incluyendo referencias, payloads, firma digital y estado de la transacción.
 *
 * <p>Tabla: {@code transacciones_pago}</p>
 */
@Entity
@Table(
    name = "transacciones_pago",
    indexes = {
        @Index(name = "idx_transaccion_referencia_externa", columnList = "referencia_externa, gateway"),
        @Index(name = "idx_transaccion_idempotencia", columnList = "idempotencia_key"),
        @Index(name = "idx_transaccion_reserva", columnList = "reserva_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransaccionPago {

    /** Identificador único de la transacción. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Reserva asociada a la transacción. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    /** Pago del sistema asociado a esta transacción. Relación uno-a-uno. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    /** Tipo de pasarela de pago utilizada según {@link PaymentGatewayType}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGatewayType gateway;

    /** Estado de la transacción según la pasarela de pago. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pasarela", nullable = false, length = 20)
    private PaymentStatus estadoPasarela;

    /** Referencia externa de la transacción en la pasarela de pago. Única por gateway. */
    @Column(name = "referencia_externa", nullable = false, length = 100)
    private String referenciaExterna;

    /** Referencia interna del sistema. Puede ser nula. */
    @Column(name = "referencia_interna", length = 50)
    private String referenciaInterna;

    /** Monto de la transacción. Precisión 12, escala 2. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Código de la moneda (ej: COP, USD). Máximo 10 caracteres. */
    @Column(length = 10)
    private String moneda;

    /** Tipo de pago según {@link TipoPago}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false, length = 20)
    private TipoPago tipoPago;

    /** Método de pago utilizado según {@link MetodoPago}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    /** Clave de idempotencia para prevenir procesamiento duplicado. Única en el sistema. */
    @Column(name = "idempotencia_key", nullable = false, unique = true, length = 100)
    private String idempotenciaKey;

    /** Payload de la solicitud enviada a la pasarela de pago en formato JSON. */
    @Column(name = "payload_solicitud", columnDefinition = "TEXT")
    private String payloadSolicitud;

    /** Payload de la notificación recibida de la pasarela de pago en formato JSON. */
    @Column(name = "payload_notificacion", columnDefinition = "TEXT")
    private String payloadNotificacion;

    /** Firma digital recibida de la pasarela de pago para verificación. Máximo 500 caracteres. */
    @Column(name = "firma_recibida", length = 500)
    private String firmaRecibida;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    @Column(name = "version")
    private Long version;

    /** Callback JPA que establece las fechas antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
