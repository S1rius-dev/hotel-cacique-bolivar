package com.hotelcaciquebolivar.reservas.entity.pago;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad que representa un evento o cambio de estado en la vida de un pago.
 * Registra cada transición importante del pago (creación, confirmación,
 * reembolso, cancelación, etc.) con su descripción y timestamp.
 *
 * <p>Tabla: {@code pago_eventos}</p>
 */
@Entity
@Table(name = "pago_eventos", indexes = {
        @jakarta.persistence.Index(name = "idx_evento_pago_fecha", columnList = "pago_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEvento {

    /** Identificador único del evento de pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pago al que pertenece el evento. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    /** Descripción del evento. Máximo 120 caracteres. */
    @Column(nullable = false, length = 120)
    private String descripcion;

    /** Subtítulo o detalle adicional del evento. Máximo 120 caracteres. */
    @Column(length = 120)
    private String subtitulo;

    /** Fecha y hora de creación del evento. Se establece en zona horaria de Bogotá. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.of("America/Bogota"));
        }
    }
}
