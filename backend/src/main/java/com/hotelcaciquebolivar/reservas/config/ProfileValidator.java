package com.hotelcaciquebolivar.reservas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * Validador de perfiles de Spring activos al arrancar la aplicación.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Ordered#HIGHEST_PRECEDENCE + 1}
 * para ejecutarse justo después de {@link ProdEnvironmentValidator}. Valida que
 * exista al menos un perfil de Spring activo y que todos los perfiles sean válidos.</p>
 *
 * <p>Perfiles válidos: {@code dev}, {@code test}, {@code prod}.</p>
 *
 * <p>Comportamiento:</p>
 * <ul>
 *   <li>Si no hay perfiles activos pero la propiedad {@code spring.profiles.active}
 *       contiene un valor válido, acepta la configuración (tolerancia a démora de
 *       inicialización de Spring Boot).</li>
 *   <li>Si no hay perfil válido, aborta el arranque con un error descriptivo.</li>
 *   <li>Si un perfil no está en la lista de válidos, aborta el arranque.</li>
 * </ul>
 *
 * @see ProfileEnvironmentPostProcessor
 * @see ProdEnvironmentValidator
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ProfileValidator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProfileValidator.class);
    private static final Set<String> VALID = Set.of("dev", "test", "prod");

    private final Environment env;

    public ProfileValidator(Environment env) {
        this.env = env;
    }

    /**
     * Ejecuta la validación de perfiles de Spring al arrancar la aplicación.
     *
     * <p>Verifica que los perfiles activos sean válidos y registra un log informativo
     * con los perfiles detectados. Si no hay perfiles activos, intenta detectar el
     * perfil desde las propiedades del sistema antes de fallar.</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     * @throws IllegalStateException si no hay perfil activo válido o si algún perfil
     *         no está en el conjunto de perfiles permitidos
     */
    @Override
    public void run(String... args) {
        String[] active = env.getActiveProfiles();
        String springActive = env.getProperty("spring.profiles.active");
        String envActive = env.getProperty("SPRING_PROFILES_ACTIVE");
        // Spring Boot mapea SPRING_PROFILES_ACTIVE -> spring.profiles.active vía relaxed binding,
        // pero en algunos arranques (run-dev.ps1 desde hotel-app/) active puede quedar vacío un instante.
        // Si la propiedad existe y es válida, no fallar - el perfil se activará.
        if (active.length == 0) {
            String candidate = (springActive != null && !springActive.isBlank()) ? springActive.trim()
                    : (envActive != null && !envActive.isBlank()) ? envActive.trim() : null;
            if (candidate != null && VALID.contains(candidate)) {
                log.info("[CONFIG] Perfil activo por propiedad: {} (activeProfiles vacío pero SPRING_PROFILES_ACTIVE/spring.profiles.active presente)", candidate);
                return;
            }
            String msg = "[CONFIG][FAIL-FAST] No hay perfil activo. Debes establecer SPRING_PROFILES_ACTIVE=dev|test|prod. Ej: $env:SPRING_PROFILES_ACTIVE=\"dev\"; ./mvnw spring-boot:run o ./run-dev.ps1";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        for (String p : active) {
            if (!VALID.contains(p)) {
                String msg = "[CONFIG][FAIL-FAST] Perfil no válido: " + p + ". Válidos: " + VALID + ". Revisa SPRING_PROFILES_ACTIVE y .env";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        log.info("[CONFIG] Perfil activo: {}", Arrays.toString(active));
    }
}
