package com.hotelcaciquebolivar.reservas.config;

import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Procesador de entorno que valida y asegura que exista un perfil de Spring activo.
 *
 * <p>Ejecutado en {@link Ordered#LOWEST_PRECEDENCE} para que otros procesadores
 * de entorno puedan configurar el perfil antes de esta validación. Implementa
 * {@link EnvironmentPostProcessor} y se registra en
 * {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports}.</p>
 *
 * <p>Comportamiento:</p>
 * <ul>
 *   <li>Si no hay perfil activo y no se detecta entorno de pruebas, lanza
 *       {@link IllegalStateException} con instrucciones de configuración.</li>
 *   <li>Si hay un perfil activo no válido, también lanza excepción.</li>
 *   <li>Perfiles válidos: {@code dev}, {@code test}, {@code prod}.</li>
 *   <li>En ejecuciones de tests (Surefire, SpringBootTest, ActiveProfiles) permite
 *       la ausencia de perfil activo para evitar falsos positivos.</li>
 * </ul>
 *
 * @see ProfileValidator
 * @see ProdEnvironmentValidator
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class ProfileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Set<String> VALID = Set.of("dev", "test", "prod");

    /**
     * Procesa el entorno de la aplicación para validar la presencia de un perfil activo.
     *
     * <p>Verifica múltiples fuentes de configuración de perfil (propiedades de Spring,
     * variables de entorno) y valida que el perfil indicado sea uno de los permitidos.
     * Si no se detecta un perfil válido y no es una ejecución de prueba, aborta el
     * arranque con un error descriptivo.</p>
     *
     * @param environment entorno configurable de la aplicación
     * @param application aplicación Spring Boot en proceso de arranque
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] active = environment.getActiveProfiles();
        if (active.length == 0) {
            String springActive = environment.getProperty("spring.profiles.active");
            String envActive = environment.getProperty("SPRING_PROFILES_ACTIVE");
            // .env también puede aportar spring.profiles.active en minúsculas
            String lowerActive = environment.getProperty("spring.profiles.active");
            if ((springActive == null || springActive.isBlank())
                    && (envActive == null || envActive.isBlank())
                    && (lowerActive == null || lowerActive.isBlank())) {
                if (isTestRun(environment)) {
                    return;
                }
                throw new IllegalStateException(
                        "[CONFIG][FAIL-FAST] No hay perfil activo. Debes establecer SPRING_PROFILES_ACTIVE=dev|test|prod. "
                                + "Ej: $env:SPRING_PROFILES_ACTIVE=\"dev\"; ./mvnw spring-boot:run o ./run-dev.ps1 | "
                                + "Perfiles válidos: " + VALID);
            }
        }
        for (String p : active) {
            if (!VALID.contains(p)) {
                throw new IllegalStateException(
                        "[CONFIG][FAIL-FAST] Perfil no válido: " + p + ". Válidos: " + VALID);
            }
        }
    }

    /**
     * Determina si la ejecución actual corresponde a un entorno de pruebas.
     *
     * <p>Analiza la pila de llamadas para detectar clases de testing (Surefire,
     * SpringBootTest, ActiveProfiles) y también verifica la propiedad del sistema
     * {@code spring.profiles.active} y la variable de entorno {@code SPRING_PROFILES_ACTIVE}.</p>
     *
     * @param env entorno de configuración actual
     * @return {@code true} si se detecta que es una ejecución de pruebas
     */
    private boolean isTestRun(ConfigurableEnvironment env) {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            String cn = e.getClassName();
            if (cn.contains("surefire") || cn.contains("SpringBootTest") || cn.contains("ActiveProfiles")) {
                return true;
            }
        }
        String sys = System.getProperty("spring.profiles.active");
        if ("test".equals(sys) || "test".equals(env.getProperty("spring.profiles.active"))) {
            return true;
        }
        String envVar = env.getProperty("SPRING_PROFILES_ACTIVE");
        return "test".equals(envVar);
    }
}
