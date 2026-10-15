package com.hotelcaciquebolivar.reservas.entity.reserva;

import java.math.BigDecimal;

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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un ajuste aplicado a una reserva.
 * Los ajustes permiten agregar cargos adicionales, descuentos o modificaciones
 * al total de la reserva, ya sea como valor fijo o porcentaje.
 *
 * <p>Tabla: {@code ajustes_reserva}</p>
 */
@Entity
@Table(
    name = "ajustes_reserva",
    indexes = {
        @jakarta.persistence.Index(name = "idx_ajuste_reserva", columnList = "reserva_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AjusteReserva {

    /** Identificador único del ajuste. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Reserva a la que se aplica el ajuste. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    /** Tipo de ajuste según el enumerador {@link TipoAjuste}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ajuste", nullable = false, length = 20)
    private TipoAjuste tipoAjuste;

    /** Modo de cálculo del ajuste según el enumerador {@link ModoCalculo}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_calculo", nullable = false, length = 20)
    private ModoCalculo modoCalculo;

    /** Concepto o descripción del ajuste. Máximo 100 caracteres. */
    @Column(length = 100)
    private String concepto;

    /** Valor del ajuste (monto fijo o porcentaje). Precisión 12, escala 2. */
    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    /** Indica si el ajuste está activo. Por defecto {@code true}. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Desactiva el ajuste. */
    public void desactivar() {
        this.activo = false;
    }
}
