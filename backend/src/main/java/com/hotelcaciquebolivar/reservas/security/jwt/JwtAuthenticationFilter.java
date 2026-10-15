package com.hotelcaciquebolivar.reservas.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hotelcaciquebolivar.reservas.config.security.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de autenticación JWT que se ejecuta una vez por cada solicitud HTTP.
 * 
 * Esta clase extiende {@link OncePerRequestFilter} de Spring Security y es responsable
 * de interceptar cada solicitud para extraer y validar tokens JWT, estableciendo
 * el contexto de seguridad correspondiente.
 * 
 * <p>El proceso de autenticación sigue estos pasos:</p>
 * <ol>
 *   <li>Extraer el token JWT de la solicitud (header Authorization, cookies o header token)</li>
 *   <li>Validar que el token no haya expirado y sea criptográficamente válido</li>
 *   <li>Extraer el nombre de usuario del token</li>
 *   <li>Cargar los detalles del usuario desde la base de datos</li>
 *   <li>Establecer la autenticación en el {@link SecurityContextHolder}</li>
 * </ol>
 * 
 * <p>Fuentes de extracción de token (en orden de prioridad):</p>
 * <ul>
 *   <li>Header {@code Authorization: Bearer <token>}</li>
 *   <li>Header {@code Authorization: <token>} (sin prefijo Bearer)</li>
 *   <li>Cookies: access_token, refresh_token, jwt, token</li>
 *   <li>Header personalizado {@code token}</li>
 * </ul>
 * 
 * @author Hotel Cacique Bolívar
 * @version 1.0
 * @see JwtService
 * @see com.hotelcaciquebolivar.reservas.config.security.UserDetailsServiceImpl
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructor que inyecta los servicios necesarios para la autenticación JWT.
     * 
     * @param jwtService Servicio para generación, validación y extracción de claims de tokens JWT
     * @param userDetailsService Servicio para cargar detalles de usuario desde la fuente de datos
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Extrae el token JWT de la solicitud HTTP verificando múltiples fuentes.
     * 
     * <p>El método busca el token en el siguiente orden de prioridad:</p>
     * <ol>
     *   <li>Header {@code Authorization} con prefijo "Bearer "</li>
     *   <li>Header {@code Authorization} sin prefijo (solo el token)</li>
     *   <li>Cookies con nombres: access_token, refresh_token, jwt, token</li>
     *   <li>Header personalizado {@code token}</li>
     * </ol>
     * 
     * @param request Solicitud HTTP de la cual se extraerá el token
     * @return El token JWT extraído, o {@code null} si no se encuentra ningún token válido
     */
    private String getToken(HttpServletRequest request) {

        // 1. Intentar por Authorization header (Bearer)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 1.1. Authorization sin "Bearer" (solo el token)
        if (header != null && !header.isEmpty()) {
            return header;
        }

        // 2. Intentar por cookies
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName()) ||
                    "refresh_token".equals(cookie.getName()) ||
                    "jwt".equals(cookie.getName()) ||
                    "token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 3. Buscar en header "token"
        String tokenHeader = request.getHeader("token");
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            return tokenHeader;
        }

        return null;
    }

    /**
     * Procesa cada solicitud HTTP para autenticar al usuario mediante JWT.
     * 
     * <p>Este método se invoca una vez por cada solicitud y realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Extrae el token JWT de la solicitud</li>
     *   <li>Verifica que no haya una autenticación existente en el contexto</li>
     *   <li>Extrae el nombre de usuario del token</li>
     *   <li>Carga los detalles del usuario desde la base de datos</li>
     *   <li>Valida que el token sea válido para el usuario</li>
     *   <li>Establece la autenticación en el contexto de seguridad</li>
     * </ol>
     * 
     * <p>Si ocurre un error durante el procesamiento (usuario no encontrado, token inválido),
     * el error se registra en el log pero no se interrumpe la cadena de filtros.</p>
     * 
     * @param request Solicitud HTTP entrante
     * @param response Respuesta HTTP a enviar
     * @param filterChain Cadena de filtros a continuación
     * @throws ServletException Si ocurre un error en el procesamiento del servlet
     * @throws IOException Si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = getToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jwtService.extractUsername(token);

            if (username != null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth
                                = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException e) {
                    log.warn("Usuario no encontrado: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("Error validando token JWT", e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
