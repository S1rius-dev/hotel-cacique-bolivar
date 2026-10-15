package com.hotelcaciquebolivar.reservas.controller.password;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.password.ForgotPasswordRequest;
import com.hotelcaciquebolivar.reservas.dto.password.ResetPasswordRequest;
import com.hotelcaciquebolivar.reservas.dto.password.VerifyResetCodeRequest;
import com.hotelcaciquebolivar.reservas.service.password.PasswordRecoveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la recuperación de contraseñas.
 * Proporciona endpoints para solicitar código de recuperación,
 * verificar el código y restablecer la contraseña.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    /**
     * Envía un código de recuperación de contraseña al email especificado.
     * Si el correo existe en el sistema, se envía un código de verificación.
     *
     * @param dto datos que contienen el email del usuario
     * @return mensaje de confirmación
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest dto) {
        passwordRecoveryService.enviarCodigoRecuperacion(dto.email());
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un código de recuperación."));
    }

    /**
     * Verifica la validez de un código de recuperación de contraseña.
     *
     * @param dto datos que contienen el email y el código de verificación
     * @return mensaje de confirmación de verificación exitosa
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest dto) {
        passwordRecoveryService.verificarCodigo(dto.email(), dto.codigo());
        return ResponseEntity.ok(Map.of("message", "Código verificado correctamente."));
    }

    /**
     * Restablece la contraseña del usuario con una nueva contraseña.
     * Requiere un código de verificación válido previamente verificado.
     *
     * @param dto datos que contienen el email, código de verificación y nueva contraseña
     * @return mensaje de confirmación de restablecimiento exitoso
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest dto) {
        passwordRecoveryService.restablecerPassword(dto.email(), dto.codigo(), dto.password());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida correctamente."));
    }
}
