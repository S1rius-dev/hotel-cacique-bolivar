package com.hotelcaciquebolivar.reservas.repository.cliente;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

/**
 * Repositorio para la gestión de clientes del hotel.
 * Permite buscar clientes por cédula, usuario y tipo de identificación,
 * así como obtener todas las cédulas registradas.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    /**
     * Busca un cliente por su número de cédula.
     *
     * @param cedula número de cédula del cliente
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByCedula(String cedula);

    /**
     * Busca un cliente por su ID de usuario asociado.
     *
     * @param userId identificador del usuario
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByUsuarioId(Long userId);

    /**
     * Busca un cliente por tipo de identificación y número de cédula.
     *
     * @param tipoIdentificacionId identificador del tipo de identificación
     * @param cedula número de cédula del cliente
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByTipoIdentificacionIdAndCedula(Long tipoIdentificacionId, String cedula);

    /**
     * Obtiene todas las cédulas de clientes registrados que no son nulas.
     *
     * @return conjunto de cédulas de clientes
     */
    @Query("SELECT c.cedula FROM Cliente c WHERE c.cedula IS NOT NULL")
    Set<String> findAllCedulas();
}
