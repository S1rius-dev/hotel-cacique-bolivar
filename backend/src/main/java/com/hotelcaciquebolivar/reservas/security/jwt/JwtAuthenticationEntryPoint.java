package com.hotelcaciquebolivar.reservas.security.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Punto de entrada de autenticación JWT que maneja solicitudes no autorizadas.
 * 
 * Esta clase implementa {@link AuthenticationEntryPoint} de Spring Security y se encarga
 * de gestionar las respuestas cuando un usuario intenta acceder a un recurso protegido
 * sin las credenciales válidas o cuando su token JWT ha expirado.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Establece el código de estado HTTP 401 (Unauthorized)</li>
 *   <li>Limpia las cookies JWTexistentes (jwt, token) para invalidar la sesión</li>
 *   <li>Establece el header {@code X-Session-Expired: true} para que el frontend detecte la expiración</li>
 *   <li>Retorna un cuerpo de respuesta JSON con información del error</li>
 * </ul>
 * 
 * <p>Esta clase es componente del módulo de seguridad y se integra con el filtro
 * {@link JwtAuthenticationFilter} para manejar toda la autenticación basada en JWT.</p>
 * 
 * @author Hotel Cacique Bolívar
 * @version 1.0
 * @see JwtAuthenticationFilter
 * @see JwtService
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Constructor que inyecta el ObjectMapper para serialización JSON.
     * 
     * @param objectMapper Instancia de ObjectMapper utilizada para serializar el cuerpo de respuesta
     */
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Maneja la respuesta cuando una solicitud no está autenticada.
     * 
     * <p>Este método se invoca automáticamente por Spring Security cuando un usuario
     * intenta acceder a un recurso protegido sin las credenciales adecuadas.
     * Establece el estado HTTP 401, limpia las cookies JWT y retorna un
     * cuerpo JSON con la información del error.</p>
     * 
     * @param request Solicitud HTTP entrante
     * @param response Respuesta HTTP a enviar
     * @param authException Excepción de autenticación que provocó la invocación
     * @throws IOException Si ocurre un error de entrada/salida al escribir la respuesta
     * @throws ServletException Si ocurre un error en el procesamiento del servlet
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Limpiar cookies JWT
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);

        Cookie tokenCookie = new Cookie("token", null);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        response.addCookie(tokenCookie);

        // Header para que el frontend detecte expiró
        response.setHeader("X-Session-Expired", "true");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "UNAUTHORIZED");
        errorBody.put("mensaje", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.");
        errorBody.put("ruta", request.getRequestURI());
        errorBody.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
