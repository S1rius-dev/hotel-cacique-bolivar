package com.hotelcaciquebolivar.reservas.entity.password;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.auth.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que almacena tokens de restablecimiento de contraseña.
 * Cada token contiene un código numérico de 6 dígitos, una fecha de expiración
 * y un contador de intentos fallidos para prevenir abusos.
 *
 * <p>Tabla: {@code password_reset_tokens}</p>
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    /** Identificador único del token. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario al que pertenece el token. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Código numérico de 6 dígitos para restablecer la contraseña. */
    @Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

    /** Fecha y hora de expiración del token. */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /** Indica si el token ya ha sido utilizado. Por defecto {@code false}. */
    @Column(name = "usado", nullable = false)
    @Builder.Default
    private boolean usado = false;

    /** Número de intentos fallidos de uso del token. Por defecto 0. */
    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private int intentosFallidos = 0;

    /** Fecha y hora de creación del token. No se puede actualizar. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Verifica si el token ha expirado.
     *
     * @return {@code true} si la fecha actual es posterior a la fecha de expiración
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si se pueden realizar más intentos de uso del token.
     * El límite máximo es de 5 intentos fallidos.
     *
     * @return {@code true} si se permiten más intentos
     */
    public boolean puedeReintentar() {
        return intentosFallidos < 5;
    }
}
