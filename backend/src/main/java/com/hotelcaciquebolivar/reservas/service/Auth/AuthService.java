package com.hotelcaciquebolivar.reservas.service.Auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.hotelcaciquebolivar.reservas.dto.auth.LoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.LoginResponse;
import com.hotelcaciquebolivar.reservas.dto.auth.GoogleLoginRequest;
import com.hotelcaciquebolivar.reservas.dto.auth.RegisterRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;

/**
 * Interfaz que define las operaciones de autenticación y autorización del sistema hotelero.
 * Proporciona funcionalidades para registro de usuarios, inicio de sesión con credenciales
 * locales o Google, obtención del usuario actual, carga de usuarios por email o username,
 * y renovación de tokens JWT.
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema con las credenciales proporcionadas.
     *
     * @param request objeto que contiene los datos necesarios para el registro del usuario
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     */
    LoginResponse register(RegisterRequest request);

    /**
     * Inicia sesión en el sistema con las credenciales proporcionadas.
     *
     * @param request objeto que contiene el email y contraseña del usuario
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     */
    LoginResponse login(LoginRequest request);

    /**
     * Obtiene la información del usuario actualmente autenticado.
     *
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return respuesta con los datos del usuario autenticado
     */
    UserResponse getCurrentUser(Authentication authentication);

    /**
     * Carga los detalles de un usuario por su dirección de email.
     *
     * @param email dirección de email del usuario a buscar
     * @return detalles del usuario encontrado para Spring Security
     */
    UserDetails loadUserByEmail(String email);

    /**
     * Carga los detalles de un usuario por su nombre de usuario (username).
     *
     * @param username nombre de usuario del usuario a buscar
     * @return detalles del usuario encontrado para Spring Security
     */
    UserDetails loadUserByUsername(String username);

    /**
     * Renueva el token JWT de un usuario autenticado actualmente.
     *
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return respuesta de autenticación con el nuevo token JWT generado
     */
    LoginResponse refresh(Authentication authentication);

    /**
     * Inicia sesión o registra un usuario utilizando autenticación con Google.
     * Si el usuario no existe, lo crea automáticamente con los datos de Google.
     *
     * @param request objeto que contiene el token de ID de Google para autenticación
     * @return respuesta de autenticación con el token JWT generado y los datos del usuario
     */
    LoginResponse googleLogin(GoogleLoginRequest request);
}
