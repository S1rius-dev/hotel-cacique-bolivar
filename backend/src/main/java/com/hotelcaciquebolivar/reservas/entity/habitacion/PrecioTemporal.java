package com.hotelcaciquebolivar.reservas.entity.habitacion;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa un precio temporal para un tipo de habitaciÃ³n.
 * Permite definir precios especiales para periodos especÃ­ficos (temporada
 * alta, feriados, promociones), sobreescribiendo el precio base del tipo de
 * habitaciÃ³n.
 *
 * <p>
 * Tabla: {@code precios_temporales}</p>
 */
@Entity
@Table(name = "precios_temporales")
@BatchSize(size = 20)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrecioTemporal {

    /**
     * Identificador Ãºnico del precio temporal.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Tipo de habitaciÃ³n al que aplica el precio temporal. RelaciÃ³n
     * many-to-one.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_habitacion_id", nullable = false)
    private TipoHabitacion tipoHabitacion;

    /**
     * Fecha de inicio del periodo de vigencia del precio temporal.
     */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del periodo de vigencia del precio temporal.
     */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    /**
     * Precio por noche durante el periodo temporal. PrecisiÃ³n 10, escala 2.
     */
    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNoche;

    /**
     * Precio por noche con IVA durante el periodo temporal (valor impuesto
     * almacenado).
     */
    @Column(name = "precio_noche_iva", precision = 10, scale = 2)
    private BigDecimal precioNocheIva;

    /**
     * Precio por persona extra durante el periodo temporal. PrecisiÃ³n 10,
     * escala 2.
     */
    @Column(name = "precio_persona_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPersonaExtra;

    /**
     * Verifica si una fecha especÃ­fica estÃ¡ cubierta por este precio
     * temporal.
     *
     * @param fecha la fecha a evaluar
     * @return {@code true} si la fecha estÃ¡ dentro del rango de vigencia
     */
    public boolean cubre(LocalDate fecha) {
        return fecha != null
                && !fecha.isBefore(fechaInicio)
                && !fecha.isAfter(fechaFin);
    }

    /**
     * Verifica si este precio temporal se solapa con otro precio temporal. Dos
     * precios se solapan si sus rangos de fechas tienen al menos un dÃ­a en
     * comÃºn.
     *
     * @param otro el otro precio temporal a comparar
     * @return {@code true} si los rangos de fechas se solapan
     */
    public boolean solapa(PrecioTemporal otro) {
        return !this.fechaInicio.isAfter(otro.getFechaFin())
                && !otro.getFechaInicio().isAfter(this.fechaFin);
    }
}
