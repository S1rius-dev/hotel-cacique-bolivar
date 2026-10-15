package com.hotelcaciquebolivar.reservas.security.jwt;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio centralizado para la gestión de tokens JWT (JSON Web Token).
 * 
 * Esta clase es responsable de toda la operación con tokens JWT en el sistema,
 * incluyendo generación, validación y extracción de claims. Utiliza la librería
 * JJWT para el procesamiento criptográfico.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Generación de tokens de acceso y refresh con expiración configurable</li>
 *   <li>Validación de tokens contra la clave secreta y verificación de expiración</li>
 *   <li>Extracción de claims (username, roles, tipo de token)</li>
 *   <li>Manejo seguro de claves de firma con algoritmo HMAC-SHA</li>
 * </ul>
 * 
 * <p>La clave secreta debe estar configurada en Base64 y tener al menos 256 bits
 * de longitud para garantizar la seguridad. La configuración se realiza a través
 * de las propiedades:</p>
 * <ul>
 *   <li>{@code app.jwt.secret} - Clave secreta en formato Base64</li>
 *   <li>{@code app.jwt.expiration-ms} - Tiempo de expiración del access token en milisegundos</li>
 *   <li>{@code app.jwt.refresh-expiration-ms} - Tiempo de expiración del refresh token en milisegundos</li>
 * </ul>
 * 
 * @author Hotel Cacique Bolívar
 * @version 1.0
 * @see JwtAuthenticationFilter
 * @see JwtAuthenticationEntryPoint
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String jwtSecret;
    private final long jwtExpiration;
    private final long refreshExpiration;

    /**
     * Constructor que inicializa el servicio con la configuración JWT del sistema.
     * 
     * <p>Realiza validación estricta de la clave secreta al iniciar:</p>
     * <ul>
     *   <li>Verifica que la clave no sea nula o vacía</li>
     *   <li>Valida que sea Base64 válido</li>
     *   <li>Asegura que tenga al menos 32 bytes (256 bits) después de decodificar</li>
     * </ul>
     * 
     * @param jwtSecret Clave secreta JWT en formato Base64 (propiedad app.jwt.secret)
     * @param jwtExpiration Tiempo de expiración del access token en milisegundos (propiedad app.jwt.expiration-ms)
     * @param refreshExpiration Tiempo de expiración del refresh token en milisegundos (propiedad app.jwt.refresh-expiration-ms)
     * @throws IllegalArgumentException Si la clave es nula, vacía, no es Base64 válido o tiene menos de 32 bytes
     */
    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpiration,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;

        // Validación estricta de la clave
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("La clave JWT (app.jwt.secret) no puede ser nula o vacía. Verifica tu configuración y variables de entorno.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(jwtSecret);
            if (decoded.length < 32) {
                throw new IllegalArgumentException("La clave JWT debe ser de al menos 256 bits (32 bytes) después de decodificar Base64. Longitud actual: " + decoded.length);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La clave JWT no es Base64 válida o es demasiado corta: " + e.getMessage());
        }
    }

    /**
     * Obtiene la clave de firma HMAC-SHA a partir de la clave secreta configurada.
     * 
     * <p>Decodifica la clave secreta de Base64 y genera una {@link Key} HMAC-SHA
     * utilizada para firmar y verificar tokens JWT. La clave debe tener al menos
     * 32 bytes (256 bits) para ser compatible con HMAC-SHA256.</p>
     * 
     * @return Clave de firma HMAC-SHA
     * @throws IllegalArgumentException Si la clave decodificada tiene menos de 32 bytes
     * @throws Exception Si ocurre un error al decodificar la clave Base64
     */
    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("La clave JWT debe ser de al menos 256 bits (32 bytes) después de decodificar Base64. Longitud actual: " + keyBytes.length);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("[JWT] Error al obtener la clave de firma: {}", e.getMessage());
            throw e;
        }
    }

    // =========================
    // ACCESS TOKEN
    // =========================
    /**
     * Genera un token de acceso JWT para el usuario especificado.
     * 
     * <p>El token generado contiene los siguientes claims:</p>
     * <ul>
     *   <li>{@code sub} - Nombre de usuario (username)</li>
     *   <li>{@code roles} - Lista de roles del usuario</li>
     *   <li>{@code iat} - Fecha de emisión del token</li>
     *   <li>{@code exp} - Fecha de expiración del token</li>
     * </ul>
     * 
     * @param userDetails Detalles del usuario para el cual se genera el token
     * @return Token JWT generado en formato compacto
     */
    public String generateToken(UserDetails userDetails) {

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera un token de acceso JWT para el usuario especificado.
     * 
     * <p>Este método es un alias de {@link #generateToken(UserDetails)} para mayor
     * claridad semántica en el código que lo invoca.</p>
     * 
     * @param userDetails Detalles del usuario para el cual se genera el token
     * @return Token de acceso JWT generado en formato compacto
     * @see #generateToken(UserDetails)
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails);
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    /**
     * Genera un token de actualización (refresh token) JWT para el usuario especificado.
     * 
     * <p>El refresh token contiene los siguientes claims:</p>
     * <ul>
     *   <li>{@code sub} - Nombre de usuario (username)</li>
     *   <li>{@code type} - Tipo de token: "refresh"</li>
     *   <li>{@code iat} - Fecha de emisión del token</li>
     *   <li>{@code exp} - Fecha de expiración del token</li>
     * </ul>
     * 
     * <p>El refresh token tiene un tiempo de expiración mayor que el access token
     * y se utiliza para obtener nuevos access tokens sin requerir autenticación completa.</p>
     * 
     * @param userDetails Detalles del usuario para el cual se genera el refresh token
     * @return Refresh token JWT generado en formato compacto
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene el tiempo de expiración del access token en milisegundos.
     * 
     * @return Tiempo de expiración del access token en milisegundos
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Obtiene el tiempo de expiración del refresh token en milisegundos.
     * 
     * @return Tiempo de expiración del refresh token en milisegundos
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    // =========================
    // VALIDATION
    // =========================
    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     * 
     * <p>Parsea el token y extrae el claim {@code sub} que contiene el
     * nombre de usuario al que fue emitido el token.</p>
     * 
     * @param token Token JWT del cual se extraerá el nombre de usuario
     * @return Nombre de usuario contenido en el token, o {@code null} si el token es inválido
     */
    public String extractUsername(String token) {
        Claims claims = getAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Valida que un token JWT sea válido para el usuario especificado.
     * 
     * <p>La validación verifica tres condiciones:</p>
     * <ol>
     *   <li>El nombre de usuario en el token coincide con el del usuario</li>
     *   <li>El token no ha expirado</li>
     *   <li>El token puede ser parseado correctamente</li>
     * </ol>
     * 
     * @param token Token JWT a validar
     * @param userDetails Detalles del usuario contra el cual se valida el token
     * @return {@code true} si el token es válido para el usuario, {@code false} en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Valida que un refresh token JWT sea válido para el usuario especificado.
     * 
     * <p>Este método delega la validación a {@link #isTokenValid(String, UserDetails)},
     * ya que la lógica de validación es la misma tanto para access tokens como para
     * refresh tokens.</p>
     * 
     * @param token Refresh token JWT a validar
     * @param userDetails Detalles del usuario contra el cual se valida el token
     * @return {@code true} si el refresh token es válido para el usuario, {@code false} en caso contrario
     * @see #isTokenValid(String, UserDetails)
     */
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails);
    }

    /**
     * Verifica si un token JWT ha expirado.
     * 
     * <p>Extrae el claim de expiración ({@code exp}) del token y lo compara
     * con la fecha y hora actuales.</p>
     * 
     * @param token Token JWT a verificar
     * @return {@code true} si el token ha expirado o es inválido, {@code false} si es válido
     */
    private boolean isTokenExpired(String token) {
        Claims claims = getAllClaims(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    /**
     * Extrae todos los claims (reclamaciones) de un token JWT.
     * 
     * <p>Parsea el token completo y retorna todos los claims contenidos en él,
     * incluyendo subject, expiration, issuedAt y cualquier claim personalizado
     * (como roles o tipo de token).</p>
     * 
     * @param token JWT Token JWT del cual se extraerán los claims
     * @return Objeto {@link Claims} con todos los claims del token, o {@code null} si el token es inválido
     */
    public Claims getAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("[JWT] Error al parsear el token: {}", e.getMessage());
            return null;
        }
    }

}
