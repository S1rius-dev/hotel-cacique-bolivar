package com.hotelcaciquebolivar.reservas.repository.pqrs;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.pqrs.EstadosPqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.Pqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;

/**
 * Repositorio para la gestión de PQRS (Peticiones, Quejas, Reclamos y Sugerencias).
 * Permite buscar PQRS por código, usuario, estado, tipo y reserva asociada.
 */
public interface PqrsRepository extends JpaRepository<Pqrs, Long> {

    /**
     * Busca un PQRS por su código único.
     *
     * @param código código único del PQRS
     * @return Optional con el PQRS encontrado o vacío si no existe
     */
    Optional<Pqrs> findByCodigo(String codigo);

    /**
     * Verifica si existe un PQRS con el código especificado.
     *
     * @param codigo código del PQRS a verificar
     * @return true si existe un PQRS con ese código, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Obtiene los PQRS de un usuario específico, con paginación.
     *
     * @param usuarioId identificador del usuario
     * @param pageable paginación de resultados
     * @return página de PQRS del usuario
     */
    Page<Pqrs> findByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Obtiene los PQRS filtrados por estado, con paginación.
     *
     * @param estado estado del PQRS (ABIERTO, EN_PROCESO, CERRADO, etc.)
     * @param pageable paginación de resultados
     * @return página de PQRS con el estado especificado
     */
    Page<Pqrs> findByEstado(EstadosPqrs estado, Pageable pageable);

    /**
     * Obtiene los PQRS filtrados por tipo, con paginación.
     *
     * @param tipo tipo de PQRS (PETICION, QUEJA, RECLAMO, SUGERENCIA)
     * @param pageable paginación de resultados
     * @return página de PQRS del tipo especificado
     */
    Page<Pqrs> findByTipo(TipoPqrs tipo, Pageable pageable);

    /**
     * Obtiene los PQRS asociados a una reserva específica, con paginación.
     *
     * @param reservaId identificador de la reserva
     * @param pageable paginación de resultados
     * @return página de PQRS de la reserva
     */
    Page<Pqrs> findByReservaId(Long reservaId, Pageable pageable);
}
