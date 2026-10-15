package com.hotelcaciquebolivar.reservas.entity.pago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que controla la secuencia de numeración de recibos por año.
 * Utiliza un enfoque basado en optimistic locking para generar números
 * secuenciales únicos de recibo de forma concurrente.
 *
 * <p>Tabla: {@code recibo_secuencia}</p>
 */
@Entity
@Table(name = "recibo_secuencia")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReciboSequence {

    /** Año de la secuencia. Funciona como identificador único. */
    @Id
    @Column(name = "year")
    private Integer year;

    /** Último número utilizado en la secuencia del año. Por defecto 0. */
    @Column(name = "last_number", nullable = false)
    @Builder.Default
    private Long lastNumber = 0L;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    private Long version;
}
