package com.hotelcaciquebolivar.reservas.security.ratelimit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelcaciquebolivar.reservas.util.RateLimiter;
import com.hotelcaciquebolivar.reservas.util.RequestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de rate limiting que controla la tasa de solicitudes HTTP entrantes.
 * 
 * Esta clase extiende {@link OncePerRequestFilter} y aplica límites de tasa
 * basados en reglas predefinidas para diferentes endpoints de la API.
 * Utiliza un {@link RateLimiter} para rastrear y controlar las solicitudes.
 * 
 * <p>El filtro implementa las siguientes funcionalidades:</p>
 * <ul>
 *   <li>Configuración de reglas de rate limiting por endpoint</li>
 *   <li>Generación de claves de rate limiting basadas en IP, usuario o ambos</li>
 *   <li>Establecimiento de headers de respuesta (X-RateLimit-*)</li>
 *   <li>Generación de respuestas HTTP 429 (Too Many Requests) con información de reintento</li>
 *   <li>Exclusión de endpoints no protegidos (Swagger, Actuator, SSE)</li>
 * </ul>
 * 
 * <p>Las reglas de rate limiting se definen en el método {@link #buildRules()} y
 * especifican el método HTTP, patrón de URL, límite de solicitudes y tipo de clave
 * (IP, usuario o ambos).</p>
 * 
 * <p>La ventana de tiempo para el rate limiting es de 60 segundos (1 minuto).</p>
 * 
 * @author Hotel Cacique Bolívar
 * @version 1.0
 * @see RateLimiter
 * @see RateLimitExceededException
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long WINDOW_MS = 60_000L;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<RateLimitRule> rules;

    /**
     * Registro que define una regla de rate limiting para un endpoint específico.
     * 
     * @param method Método HTTP al que aplica la regla (GET, POST, etc.)
     * @param pattern Patrón URL que identifica el endpoint (soporta wildcards)
     * @param limit Número máximo de solicitudes permitidas en la ventana de tiempo
     * @param keyType Tipo de clave para rate limiting: "IP", "USER" o "IP_AND_USER"
     */
    public record RateLimitRule(String method, String pattern, int limit, String keyType) {}

    /**
     * Constructor que inyecta el RateLimiter y construye las reglas de rate limiting.
     * 
     * @param rateLimiter Instancia de RateLimiter utilizada para rastrear solicitudes
     */
    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.rules = buildRules();
    }

    /**
     * Construye y retorna la lista de reglas de rate limiting para los endpoints de la API.
     * 
     * <p>Cada regla define:</p>
     * <ul>
     *   <li>El método HTTP (GET, POST, etc.)</li>
     *   <li>El patrón URL del endpoint</li>
     *   <li>El número máximo de solicitudes permitidas por ventana de 60 segundos</li>
     *   <li>El tipo de clave (IP, USER o IP_AND_USER) para rastrear solicitudes</li>
     * </ul>
     * 
     * @return Lista de reglas de rate limiting configuradas
     */
    private List<RateLimitRule> buildRules() {
        return List.of(
                new RateLimitRule("POST", "/api/auth/login", 5, "IP"),
                new RateLimitRule("POST", "/api/auth/register", 3, "IP"),
                new RateLimitRule("POST", "/api/auth/google", 10, "IP"),
                new RateLimitRule("POST", "/api/auth/refresh", 20, "IP_AND_USER"),
                new RateLimitRule("POST", "/api/pagos", 10, "USER"),
                new RateLimitRule("POST", "/api/pagos/*/reembolsar", 5, "USER"),
                new RateLimitRule("POST", "/api/reservas", 15, "USER"),
                new RateLimitRule("POST", "/api/reservas/rapida", 15, "USER"),
                new RateLimitRule("POST", "/api/reservas/*/check-in", 20, "USER"),
                new RateLimitRule("POST", "/api/reservas/*/check-out", 20, "USER"),
                new RateLimitRule("POST", "/api/reservas/*/detalles/*/check-in", 20, "USER"),
                new RateLimitRule("POST", "/api/reservas/*/detalles/*/check-out", 20, "USER"),
                new RateLimitRule("POST", "/api/caja/turno/abrir", 5, "USER"),
                new RateLimitRule("POST", "/api/caja/turno/cerrar", 5, "USER"),
                new RateLimitRule("POST", "/api/caja/gastos", 15, "USER"),
                new RateLimitRule("GET", "/api/clientes/buscar", 30, "USER"),
                new RateLimitRule("GET", "/api/clientes/search", 30, "USER"),
                new RateLimitRule("GET", "/api/reservas/disponibilidad", 40, "USER"),
                new RateLimitRule("GET", "/api/dashboard/**", 60, "USER")
        );
    }

    /**
     * Procesa cada solicitud HTTP para aplicar rate limiting según las reglas configuradas.
     * 
     * <p>El proceso de filtrado sigue estos pasos:</p>
     * <ol>
     *   <li>Identificar si la solicitud coincide con alguna regla de rate limiting</li>
     *   <li>Si no coincide, permitir el paso sin restricciones</li>
     *   <li>Generar la clave de rate limiting según el tipo de regla</li>
     *   <li>Verificar si la solicitud está dentro del límite permitido</li>
     *   <li>Establecer headers de respuesta con información de rate limiting</li>
     *   <li>Si excede el límite, retornar HTTP 429 con información de reintento</li>
     * </ol>
     * 
     * @param request Solicitud HTTP entrante
     * @param response Respuesta HTTP a enviar
     * @param filterChain Cadena de filtros a continuación
     * @throws ServletException Si ocurre un error en el procesamiento del servlet
     * @throws IOException Si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        RateLimitRule matched = null;
        for (RateLimitRule rule : rules) {
            if (rule.method().equalsIgnoreCase(method) && matcher.match(rule.pattern(), uri)) {
                matched = rule;
                break;
            }
        }

        if (matched == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = buildKey(request, matched);
        int limit = matched.limit();

        boolean allowed = rateLimiter.tryConsume(key, limit, WINDOW_MS);
        long remaining = rateLimiter.getRemainingRequests(key, limit, WINDOW_MS);
        long resetTime = rateLimiter.getResetTime(key, WINDOW_MS);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));

        if (!allowed) {
            int retryAfter = (int) Math.ceil((resetTime - System.currentTimeMillis()) / 1000.0);
            if (retryAfter < 1) retryAfter = 1;
            log.warn("Rate limit exceeded: {} {} — key: {} (remaining: {}, resetAt: {})",
                    method, uri, key, remaining, resetTime);

            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = Map.of(
                    "status", 429,
                    "error", "TOO_MANY_REQUESTS",
                    "message", "Demasiadas solicitudes. Intente de nuevo en " + retryAfter + " segundos.",
                    "retryAfter", retryAfter,
                    "path", request.getRequestURI(),
                    "timestamp", System.currentTimeMillis()
            );

            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Genera la clave única de rate limiting para una solicitud específica.
     * 
     * <p>La clave se genera según el tipo de regla:</p>
     * <ul>
     *   <li><strong>IP</strong>: Clave basada en la dirección IP del cliente</li>
     *   <li><strong>USER</strong>: Clave basada en el email del usuario autenticado,
     *       o IP si no hay usuario autenticado</li>
     *   <li><strong>IP_AND_USER</strong>: Clave basada en el email del usuario,
     *       o IP si no hay usuario autenticado</li>
     * </ul>
     * 
     * @param request Solicitud HTTP de la cual se extrae la IP y usuario
     * @param rule Regla de rate limiting que se está aplicando
     * @return Clave única para identificar la solicitud en el rate limiter
     */
    private String buildKey(HttpServletRequest request, RateLimitRule rule) {
        String ip = RequestUtils.getClientIp(request);
        String user = getUserEmail();

        return switch (rule.keyType()) {
            case "IP" -> "rl:ip:" + rule.pattern() + ":" + ip;
            case "USER" -> {
                if (user != null) yield "rl:user:" + rule.pattern() + ":" + user;
                yield "rl:ip:" + rule.pattern() + ":" + ip;
            }
            case "IP_AND_USER" -> {
                if (user != null) yield "rl:user:" + rule.pattern() + ":" + user;
                yield "rl:ip:" + rule.pattern() + ":" + ip;
            }
            default -> "rl:ip:" + rule.pattern() + ":" + ip;
        };
    }

    /**
     * Obtiene el email del usuario autenticado actualmente.
     * 
     * <p>Verifica el contexto de seguridad para obtener el nombre de usuario
     * (que en este sistema es el email) del usuario autenticado. Retorna null
     * si no hay usuario autenticado o si es el usuario anónimo.</p>
     * 
     * @return Email del usuario autenticado, o {@code null} si no hay usuario autenticado
     */
    private String getUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    /**
     * Determina si una solicitud debe ser excluida del rate limiting.
     * 
     * <p>Las siguientes rutas están exentas de rate limiting:</p>
     * <ul>
     *   <li>{@code /swagger-ui*} - Interfaz de documentación Swagger</li>
     *   <li>{@code /v3/api-docs*} - Documentación OpenAPI</li>
     *   <li>{@code /actuator/health} - Endpoint de salud de Spring Boot</li>
     *   <li>{@code /api/sse/subscribe} - Endpoint de Server-Sent Events</li>
     * </ul>
     * 
     * @param request Solicitud HTTP a evaluar
     * @return {@code true} si la solicitud debe ser excluida del rate limiting, {@code false} en caso contrario
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/actuator/health")
                || path.equals("/api/sse/subscribe");
    }
}
