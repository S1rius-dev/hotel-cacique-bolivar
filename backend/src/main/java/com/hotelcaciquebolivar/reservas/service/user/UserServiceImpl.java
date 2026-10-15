package com.hotelcaciquebolivar.reservas.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.user.UserRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.dto.user.UserUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.AuthProvider;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.mapper.UserMapper;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;

/**
 * Implementación del servicio de gestión de usuarios del sistema.
 * Administra la creación, consulta, actualización y eliminación de usuarios
 * con encriptación de contraseñas, validación de unicidad de email,
 * asignación de roles y registro de acciones de auditoría.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;
    private final AuditService auditService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            PageMapper pageMapper,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.pageMapper = pageMapper;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Valida campos obligatorios (email, password, roles), verifica que el email no exista,
     * encripta la contraseña y establece el proveedor como LOCAL.
     */
    /* =====================================================
     * CREAR USUARIO
     * ===================================================== */
    @Override
    public UserResponse crearUsuario(UserRequest dto) {

        validarCreacion(dto);

        User user = userMapper.toEntity(dto);

        // 🔐 Siempre encriptar
        user.setProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(dto.password()));

        // 🔒 Roles controlados
        if (dto.roles() != null) {
            user.getRoles().clear();
            user.getRoles().addAll(dto.roles());
        }

        User guardado = userRepository.save(user);

        auditService.log(AuditAction.CREATE, "User", guardado.getId(),
                String.format("Usuario creado — #%d\nEmail: %s\nUsername: %s\nRoles: %s",
                        guardado.getId(), guardado.getEmail(), guardado.getUsername(), guardado.getRoles()));

        return userMapper.toAdminResponse(guardado);
    }

    /** {@inheritDoc} */
    /* =====================================================
     * BUSCAR POR ID
     * ===================================================== */
    @Override
    @Transactional(readOnly = true)
    public UserResponse buscarPorId(Long id) {
        return userMapper.toAdminResponse(obtenerUsuario(id));
    }

    /** {@inheritDoc} */
    /* =====================================================
     * LISTAR
     * ===================================================== */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listarUsuarios(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, userMapper::toAdminResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Valida que el usuario esté activo, que el email no esté en uso por otro usuario,
     * encripta la contraseña solo si se proporciona y actualiza roles y estado.
     */
    /* =====================================================
     * ACTUALIZAR
     * ===================================================== */
    @Override
    public UserResponse actualizarUsuario(Long id, UserUpdateRequest dto) {

        User user = obtenerUsuario(id);

        user.validarActivo();
        user.validarCambioEmail(dto.email());

        userMapper.updateFromDto(dto, user);

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.roles() != null) {
            user.getRoles().clear();
            user.getRoles().addAll(dto.roles());
        }

        if (dto.activo() != null) {
            user.setActivo(dto.activo());
        }

        User actualizado = userRepository.save(user);

        auditService.log(AuditAction.UPDATE, "User", actualizado.getId(),
                String.format("Usuario actualizado — #%d\nEmail: %s\nUsername: %s\nRoles: %s\nActivo: %s",
                        actualizado.getId(), actualizado.getEmail(), actualizado.getUsername(),
                        actualizado.getRoles(), actualizado.getActivo()));

        return userMapper.toAdminResponse(actualizado);
    }

    /** {@inheritDoc} */
    /* =====================================================
     * ELIMINAR
     * ===================================================== */
    @Override
    public void eliminarUsuario(Long id) {

        User user = obtenerUsuario(id);

        auditService.log(AuditAction.DELETE, "User", id,
                String.format("Usuario eliminado — #%d\nEmail: %s\nUsername: %s\nRoles: %s",
                        id, user.getEmail(), user.getUsername(), user.getRoles()));

        userRepository.delete(user);
    }

    /**
     * Obtiene un usuario por su identificador o lanza excepción si no existe.
     *
     * @param id identificador del usuario
     * @return la entidad User
     * @throws ResourceNotFoundException si no se encuentra el usuario
     */
    /* =====================================================
     * PRIVADOS
     * ===================================================== */
    private User obtenerUsuario(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Usuario", id)
                );
    }

    /**
     * Valida los datos obligatorios para la creación de un usuario.
     * Verifica que email no esté vacío, password no esté vacío,
     * que se asigne al menos un rol y que el email no exista en el sistema.
     *
     * @param dto datos a validar
     * @throws ValidationException              si faltan campos obligatorios
     * @throws ResourceAlreadyExistsException   si el email ya está registrado
     */
    private void validarCreacion(UserRequest dto) {

        if (dto.email() == null || dto.email().isBlank()) {
            throw new ValidationException("email", "es obligatorio");
        }

        if (dto.password() == null || dto.password().isBlank()) {
            throw new ValidationException("password", "es obligatoria");
        }

        if (dto.roles() == null || dto.roles().isEmpty()) {
            throw new ValidationException(
                    "roles",
                    "debe asignar al menos un rol"
            );
        }

        userRepository.findByEmail(dto.email())
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "Usuario",
                            "email",
                            dto.email()
                    );
                });
    }

}
