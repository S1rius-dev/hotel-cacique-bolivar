package com.hotelcaciquebolivar.reservas.service.Auth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hotelcaciquebolivar.reservas.config.security.UserDetailsServiceImpl;
import com.hotelcaciquebolivar.reservas.dto.auth.LoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.LoginResponse;
import com.hotelcaciquebolivar.reservas.dto.auth.GoogleLoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.RegisterRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.AuthProvider;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.exception.auth.InvalidCredentialsException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.AuthMapper;
import com.hotelcaciquebolivar.reservas.mapper.UserMapper;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.security.jwt.JwtService;
import org.springframework.context.ApplicationEventPublisher;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.ClientePayload;

/**
 * Implementación del servicio de autenticación y autorización del sistema hotelero.
 * Maneja el registro de usuarios, inicio de sesión con credenciales locales o Google,
 * obtención del usuario actual, carga de usuarios por email o username, y renovación
 * de tokens JWT.
 * 
 * <p>Esta clase integra con Spring Security para la autenticación, genera tokens JWT
 * para la autorización, y maneja la vinculación de cuentas existentes con Google.</p>
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.google.client-id}")
    private String googleClientId;

    /**
     * Constructor que inyecta todas las dependencias necesarias para el servicio de autenticación.
     *
     * @param userRepository repositorio de acceso a datos de usuarios
     * @param passwordEncoder codificador de contraseñas para encriptación
     * @param userDetailsService servicio de carga de detalles de usuario para Spring Security
     * @param jwtService servicio de generación y validación de tokens JWT
     * @param authenticationManager gestor de autenticación de Spring Security
     * @param userMapper mapper para conversión entre entidades y DTOs de usuario
     * @param authMapper mapper para conversión de datos de autenticación
     * @param eventPublisher publicador de eventos de dominio para notificaciones SSE
     */
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserDetailsServiceImpl userDetailsService, JwtService jwtService,
            AuthenticationManager authenticationManager, UserMapper userMapper,
            AuthMapper authMapper, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.authMapper = authMapper;
        this.eventPublisher = eventPublisher;
    }

    /* =====================================================
     *                     REGISTER
     * ===================================================== */
    /**
     * Registra un nuevo usuario en el sistema con las credenciales proporcionadas.
     * Valida que el email y contraseña no estén vacíos, verifica que el email no exista
     * previamente, crea el usuario con el rol especificado (por defecto CLIENTE), y genera
     * tokens JWT de acceso y renovación.
     * 
     * <p>Si el rol es CLIENTE, establece un username basado en el ID del usuario y
     * publica un evento de dominio para notificación SSE.</p>
     *
     * @param request objeto que contiene los datos necesarios para el registro del usuario
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     * @throws ValidationException si el email o contraseña están vacíos
     * @throws ResourceAlreadyExistsException si el email ya está registrado
     */
    @Override
    public LoginResponse register(RegisterRequest request) {

        if (request.email() == null || request.email().isBlank()) {
            throw new ValidationException("email", "es obligatorio");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("password", "es obligatoria");
        }

        userRepository.findByEmail(request.email())
                .ifPresent(u -> {
                    throw new ResourceAlreadyExistsException(
                            "Usuario",
                            "email",
                            request.email()
                    );
                });

        User user = authMapper.toUserFromRegister(request, passwordEncoder.encode(request.password()));
        user.setUsername(request.email());

        Rol rol = request.role() != null ? request.role() : Rol.CLIENTE;
        user.agregarRol(rol);

        User savedUser = userRepository.save(user);

        if (rol == Rol.CLIENTE) {
            savedUser.setUsername("cliente-" + savedUser.getId());
            userRepository.save(savedUser);

            eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CLIENTE_CREADO,
                new ClientePayload(
                    null,
                    request.email(),
                    null,
                    request.email(),
                    "REGISTRO")));
        }

        UserDetails userDetails
                = userDetailsService.loadUserByUsername(savedUser.getEmail());

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return authMapper.toAuthResponse(accessToken, rol.name(), refreshToken);
    }

    /* =====================================================
     *                      LOGIN
     * ===================================================== */
    /**
     * Inicia sesión en el sistema con las credenciales proporcionadas.
     * Valida que el email y contraseña no estén vacíos, autentica al usuario
     * mediante Spring Security, verifica que esté activo, actualiza la fecha
     * de último inicio de sesión y genera tokens JWT de acceso y renovación.
     *
     * @param request objeto que contiene el email y contraseña del usuario
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     * @throws ValidationException si el email o contraseña están vacíos
     * @throws InvalidCredentialsException si las credenciales son incorrectas o el usuario está inactivo
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        if (request.email() == null || request.password() == null) {
            throw new ValidationException(
                    "credenciales",
                    "email y password son obligatorios"
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

        // 🔥 Aquí ya sabes que es válido
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        user.validarActivo();
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails
                = userDetailsService.loadUserByUsername(request.email());

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        String role = user.getRoles()
                .stream()
                .findFirst()
                .map(Rol::name)
                .orElse("CLIENTE");

        return authMapper.toAuthResponse(accessToken, role, refreshToken);
    }

    /**
     * Obtiene la información del usuario actualmente autenticado.
     * Extrae el email del objeto de autenticación, busca el usuario en la base de datos
     * y verifica que esté activo antes de retornar su información.
     *
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return respuesta con los datos del usuario autenticado
     * @throws InvalidCredentialsException si el usuario no existe o está inactivo
     */
    @Override
    public UserResponse getCurrentUser(Authentication authentication) {

        String email = authentication.getName(); // 👈 viene del JWT

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        user.validarActivo();

        return userMapper.toAdminResponse(user);
    }

    /**
     * Carga los detalles de un usuario por su dirección de email.
     * Valida que el email no esté vacío y delega la carga al servicio de detalles de usuario.
     *
     * @param email dirección de email del usuario a buscar
     * @return detalles del usuario encontrado para Spring Security
     * @throws ValidationException si el email está vacío
     */
    @Override
    public UserDetails loadUserByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "es obligatorio");
        }

        return userDetailsService.loadUserByUsername(email);
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario (username).
     * Valida que el username no esté vacío y delega la carga al servicio de detalles de usuario.
     * En este sistema, el username corresponde al email del usuario.
     *
     * @param username nombre de usuario del usuario a buscar
     * @return detalles del usuario encontrado para Spring Security
     * @throws ValidationException si el username está vacío
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "es obligatorio");
        }

        // En tu caso username = email
        return userDetailsService.loadUserByUsername(username);
    }

    /**
     * Renueva el token JWT de un usuario autenticado actualmente.
     * Valida que la autenticación sea válida, busca el usuario en la base de datos,
     * verifica que esté activo y genera nuevos tokens JWT de acceso y renovación.
     *
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return respuesta de autenticación con el nuevo token JWT generado
     * @throws InvalidCredentialsException si la autenticación no es válida o el usuario está inactivo
     */
    @Override
    public LoginResponse refresh(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Intento de refresh sin autenticación válida");
            throw new InvalidCredentialsException("Sesión expirada, debe iniciar sesión nuevamente");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        user.validarActivo();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        String role = user.getRoles()
                .stream()
                .findFirst()
                .map(Rol::name)
                .orElse("CLIENTE");

        return authMapper.toAuthResponse(accessToken, role, refreshToken);
    }

    /**
     * Inicia sesión o registra un usuario utilizando autenticación con Google.
     * Verifica el token de ID de Google, busca si el usuario existe por provider ID o email,
     * crea uno nuevo si no existe, vincula cuentas existentes con Google, y genera tokens JWT.
     * 
     * <p>Si el usuario ya existe con el mismo email pero sin provider ID de Google,
     * lo vincula automáticamente. Si no existe, crea un nuevo usuario con el rol CLIENTE.</p>
     *
     * @param request objeto que contiene el token de ID de Google para autenticación
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     * @throws InvalidCredentialsException si el token de Google es inválido o hay errores de configuración
     */
    @Override
    public LoginResponse googleLogin(GoogleLoginRequest request) {

        try {
            if (googleClientId == null || googleClientId.isBlank()) {
                log.error("GOOGLE_CLIENT_ID no está configurado en las variables de entorno");
                throw new InvalidCredentialsException("Error de configuración: Google Client ID no definido");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            log.debug("Verificando token de Google con client ID: {}", googleClientId);

            GoogleIdToken idToken = verifier.verify(request.idToken());

            if (idToken == null) {
                log.warn("Token de Google inválido o audiencia no coincide. Client ID configurado: {}", googleClientId);
                throw new InvalidCredentialsException("Token de Google inválido o expirado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            log.info("Login Google exitoso para email: {}", email);

            User user = userRepository.findByProviderId(googleId)
                    .orElseGet(() -> userRepository.findByEmail(email)
                    .map(existing -> {

                        if (existing.getProviderId() == null) {
                            existing.setProviderId(googleId);
                            log.info("Cuenta existente vinculada con Google: {}", email);
                        }

                        return existing;
                    })
                    .orElseGet(() -> {

                        User nuevo = new User();
                        nuevo.setEmail(email);
                        nuevo.setUsername(name != null && !name.isBlank() ? name : email);

                        // contraseña aleatoria porque Google autentica
                        nuevo.setPassword(
                                passwordEncoder.encode(UUID.randomUUID().toString())
                        );

                        nuevo.setProvider(AuthProvider.GOOGLE);
                        nuevo.setProviderId(googleId);

                        nuevo.setActivo(true);
                        nuevo.agregarRol(Rol.CLIENTE);

                        log.info("Nuevo usuario creado vía Google: {}", email);

                        return userRepository.save(nuevo);
                    }));

            if (name != null && !name.isBlank() && user.getProvider() == AuthProvider.GOOGLE) {
                user.setUsername(name);
            }
            user.setProfilePicture(pictureUrl);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            String role = user.getRoles().stream()
                    .findFirst()
                    .map(Rol::name)
                    .orElse("CLIENTE");

            return authMapper.toAuthResponse(accessToken, role, refreshToken);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Error al verificar token de Google: {}", e.getMessage(), e);
            throw new InvalidCredentialsException("Error al verificar el token de Google");
        }
    }
}
