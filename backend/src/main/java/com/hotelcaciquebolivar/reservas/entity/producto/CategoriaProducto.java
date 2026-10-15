package com.hotelcaciquebolivar.reservas.entity.producto;

/**
 * Enumeración legacy que define las categorías de productos del hotel.
 * Mantenida por compatibilidad con datos existentes. Nuevos productos
 * deben utilizar la entidad {@link Categoria}.
 */
public enum CategoriaProducto {
    /** Bebidas (agua, gaseosas, jugos, cervezas, etc.). */
    BEBIDA,
    /** Alimentos y snacks (galletas, papas, chocolate, etc.). */
    MECATO,
    /** Productos de higiene (cepillo de dientes, pasta dental, etc.). */
    HIGIENE,
    /** Productos de cuidado personal (crema, desodorante, etc.). */
    CUIDADO_PERSONAL,
    /** Productos para adultos. */
    ADULTOS,
    /** Otras categorías no especificadas. */
    OTROS
}
