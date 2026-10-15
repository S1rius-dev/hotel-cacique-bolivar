package com.hotelcaciquebolivar.reservas.config.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import tools.jackson.databind.ObjectMapper;
import com.hotelcaciquebolivar.reservas.security.jwt.JwtAuthenticationEntryPoint;
import com.hotelcaciquebolivar.reservas.security.jwt.JwtAuthenticationFilter;
import com.hotelcaciquebolivar.reservas.security.jwt.JwtService;
import com.hotelcaciquebolivar.reservas.security.ratelimit.RateLimitFilter;

/**
 * Configuración principal de seguridad de la aplicación.
 *
 * <p>Define la cadena de filtros de seguridad Spring Security, las reglas de
 * autorización por ruta, la autenticación basada en JWT y los beans de
 * seguridad相关. Implementa un modelo stateless (sin sesiones HTTP) donde
 * la autenticación se gestiona mediante tokens JWT en cookies.</p>
 *
 * <p>Componentes configurados:</p>
 * <ul>
 *   <li>{@link SecurityFilterChain} — cadena de filtros con reglas de acceso</li>
 *   <li>{@link JwtAuthenticationFilter} — filtro de autenticación JWT</li>
 *   <li>{@link PasswordEncoder} — codificador de contraseñas BCrypt</li>
 *   <li>{@link AuthenticationManager} — gestor de autenticación</li>
 *   <li>{@link AuthenticationProvider} — proveedor de autenticación DAO</li>
 *   <li>{@link AccessDeniedHandler} — manejador personalizado de errores 403</li>
 * </ul>
 *
 * <p>Políticas de seguridad:</p>
 * <ul>
 *   <li>CORS habilitado para el frontend</li>
 *   <li>CSRF deshabilitado (API stateless)</li>
 *   <li>Sesiones deshabilitadas (STATELESS)</li>
 *   <li>Rate limiting habilitado</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter
 * @see RateLimitFilter
 * @see UserDetailsServiceImpl
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsServiceImpl customUserDetailsService;
    private final JwtService jwtService;
    private final RateLimitFilter rateLimitFilter;
    private final ObjectMapper objectMapper;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param jwtAuthenticationEntryPoint manejador de errores de autenticación JWT
     * @param customUserDetailsService servicio de carga de usuarios personalizado
     * @param jwtService servicio de generación y validación de tokens JWT
     * @param rateLimitFilter filtro de limitación de tasa de peticiones
     * @param objectMapper serializador JSON para respuestas de error
     */
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            UserDetailsServiceImpl customUserDetailsService,
            JwtService jwtService,
            RateLimitFilter rateLimitFilter,
            ObjectMapper objectMapper) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.rateLimitFilter = rateLimitFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * Crea el filtro de autenticación JWT.
     *
     * <p>Filtro que intercepta las peticiones HTTP para extraer y validar
     * el token JWT del cookie o cabecera {@code Authorization}. Se coloca
     * antes de {@link UsernamePasswordAuthenticationFilter} en la cadena.</p>
     *
     * @return filtro de autenticación JWT configurado
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, customUserDetailsService);
    }

    /**
     * Proporciona el gestor de autenticación de la aplicación.
     *
     * @param config configuración de autenticación de Spring Security
     * @return gestor de autenticación
     * @throws Exception si hay error al obtener el gestor
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Crea el codificador de contraseñas basado en BCrypt.
     *
     * @return codificador de contraseñas BCrypt
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Crea el proveedor de autenticación DAO.
     *
     * <p>Utiliza {@link UserDetailsServiceImpl} para cargar usuarios y
     * {@link BCryptPasswordEncoder} para verificar contraseñas.</p>
     *
     * @return proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Crea el manejador personalizado de errores de acceso denegado (403).
     *
     * <p>Retorna una respuesta JSON con el error "FORBIDDEN", el mensaje
     * "No tienes permisos para acceder a este recurso.", la URI solicitada
     * y un timestamp. Evita respuestas duplicadas verificando si la respuesta
     * ya fue enviada ({@code isCommitted}).</p>
     *
     * @return manejador de acceso denegado
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            if (!response.isCommitted()) {
                response.setStatus(403);
                response.setContentType("application/json");
                Map<String, Object> body = new HashMap<>();
                body.put("error", "FORBIDDEN");
                body.put("message", "No tienes permisos para acceder a este recurso.");
                body.put("path", request.getRequestURI());
                body.put("timestamp", System.currentTimeMillis());
                response.getWriter().write(objectMapper.writeValueAsString(body));
            }
        };
    }

    /**
     * Configura y construye la cadena de filtros de seguridad principal.
     *
     * <p>Define las reglas de autorización por ruta, la configuración CORS,
     * el manejo de excepciones JWT, y los filtros personalizados. Las reglas
     * de acceso se organizan en niveles: público, autenticado, staff y admin.</p>
     *
     * @param http configuración HTTP de Spring Security
     * @param corsConfigurationSource fuente de configuración CORS
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
                // 🌐 CORS para frontend con cookies
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // ⚠️ CSRF: decisión importante (explico abajo)
                .csrf(csrf -> csrf.disable())
                // 🔐 Stateless (JWT no usa sesión)
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 🚨 Manejo de errores 401 y 403
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler())
                )
                 // 🔑 reglas de acceso
                .authorizeHttpRequests(auth -> auth
                .withObjectPostProcessor(new ObjectPostProcessor<AuthorizationFilter>() {
                    @Override
                    public <O extends AuthorizationFilter> O postProcess(O filter) {
                        filter.setFilterAsyncDispatch(false);
                        return filter;
                    }
                })
                // === PÚBLICO ===
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/auth/google",
                        "/api/auth/forgot-password",
                        "/api/auth/verify-reset-code",
                        "/api/auth/reset-password",
                        "/api/clientes/search",
                        "/api/catalogos/tipos-identificacion",
                        "/api/catalogos/municipios",
                        "/api/catalogos/municipios/buscar",
                         "/api/reservas/disponibilidad",
                        "/api/webhooks/**",
                        "/api/public/checkout",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/resenas/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(
                        "/api/pqrs/codigo/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pqrs").permitAll()
                // === CLIENTE (cualquier authenticated) ===
                .requestMatchers(
                        "/api/auth/logout",
                        "/api/auth/me",
                        "/api/auth/completar-perfil",
                        "/api/auth/vincular-cliente/**"
                ).authenticated()
                // === MIS RESERVAS — CLIENTE autenticado (antes del bloque staff) ===
                .requestMatchers("/api/reservas/mis-reservas/**").authenticated()
                // === STAFF (ADMIN + RECEPCIONISTA) ===
                .requestMatchers(
                        "/api/reservas/**",
                        "/api/habitaciones/**",
                        "/api/tipos-habitacion/**",
                        "/api/clientes/**",
                        "/api/pagos/**",
                        "/api/caja/**",
                        "/api/alertas/**",
                        "/api/pqrs/**",
                        "/api/dashboard/**",
                        "/api/registros/**",
                        "/api/sse/subscribe",
                        "/api/catalogos/roles"
                ).hasAnyRole("ADMIN", "RECEPCIONISTA")
                // === ADMIN ONLY ===
                .requestMatchers(
                        "/api/users/**",
                        "/api/empleados/**",
                        "/api/audit-logs/**",
                        "/api/backups/**",
                        "/admin/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated()
                )
                // 🔥 TU FILTRO JWT (ya adaptado a cookies)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
