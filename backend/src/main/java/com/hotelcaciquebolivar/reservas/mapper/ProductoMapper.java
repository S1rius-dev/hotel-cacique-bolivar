package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hotelcaciquebolivar.reservas.dto.producto.CategoriaDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.MovimientoInventarioDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoCreateDTO;
import com.hotelcaciquebolivar.reservas.dto.producto.ProductoResponseDTO;
import com.hotelcaciquebolivar.reservas.entity.producto.Categoria;
import com.hotelcaciquebolivar.reservas.entity.producto.MovimientoInventario;
import com.hotelcaciquebolivar.reservas.entity.producto.Producto;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Producto} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(ProductoCreateDTO)} - DTO de creacion a nueva entidad.</li>
 *   <li>{@link #toResponseDTO(Producto)} - Entidad a DTO de respuesta con estado de stock calculado.</li>
 *   <li>{@link #toCategoriaDTO(Categoria)} - Entidad de categoria a DTO.</li>
 *   <li>{@link #toMovimientoDTO(MovimientoInventario)} - Entidad de movimiento de inventario a DTO.</li>
 * </ul>
 *
 * <p>El metodo personalizado {@link #estadoStock(Producto)} determina el estado del stock
 * del producto (agotado, bajo o normal).</p>
 */
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    /**
     * Convierte un DTO de creacion {@link ProductoCreateDTO} en una nueva entidad {@link Producto}.
     *
     * <p>Los campos {@code id}, {@code activo}, {@code createdAt}, {@code updatedAt}, {@code version}
     * y {@code categoria} se ignoran porque se asignan durante la persistencia.</p>
     *
     * @param dto el DTO de creacion con los datos del producto.
     * @return una nueva entidad {@link Producto} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Producto toEntity(ProductoCreateDTO dto);

    /**
     * Convierte una entidad {@link Producto} en un DTO de respuesta {@link ProductoResponseDTO}.
     *
     * <p>Mapea la categoria asociada y calcula el estado del stock mediante el metodo personalizado
     * {@link #estadoStock(Producto)}.</p>
     *
     * @param producto la entidad de producto a convertir.
     * @return el DTO de respuesta con los campos mapeados y el estado de stock.
     */
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "estadoStock", source = "producto", qualifiedByName = "estadoStock")
    ProductoResponseDTO toResponseDTO(Producto producto);

    /**
     * Convierte una entidad {@link Categoria} en un DTO {@link CategoriaDTO}.
     *
     * @param categoria la entidad de categoria a convertir.
     * @return el DTO con los campos mapeados directamente.
     */
    CategoriaDTO toCategoriaDTO(Categoria categoria);

    /**
     * Convierte una entidad {@link MovimientoInventario} en un DTO {@link MovimientoInventarioDTO}.
     *
     * <p>Extrae el nombre del producto y el email del usuario que realizo el movimiento.</p>
     *
     * @param m la entidad de movimiento de inventario a convertir.
     * @return el DTO con los campos mapeados.
     */
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "usuario", source = "usuario.email")
    MovimientoInventarioDTO toMovimientoDTO(MovimientoInventario m);

    /**
     * Metodo personalizado que determina el estado del stock de un producto.
     *
     * <p>Retorna:</p>
     * <ul>
     *   <li>{@code "agotado"} - si el stock actual es nulo o cero.</li>
     *   <li>{@code "bajo"} - si el stock actual es menor o igual al stock minimo.</li>
     *   <li>{@code "ok"} - en cualquier otro caso.</li>
     * </ul>
     *
     * @param p la entidad de producto a evaluar.
     * @return el estado del stock como cadena de texto.
     */
    @Named("estadoStock")
    default String estadoStock(Producto p) {
        if (p.getStockActual() == null || p.getStockActual() == 0) return "agotado";
        if (p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo()) return "bajo";
        return "ok";
    }
}
