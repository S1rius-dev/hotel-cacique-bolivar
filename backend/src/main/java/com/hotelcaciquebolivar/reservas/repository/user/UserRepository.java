package com.hotelcaciquebolivar.reservas.repository.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;

/**
 * Repositorio para la gestión de usuarios del sistema.
 * Permite buscar usuarios por email, proveedor de autenticación y roles.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su email.
     *
     * @param email email del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado.
     *
     * @param email email a verificar
     * @return true si existe un usuario con ese email, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Busca un usuario por su ID de proveedor de autenticación externa (Google, Facebook, etc.).
     *
     * @param providerId identificador del proveedor de autenticación
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<User> findByProviderId(String providerId);

    /**
     * Obtiene todos los usuarios activos que tienen al menos uno de los roles especificados.
     *
     * @param roles colección de roles a buscar
     * @return lista de usuarios activos con los roles indicados
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles AND u.activo = true")
    List<User> findByRolesInAndActivoTrue(Collection<Rol> roles);

}
