package com.hotelcaciquebolivar.reservas.config.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

/**
 * Implementación personalizada de {@link UserDetailsService} para Spring Security.
 *
 * <p>Implementa la interfaz {@link UserDetailsService} para cargar usuarios desde
 * la base de datos de la aplicación. Se utiliza por el proveedor de autenticación
 * DAO para autenticar usuarios mediante email y contraseña.</p>
 *
 * <p>El método {@link #loadUserByUsername(String)} utiliza el email como nombre
 * de usuario (no un campo "username" separado). Las contraseñas se almacenan
 * encriptadas con BCrypt y se verifican automáticamente.</p>
 *
 * @see UserRepository
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Convierte un conjunto de roles de la aplicación a autorizaciones de Spring Security.
     *
     * <p>Cada {@link Rol} se convierte a un {@link SimpleGrantedAuthority} con el
     * prefijo {@code "ROLE_"} (ej. {@code ROLE_ADMIN}, {@code ROLE_RECEPCIONISTA}).</p>
     *
     * @param roles conjunto de roles del usuario
     * @return colección de autorizaciones de Spring Security
     */
    private Collection<? extends GrantedAuthority> mapToAuthorities(Set<Rol> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    /**
     * Carga un usuario por su dirección de correo electrónico.
     *
     * <p>Busca el usuario en la base de datos usando el email como identificador.
     * Si no lo encuentra, lanza {@link UsernameNotFoundException}. Retorna un
     * objeto {@link org.springframework.security.core.userdetails.User} con
     * las credenciales, roles y estado del usuario.</p>
     *
     * @param email dirección de correo electrónico del usuario
     * @return detalles del usuario para Spring Security
     * @throws UsernameNotFoundException si no se encuentra un usuario con el email proporcionado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User usuario = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new UsernameNotFoundException("Usuario no encontrado con email: " + email)
                );

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(), // 👈 LOGIN
                usuario.getPassword(),
                usuario.getActivo(),
                true,
                true,
                true,
                mapToAuthorities(usuario.getRoles())
        );
    }
}
