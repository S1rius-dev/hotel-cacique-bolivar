package com.hotelcaciquebolivar.reservas.entity.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un producto disponible en el hotel (minibar, tienda, etc.).
 * Administra información de inventario, precios y categorías de productos.
 *
 * <p>Tabla: {@code productos}</p>
 */
@Entity
@Table(name = "productos", indexes = {
        @jakarta.persistence.Index(name = "idx_producto_sku", columnList = "codigo_barra", unique = true),
        @jakarta.persistence.Index(name = "idx_producto_categoria", columnList = "categoria_id"),
        @jakarta.persistence.Index(name = "idx_producto_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    /** Identificador único del producto. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del producto. Máximo 150 caracteres. */
    @Column(nullable = false, length = 150)
    private String nombre;

    /** Código de barras o SKU del producto. Único en el sistema. Máximo 50 caracteres. */
    @Column(name = "codigo_barra", unique = true, length = 50)
    private String skuBarcode;

    /** Stock actual disponible del producto. Por defecto 0. */
    @Column(name = "stock_actual", nullable = false)
    @Builder.Default
    private Integer stockActual = 0;

    /** Stock mínimo recomendado para generar alertas de reabastecimiento. Por defecto 5. */
    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = 5;

    /** Precio de venta al público del producto. Precisión 12, escala 2. Por defecto 0. */
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal precioVenta = BigDecimal.ZERO;

    /** Costo de adquisición del producto. Precisión 12, escala 2. Por defecto 0. */
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costo = BigDecimal.ZERO;

    /** Categoría del producto. Relación many-to-one con {@link Categoria}. Puede ser nula. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Campo legacy de categoría en formato string. Mantenido por compatibilidad con datos existentes. */
    @Column(name = "categoria_legacy", length = 50)
    private String categoriaLegacy;

    /** Indica si el producto está activo y disponible para venta. Por defecto {@code true}. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Fecha y hora de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    private Long version;

    /**
     * Obtiene el precio de venta del producto (método de compatibilidad con campo legacy "precio").
     *
     * @return precio de venta
     */
    public BigDecimal getPrecio() {
        return precioVenta;
    }

    /**
     * Establece el precio de venta del producto (método de compatibilidad con campo legacy "precio").
     *
     * @param precio el precio de venta a establecer
     */
    public void setPrecio(BigDecimal precio) {
        this.precioVenta = precio;
    }

    /**
     * Obtiene el stock actual del producto (método de compatibilidad con campo legacy "stock").
     *
     * @return stock actual
     */
    public Integer getStock() {
        return stockActual;
    }

    /**
     * Establece el stock actual del producto (método de compatibilidad con campo legacy "stock").
     *
     * @param stock el stock a establecer
     */
    public void setStock(Integer stock) {
        this.stockActual = stock;
    }

    /**
     * Obtiene el código de barras del producto (método de compatibilidad).
     *
     * @return código de barras
     */
    public String getCodigoBarra() {
        return skuBarcode;
    }

    /**
     * Establece el código de barras del producto (método de compatibilidad).
     *
     * @param codigoBarra el código de barras a establecer
     */
    public void setCodigoBarra(String codigoBarra) {
        this.skuBarcode = codigoBarra;
    }

    /** Callback JPA que establece fechas y valores por defecto antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (stockActual == null) stockActual = 0;
        if (stockMinimo == null) stockMinimo = 5;
        if (precioVenta == null) precioVenta = BigDecimal.ZERO;
        if (costo == null) costo = BigDecimal.ZERO;
        if (activo == null) activo = true;
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
