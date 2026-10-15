package com.hotelcaciquebolivar.reservas.repository.producto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.producto.Categoria;

/**
 * Repositorio para la gestión de categorías de productos.
 * Permite buscar categorías por nombre y verificar existencia.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre (case-insensitive).
     *
     * @param nombre nombre de la categoría
     * @return Optional con la categoría encontrada o vacío si no existe
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    /**
     * Verifica si existe una categoría con el nombre especificado (case-insensitive).
     *
     * @param nombre nombre de la categoría a verificar
     * @return true si existe una categoría con ese nombre, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
