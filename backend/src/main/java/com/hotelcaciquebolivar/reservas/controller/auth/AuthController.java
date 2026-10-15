package com.hotelcaciquebolivar.reservas.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.auth.LoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.LoginResponse;
import com.hotelcaciquebolivar.reservas.dto.auth.GoogleLoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.RegisterRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClientePublicResponse;
import com.hotelcaciquebolivar.reservas.service.Auth.AuthService;
import com.hotelcaciquebolivar.reservas.service.cliente.ClienteService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para autenticación y gestión de sesiones de usuario.
 * Proporciona endpoints para registro, login, logout, renovación de tokens,
 * autenticación con Google y gestión de perfiles de cliente.
 * Utiliza cookies HTTPOnly para almacenar tokens de acceso y refresco.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ClienteService clienteService;
    private final com.hotelcaciquebolivar.reservas.config.CookieProperties cookieProperties;

    /**
     * Verifica si las cookies deben ser marcadas como seguras (HTTPS).
     *
     * @return true si las cookies son seguras, false en caso contrario
     */
    private boolean isCookieSecure() {
        return cookieProperties.isSecure();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request datos de registro del usuario, validados con @Valid
     * @return ResponseEntity con la respuesta de login que incluye tokens de acceso y refresco
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        LoginResponse response = authService.register(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Autentica un usuario existente con email y contraseña.
     * Establece cookies HTTPOnly con tokens de acceso (1 hora) y refresco (7 días).
     *
     * @param request  credenciales de login (email y contraseña), validadas con @Valid
     * @param response objeto HttpServletResponse para agregar las cookies
     * @return ResponseEntity con la respuesta de login que incluye tokens de acceso y refresco
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse authResponse = authService.login(request);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.accessToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(604800)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Obtiene la información del usuario actualmente autenticado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @return ResponseEntity con los datos del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication));
    }

    /**
     * Completa el perfil del usuario autenticado creando un cliente asociado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @param dto            datos del cliente a crear, validados con @Valid
     * @return ResponseEntity con el perfil público del cliente creado, código 201 Created
     */
    @PostMapping("/completar-perfil")
    public ResponseEntity<ClientePublicResponse> completarPerfil(
            Authentication authentication,
            @Valid @RequestBody ClienteRequest dto) {
        ClientePublicResponse response = clienteService.crearMiPerfil(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Vincula un cliente existente al usuario autenticado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @param clienteId      identificador del cliente a vincular
     * @return ResponseEntity con el perfil público del cliente vinculado, código 201 Created
     */
    @PostMapping("/vincular-cliente/{clienteId}")
    public ResponseEntity<ClientePublicResponse> vincularCliente(
            Authentication authentication,
            @PathVariable Long clienteId) {
        ClientePublicResponse response = clienteService.vincularCliente(clienteId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cierra la sesión del usuario eliminando las cookies de tokens.
     * Las cookies de access_token y refresh_token se establecen con maxAge=0 para eliminarlas.
     *
     * @param response objeto HttpServletResponse para modificar las cookies
     * @return ResponseEntity con código 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok().build();
    }

    /**
     * Renueva los tokens de acceso y refresco del usuario autenticado.
     * Establece nuevas cookies con los tokens actualizados.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @param httpResponse   objeto HttpServletResponse para agregar las cookies
     * @return ResponseEntity con la respuesta de login que incluye nuevos tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            Authentication authentication,
            HttpServletResponse httpResponse) {
        LoginResponse authResponse = authService.refresh(authentication);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.accessToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(604800)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader("Set-Cookie", accessCookie.toString());
        httpResponse.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Autentica un usuario mediante Google OAuth2.
     * Establece cookies HTTPOnly con tokens de acceso (1 hora) y refresco (24 horas).
     *
     * @param request        token de autenticación de Google, validado con @Valid
     * @param httpResponse   objeto HttpServletResponse para agregar las cookies
     * @return ResponseEntity con la respuesta de login que incluye tokens de acceso y refresco
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request, HttpServletResponse httpResponse) {

        LoginResponse response = authService.googleLogin(request);

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", response.accessToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", response.refreshToken())
                .httpOnly(true)
                .secure(isCookieSecure())
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader("Set-Cookie", accessTokenCookie.toString());
        httpResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // Retornar 200 OK con el token generado
        return ResponseEntity.ok(response);
    }

}
