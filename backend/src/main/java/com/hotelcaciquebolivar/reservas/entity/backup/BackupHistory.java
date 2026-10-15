package com.hotelcaciquebolivar.reservas.entity.backup;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que registra una ejecucion de copia de seguridad de la base de
 * datos.P
 *
 * <p>
 * Una fila nace en estado {@link BackupEstado#EN_PROGRESO} cuando se lanza
 * mysqldump y se actualiza a {@link BackupEstado#EXITOSO} o
 * {@link BackupEstado#FALLIDO} al terminar. Es la fuente de verdad del
 * historial: el almacenamiento fisico solo guarda los archivos .sql.gz.</p>
 *
 * <p>
 * No se modela el restore: un restore se ejecuta fuera de banda con la
 * aplicacion detenida. Ver {@code docs/backup-runbook.md}.</p>
 *
 * <p>
 * Tabla: {@code backup_history}</p>
 */
@Entity
@Table(
        name = "backup_history",
        indexes = {
            @Index(name = "idx_backup_fecha", columnList = "fecha"),
            @Index(name = "idx_backup_estado", columnList = "estado"),
            @Index(name = "idx_backup_tipo", columnList = "tipo")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BackupHistory {

    /**
     * Identificador unico de la ejecucion.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Fecha y hora de inicio de la ejecucion. Se asigna automaticamente al
     * persistir.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Fecha y hora de finalizacion. Nula mientras la ejecucion sigue
     * EN_PROGRESO.
     */
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    /**
     * Origen de la copia segun {@link BackupTipo}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BackupTipo tipo;

    /**
     * Estado de la ejecucion segun {@link BackupEstado}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BackupEstado estado;

    /**
     * Nombre del archivo comprimido, unico en toda la tabla.
     */
    @Column(name = "archivo_nombre", nullable = false, unique = true, length = 255)
    private String archivoNombre;

    /**
     * Ruta del archivo dentro del proveedor de almacenamiento. Para
     * {@link ProveedorStorage#LOCAL} es una ruta relativa al directorio
     * configurado.
     */
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    /**
     * Proveedor fisico donde quedo el archivo segun {@link ProveedorStorage}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProveedorStorage proveedor;

    /**
     * Tamano en bytes del archivo .sql.gz almacenado. Nulo si la ejecucion
     * fallo.
     */
    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    /**
     * Duracion de la ejecucion en milisegundos. Nula si la ejecucion no ha
     * terminado.
     */
    @Column(name = "duracion_ms")
    private Long duracionMs;

    /**
     * Email del administrador que solicito la copia. Solo para backups MANUAL.
     */
    @Column(length = 120)
    private String usuario;

    /**
     * SHA-256 del archivo comprimido, para verificar integridad antes de un
     * restore.
     */
    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    /**
     * Detalle del error cuando la ejecucion termina en FALLIDO.
     */
    @JsonIgnore
    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    /**
     * Callback JPA que establece la fecha de inicio antes de persistir.
     */
    @PrePersist
    void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    /**
     * Callback JPA que actualiza la fecha de finalizacion antes de actualizar.
     */
    @PreUpdate
    void onUpdate() {
        if (this.estado != BackupEstado.EN_PROGRESO) {
            this.fechaFin = LocalDateTime.now();
        }
    }
}
