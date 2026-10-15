package com.hotelcaciquebolivar.reservas.config.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

import jakarta.annotation.PostConstruct;

/**
 * Inicializador del usuario administrador del sistema.
 *
 * <p>Ejecuta al construirse ({@code @PostConstruct}) y crea el usuario administrador
 * por defecto si no existe aún. Lee las credenciales de las variables de entorno
 * {@code ADMIN_EMAIL}, {@code ADMIN_USERNAME} y {@code ADMIN_PASSWORD}.</p>
 *
 * <p>Si el usuario ya existe (por email), se omite la creación. La contraseña se
 * almacena encriptada con {@link PasswordEncoder}. El usuario se crea con el rol
 * {@link Rol#ADMIN} y estado activo.</p>
 *
 * @see UserRepository
 * @see PasswordEncoder
 */
@Component
public class AdminDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    public AdminDataInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Método de inicialización ejecutado después de la construcción del bean.
     *
     * <p>Registra el inicio y fin del proceso de carga del administrador.
     * Captura excepciones para evitar que la aplicación falle si hay un
     * problema con la inicialización del administrador.</p>
     */
    @PostConstruct
    public void init() {

        log.info("[INIT][ADMIN] Iniciando carga de administrador");

        try {
            crearAdmin();
            log.info("[INIT][ADMIN] Proceso finalizado");

        } catch (Exception e) {
            log.error("[INIT][ADMIN] Error al inicializar administrador", e);
        }
    }

    /**
     * Crea el usuario administrador por defecto si no existe.
     *
     * <p>Valida que las variables de entorno {@code ADMIN_EMAIL}, {@code ADMIN_USERNAME}
     * y {@code ADMIN_PASSWORD} no estén vacías. Si el email ya está registrado en
     * la base de datos, omite la creación. Crea un nuevo {@link User} con:</p>
     * <ul>
     *   <li>Email, username y contraseña (encriptada) desde variables de entorno</li>
     *   <li>Rol {@link Rol#ADMIN}</li>
     *   <li>Estado activo</li>
     * </ul>
     *
     * @see UserRepository#findByEmail(String)
     * @see UserRepository#save(User)
     */
    private void crearAdmin() {

        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("[INIT][ADMIN] ADMIN_EMAIL vacío");
            return;
        }

        if (adminUsername == null || adminUsername.isBlank()) {
            log.warn("[INIT][ADMIN] ADMIN_USERNAME vacío");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("[INIT][ADMIN] ADMIN_PASSWORD vacío");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("[INIT][ADMIN] Usuario administrador ya existe");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.agregarRol(Rol.ADMIN);
        admin.setActivo(true);

        userRepository.save(admin);

        log.info("[INIT][ADMIN] Administrador creado correctamente");
    }

    // ==================================================
    // TIPOS DE HABITACIÓN
    // ==================================================
}
