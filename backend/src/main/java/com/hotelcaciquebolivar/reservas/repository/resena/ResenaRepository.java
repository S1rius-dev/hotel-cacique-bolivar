package com.hotelcaciquebolivar.reservas.repository.resena;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.resena.Resena;

/**
 * Repositorio para la gestión de reseñas de clientes.
 * Permite consultar reseñas por calificación y cliente, y obtener las más recientes.
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    /**
     * Obtiene todas las reseñas ordenadas por fecha de creación descendente, con paginación.
     *
     * @param pageable paginación de resultados
     * @return página de reseñas ordenadas por fecha de creación
     */
    Page<Resena> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Obtiene las 12 reseñas más recientes.
     *
     * @return lista de las 12 reseñas más recientes
     */
    List<Resena> findTop12ByOrderByCreatedAtDesc();

    /**
     * Obtiene las reseñas filtradas por calificación, con paginación.
     *
     * @param calificación calificación de la reseña (1-5)
     * @param pageable paginación de resultados
     * @return página de reseñas con la calificación especificada
     */
    Page<Resena> findByCalificacion(Integer calificacion, Pageable pageable);

    /**
     * Obtiene las reseñas de un cliente específico, con paginación.
     *
     * @param clienteId identificador del cliente
     * @param pageable paginación de resultados
     * @return página de reseñas del cliente
     */
    Page<Resena> findByClienteId(Long clienteId, Pageable pageable);
}
