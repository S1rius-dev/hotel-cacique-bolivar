package com.hotelcaciquebolivar.reservas.repository.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.alerta.Alerta;

/**
 * Repositorio para la gestión de alertas de clientes.
 * Permite consultar alertas asociadas a un cliente específico.
 */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /**
     * Obtiene las alertas de un cliente específico, con paginación.
     *
     * @param clienteId identificador del cliente
     * @param pageable paginación de resultados
     * @return página de alertas del cliente
     */
    Page<Alerta> findByClienteId(Long clienteId, Pageable pageable);
}
