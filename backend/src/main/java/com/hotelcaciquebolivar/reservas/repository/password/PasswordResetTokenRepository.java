package com.hotelcaciquebolivar.reservas.repository.password;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelcaciquebolivar.reservas.entity.password.PasswordResetToken;

/**
 * Repositorio para la gestión de tokens de restablecimiento de contraseña.
 * Permite buscar tokens por email y código, y obtener el token más reciente no utilizado.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token de restablecimiento por email, código y estado de uso.
     *
     * @param email email del usuario
     * @param codigo código de verificación de restablecimiento
     * @return Optional con el token no utilizado o vacío si no existe
     */
    Optional<PasswordResetToken> findByUserEmailAndCodigoAndUsadoFalse(String email, String codigo);

    /**
     * Busca el token de restablecimiento más reciente no utilizado para un email específico.
     *
     * @param email email del usuario
     * @return Optional con el token más reciente no utilizado o vacío si no existe
     */
    Optional<PasswordResetToken> findTopByUserEmailAndUsadoFalseOrderByFechaCreacionDesc(String email);
}
