package com.hotelcaciquebolivar.reservas.entity.auth;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.empleado.Empleado;
import com.hotelcaciquebolivar.reservas.exception.auth.ForbiddenOperationException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un usuario del sistema de reservas del hotel.
 * Un usuario puede tener múltiples roles (administrador, recepcionista, cliente)
 * y está vinculado opcionalmente a una entidad Cliente o Empleado.
 *
 * <p>Tabla: {@code usuarios}</p>
 * <p>Soporta autenticación local y mediante proveedores externos (Google, Facebook).</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Correo electrónico del usuario. Único en el sistema, máximo 120 caracteres. */
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    /** Nombre de usuario único en el sistema. Máximo 100 caracteres. */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /** Hash de la contraseña del usuario. Puede ser nulo si se autentica con un proveedor externo. */
    @Column(name = "password_hash", length = 255)
    private String password;

    /** Proveedor de autenticación utilizado. Por defecto es LOCAL. */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    /** Identificador del usuario en el proveedor externo (Google, Facebook). Único si se usa. */
    @Column(name = "provider_id", unique = true, length = 120)
    private String providerId;

    /** URL de la foto de perfil del usuario. Máximo 500 caracteres. */
    @Column(name = "picture_url", length = 500)
    private String profilePicture;

    /** Indica si el usuario está activo en el sistema. Por defecto {@code true}. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /** Fecha y hora del último inicio de sesión del usuario. */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /** Fecha y hora de creación del registro. Se asigna automáticamente al persistir. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del registro. Se actualiza automáticamente. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Conjunto de roles asignados al usuario. Almacenado en tabla separada {@code usuarios_roles}. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Set<Rol> roles = new HashSet<>();

    /** Cliente vinculado a este usuario. Relación uno-a-uno inversa. Puede ser nulo. */
    @OneToOne(mappedBy = "usuario")
    private Cliente cliente;

    /** Empleado vinculado a este usuario. Relación uno-a-uno inversa. Puede ser nulo. */
    @OneToOne(mappedBy = "usuario")
    private Empleado empleado;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    @Column(name = "version")
    private Long version;

    /** Callback JPA que establece la fecha de creación antes de persistir. */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* =====================================================
       DOMINIO
       ===================================================== */

    /**
     * Valida que el usuario esté activo. Lanza una excepción si está inactivo.
     *
     * @throws ForbiddenOperationException si el usuario no está activo
     */
    public void validarActivo() {
        if (!Boolean.TRUE.equals(this.activo)) {
            throw new ForbiddenOperationException(
                    "operar con un usuario inactivo"
            );
        }
    }

    /**
     * Valida si se permite el cambio de correo electrónico.
     * Los clientes no pueden modificar su email y ningún usuario puede
     * cambiarlo a un valor diferente al actual.
     *
     * @param nuevoEmail el nuevo correo electrónico propuesto
     * @throws ForbiddenOperationException si no se permite el cambio
     */
    public void validarCambioEmail(String nuevoEmail) {

        if (nuevoEmail == null) {
            return;
        }

        if (roles.contains(Rol.CLIENTE)) {
            throw new ForbiddenOperationException(
                    "modificar email del cliente"
            );
        }

        if (!nuevoEmail.equals(this.email)) {
            throw new ForbiddenOperationException(
                    "cambiar email del usuario"
            );
        }
    }

    /**
     * Agrega un rol al usuario.
     *
     * @param rol el rol a agregar
     * @throws ValidationException si el rol es nulo
     */
    public void agregarRol(Rol rol) {

        if (rol == null) {
            throw new ValidationException(
                    "rol",
                    "no puede ser nulo"
            );
        }

        this.roles.add(rol);
    }

    /**
     * Remueve un rol del usuario.
     *
     * @param rol el rol a remover
     */
    public void removerRol(Rol rol) {
        this.roles.remove(rol);
    }

    /**
     * Verifica si el usuario tiene asignado un rol específico.
     *
     * @param rol el rol a verificar
     * @return {@code true} si el usuario tiene el rol, {@code false} en caso contrario
     */
    public boolean tieneRol(Rol rol) {
        return this.roles.contains(rol);
    }

    /**
     * Verifica si el usuario tiene el rol de administrador.
     *
     * @return {@code true} si es administrador
     */
    public boolean esAdmin() {
        return tieneRol(Rol.ADMIN);
    }

    /**
     * Verifica si el usuario tiene el rol de recepcionista.
     *
     * @return {@code true} si es recepcionista
     */
    public boolean esRecepcionista() {
        return tieneRol(Rol.RECEPCIONISTA);
    }

    /**
     * Verifica si el usuario tiene el rol de cliente.
     *
     * @return {@code true} si es cliente
     */
    public boolean esCliente() {
        return tieneRol(Rol.CLIENTE);
    }

    /** Desactiva el usuario, impidiéndole acceder al sistema. */
    public void desactivar() {
        this.activo = false;
    }

    /** Activa el usuario, permitiéndole acceder al sistema. */
    public void activar() {
        this.activo = true;
    }
}
