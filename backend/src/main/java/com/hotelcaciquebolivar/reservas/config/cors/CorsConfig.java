package com.hotelcaciquebolivar.reservas.config.cors;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de políticas CORS (Cross-Origin Resource Sharing).
 *
 * <p>Define las reglas de acceso que permiten al frontend realizar peticiones
 * HTTP al backend desde diferentes orígenes. Implementa {@link WebMvcConfigurer}
 * y expone un bean {@link CorsConfigurationSource} para que Spring Security
 * lo utilice en la cadena de filtros de seguridad.</p>
 *
 * <p>Los orígenes se leen de la propiedad {@code app.cors.allowed-origins}
 * (variable de entorno {@code CORS_ALLOWED_ORIGINS}), separados por coma. El valor
 * por defecto es solo el frontend local de Vite, de modo que el repositorio no
 * exponga direcciones privadas ni dominios de producción.</p>
 *
 * @see org.springframework.web.cors.CorsConfiguration
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Orígenes permitidos, separados por coma.
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Crea y configura la fuente de configuración CORS para la aplicación.
     *
     * <p>Permite métodos HTTP (GET, POST, PUT, DELETE, PATCH, OPTIONS),
     * cabeceras de autenticación y contenido, y credenciales (cookies).
     * La configuración se aplica a todas las rutas ({@code /**}).</p>
     *
     * @return fuente de configuración CORS aplicable a todas las peticiones
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cookie"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
