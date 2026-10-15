package com.hotelcaciquebolivar.reservas.service.password;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.password.PasswordResetToken;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mail.EmailService;
import com.hotelcaciquebolivar.reservas.mail.EmailProperties;
import com.hotelcaciquebolivar.reservas.mail.dto.PasswordRecoveryData;
import com.hotelcaciquebolivar.reservas.repository.password.PasswordResetTokenRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

/**
 * Servicio de recuperación de contraseña por código de verificación.
 * Genera un código numérico de 6 dígitos, lo envía por email y
 * gestiona la validación y restablecimiento de contraseña.
 * Los códigos expiran en 15 minutos y tienen un límite de intentos.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmailProperties emailProperties;

    public PasswordRecoveryService(UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            EmailProperties emailProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.emailProperties = emailProperties;
    }

    /**
     * Envía un código de recuperación de contraseña al email indicado.
     * Genera un código de 6 dígitos aleatorio con expiración de 15 minutos.
     *
     * @param email correo electrónico del usuario que solicita la recuperación
     * @throws ResourceNotFoundException si no existe usuario con ese email
     */
    public void enviarCodigoRecuperacion(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email", email));

        String codigo = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                .fechaCreacion(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        String nombre = user.getUsername() != null ? user.getUsername() : user.getEmail();
        String baseUrl = emailProperties.getBaseUrl();
        String urlReset = baseUrl + "/hotel/login?recovery=true&code=" + codigo;
        emailService.sendPasswordRecovery(new PasswordRecoveryData(
                email,
                "Código de recuperación - Hotel Cacique Bolívar",
                nombre,
                codigo,
                LocalDateTime.now().plusMinutes(15),
                urlReset));
    }

    /**
     * Verifica la validez de un código de recuperación.
     * Valida que el código no esté expirado y que no se hayan agotado los intentos.
     *
     * @param email correo del usuario
     * @param codigo código de 6 dígitos a verificar
     * @throws ResourceNotFoundException si no existe usuario con ese email
     * @throws ValidationException si el código es inválido, expirado o se agotaron los intentos
     */
    public void verificarCodigo(String email, String codigo) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email", email));

        PasswordResetToken token = tokenRepository
                .findByUserEmailAndCodigoAndUsadoFalse(email, codigo)
                .orElseThrow(() -> new ValidationException("codigo",
                        "Código inválido. Solicite un nuevo código."));

        if (token.isExpirado()) {
            token.setUsado(true);
            tokenRepository.save(token);
            throw new ValidationException("codigo",
                    "El código ha expirado. Solicite uno nuevo.");
        }

        if (!token.puedeReintentar()) {
            token.setUsado(true);
            tokenRepository.save(token);
            throw new ValidationException("codigo",
                    "Demasiados intentos fallidos. Solicite un nuevo código.");
        }
    }

    /**
     * Restablece la contraseña del usuario validando el código de recuperación.
     * Codifica la nueva contraseña, marca el token como usado y revierte tokens anteriores.
     *
     * @param email correo del usuario
     * @param codigo código de recuperación válido
     * @param nuevaPassword nueva contraseña en texto plano
     * @throws ResourceNotFoundException si no existe usuario con ese email
     * @throws ValidationException si el código es inválido o expirado
     */
    public void restablecerPassword(String email, String codigo, String nuevaPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email", email));

        PasswordResetToken token = tokenRepository
                .findByUserEmailAndCodigoAndUsadoFalse(email, codigo)
                .orElseThrow(() -> new ValidationException("codigo",
                        "Código inválido. Solicite un nuevo código de recuperación."));

        if (token.isExpirado()) {
            token.setUsado(true);
            tokenRepository.save(token);
            throw new ValidationException("codigo",
                    "El código ha expirado. Solicite uno nuevo.");
        }

        user.setPassword(passwordEncoder.encode(nuevaPassword));
        userRepository.save(user);

        token.setUsado(true);
        tokenRepository.save(token);

        tokenRepository.findTopByUserEmailAndUsadoFalseOrderByFechaCreacionDesc(email)
                .ifPresent(t -> {
                    t.setUsado(true);
                    tokenRepository.save(t);
                });
    }
}
