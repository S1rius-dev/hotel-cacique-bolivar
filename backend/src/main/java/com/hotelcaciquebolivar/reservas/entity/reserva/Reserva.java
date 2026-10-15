package com.hotelcaciquebolivar.reservas.entity.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.entity.pago.TransaccionPago;
import com.hotelcaciquebolivar.reservas.util.DineroUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una reserva de habitación en el hotel.
 * Gestiona el ciclo de vida completo de una reserva, desde su creación hasta
 * su finalización, incluyendo detalles de habitaciones, pagos y ajustes.
 *
 * <p>Tabla: {@code reservas}</p>
 */
@Entity
@Table(
        name = "reservas",
        indexes = {
            @jakarta.persistence.Index(name = "idx_reserva_checkin_estado", columnList = "fecha_check_in, estado"),
            @jakarta.persistence.Index(name = "idx_reserva_titular", columnList = "titular_reserva_id"),
            @jakarta.persistence.Index(name = "idx_reserva_estado", columnList = "estado"),
            @jakarta.persistence.Index(name = "idx_reserva_fecha_creacion", columnList = "fecha_creacion"),
            @jakarta.persistence.Index(name = "idx_reserva_fecha_expiracion_pago", columnList = "fecha_expiracion_pago")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reserva {

    /** Identificador único de la reserva. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código único de la reserva. Se genera automáticamente con formato AÑO-SUFijo. */
    @Column(name = "codigo_reserva", unique = true, nullable = false, length = 20)
    private String codigoReserva;

    /** Cliente titular de la reserva. Relación many-to-one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_reserva_id", nullable = false)
    private Cliente titularReserva;

    /** Lista de detalles de reserva (habitaciones reservadas). Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "reserva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DetalleReserva> detalles = new ArrayList<>();

    /** Lista de pagos asociados a la reserva. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "reserva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    /** Lista de ajustes aplicados a la reserva. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "reserva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<AjusteReserva> ajustes = new ArrayList<>();

    /** Lista de transacciones de pago de pasarelas externas. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "reserva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TransaccionPago> transaccionesPago = new ArrayList<>();

    /** Fecha y hora de creación de la reserva. No se puede actualizar. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha y hora de la última actualización del registro. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Fecha de check-in programada. */
    @Column(name = "fecha_check_in", nullable = false)
    private LocalDate fechaCheckIn;

    /** Fecha de check-out programada. */
    @Column(name = "fecha_check_out", nullable = false)
    private LocalDate fechaCheckOut;

    /** Hora real en que se realizó el check-in. Puede ser nula. */
    @Column(name = "hora_check_in")
    private LocalDateTime horaCheckIn;

    /** Hora real en que se realizó el check-out. Puede ser nula. */
    @Column(name = "hora_check_out")
    private LocalDateTime horaCheckOut;

    /** Estado de la reserva según el enumerador {@link EstadoReserva}. Por defecto PENDIENTE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    /** Origen de la reserva según el enumerador {@link OrigenReserva}. Por defecto RECEPCION. */
    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    @Builder.Default
    private OrigenReserva origen = OrigenReserva.RECEPCION;

    /** Monto total de la reserva. Precisión 12, escala 2. Por defecto 0. */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /** Fecha y hora de expiración del plazo de pago. Puede ser nula. */
    @Column(name = "fecha_expiracion_pago")
    private LocalDateTime fechaExpiracionPago;

    /** Observaciones adicionales de la reserva. Máximo 500 caracteres. */
    @Column(length = 500)
    private String observaciones;

    /** Número de versión para control de concurrencia optimista. */
    @Version
    @Column(name = "version")
    private Long version;

    /** Callback JPA que establece fechas y genera código antes de persistir. */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoReserva.PENDIENTE;
        }
        if (codigoReserva == null) {
            codigoReserva = generarCodigoReserva();
        }
    }

    /** Callback JPA que actualiza la fecha de modificación antes de actualizar. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Genera un código de reserva único con formato AÑO-SUFijo.
     *
     * @return código de reserva generado
     */
    private String generarCodigoReserva() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return LocalDate.now().getYear() + "-" + sufijo;
    }

    /**
     * Calcula el número de noches de la estadía basándose en las fechas de check-in y check-out.
     *
     * @return número de noches (mínimo 1)
     */
    public long calcularNoches() {
        return Math.max(1, ChronoUnit.DAYS.between(fechaCheckIn, fechaCheckOut));
    }

    /**
     * Calcula el monto total pagado en pagos completados.
     *
     * @return suma de montos de pagos con estado COMPLETADO
     */
    public BigDecimal getTotalPagado() {
        if (pagos == null || pagos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.COMPLETADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el saldo pendiente de pago (total - pagado).
     *
     * @return saldo pendiente
     */
    public BigDecimal getSaldoPendiente() {
        BigDecimal pagado = getTotalPagado();
        BigDecimal totalReserva = this.total != null ? this.total : BigDecimal.ZERO;
        return totalReserva.subtract(pagado);
    }

    /** Recalcula el total de la reserva sumando los totales de los detalles no cancelados. */
    public void recalcularTotal() {
        if (detalles == null || detalles.isEmpty()) {
            this.total = BigDecimal.ZERO;
            return;
        }
        this.total = DineroUtil.redondear(detalles.stream()
                .filter(d -> d.getEstado() != EstadoDetalleReserva.CANCELADA)
                .map(DetalleReserva::calcularTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /**
     * Confirma la reserva. Solo se puede confirmar si está en estado PENDIENTE o PENDIENTE_PAGO.
     *
     * @throws IllegalStateException si el estado no permite la confirmación
     */
    public void confirmar() {
        if (this.estado != EstadoReserva.PENDIENTE && this.estado != EstadoReserva.PENDIENTE_PAGO) {
            throw new IllegalStateException(
                    "Solo se puede confirmar una reserva en estado PENDIENTE o PENDIENTE_PAGO"
            );
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    /**
     * Verifica si la reserva está en estado de pendiente de pago.
     *
     * @return {@code true} si está en estado PENDIENTE_PAGO
     */
    public boolean esPendientePago() {
        return this.estado == EstadoReserva.PENDIENTE_PAGO;
    }

    /**
     * Verifica si la reserva ha expirado por falta de pago.
     *
     * @return {@code true} si está pendiente de pago y la fecha de expiración ha pasado
     */
    public boolean isExpirada() {
        return esPendientePago() && fechaExpiracionPago != null && LocalDateTime.now().isAfter(fechaExpiracionPago);
    }

    /**
     * Cancela la reserva y todos sus detalles.
     * No se puede cancelar una reserva finalizada, cancelada, en curso o marcada como NO_SHOW.
     *
     * @throws IllegalStateException si el estado no permite la cancelación
     */
    public void cancelar() {
        if (this.estado == EstadoReserva.FINALIZADA || this.estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva ya finalizada o cancelada"
            );
        }
        if (this.estado == EstadoReserva.EN_CURSO) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva en curso. Debe realizar check-out primero"
            );
        }
        if (this.estado == EstadoReserva.NO_SHOW) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva marcada como NO_SHOW"
            );
        }
        this.estado = EstadoReserva.CANCELADA;
        this.detalles.forEach(DetalleReserva::cancelar);
    }

    /**
     * Marca la reserva como NO_SHOW (el cliente no se presentó).
     * Solo se puede marcar en reservas confirmadas o pendientes.
     *
     * @throws IllegalStateException si el estado no permite marcar NO_SHOW
     */
    public void marcarNoShow() {
        if (this.estado != EstadoReserva.CONFIRMADA
                && this.estado != EstadoReserva.PENDIENTE
                && this.estado != EstadoReserva.PENDIENTE_PAGO) {
            throw new IllegalStateException(
                    "Solo se puede marcar NO_SHOW en reservas confirmadas o pendientes"
            );
        }
        this.estado = EstadoReserva.NO_SHOW;
    }

    /**
     * Agrega un detalle de reserva (habitación) a la reserva.
     * Valida que la habitación no esté duplicada.
     *
     * @param detalle el detalle de reserva a agregar
     * @throws IllegalStateException si la habitación ya está registrada
     */
    public void agregarDetalle(DetalleReserva detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        validarHabitacionNoRepetida(detalle.getHabitacion());
        detalle.setReserva(this);
        this.detalles.add(detalle);
        validarAlMenosUnDetalle();
    }

    /**
     * Valida que una habitación no esté duplicada en los detalles de la reserva.
     *
     * @param habitacion la habitación a validar
     * @throws IllegalStateException si la habitación ya está registrada
     */
    private void validarHabitacionNoRepetida(Habitacion habitacion) {
        if (habitacion == null) {
            return;
        }
        boolean duplicada = this.detalles.stream()
                .filter(d -> d.getHabitacion() != null)
                .anyMatch(d -> d.getHabitacion().getId().equals(habitacion.getId()));
        if (duplicada) {
            throw new IllegalStateException(
                    "La habitacion " + habitacion.getNumero() + " ya esta registrada en esta reserva"
            );
        }
    }

    /**
     * Remueve un detalle de reserva de la lista.
     *
     * @param detalle el detalle a remover
     */
    public void removerDetalle(DetalleReserva detalle) {
        if (this.detalles != null) {
            this.detalles.remove(detalle);
            detalle.setReserva(null);
        }
    }

    /**
     * Valida que la reserva tenga al menos un detalle.
     *
     * @throws IllegalStateException si no hay detalles
     */
    public void validarAlMenosUnDetalle() {
        if (this.detalles == null || this.detalles.isEmpty()) {
            throw new IllegalStateException("La reserva debe tener al menos un detalle");
        }
    }

    /** Número máximo de días de estadía permitidos. */
    public static final int MAX_DIAS_ESTADIA = 30;

    /**
     * Valida que las fechas de check-in y check-out sean correctas.
     * La estadía debe ser de al menos 1 noche y máximo 30 días.
     *
     * @throws IllegalStateException si las fechas son inválidas
     */
    public void validarFechas() {
        if (fechaCheckOut == null || fechaCheckIn == null) {
            throw new IllegalStateException("Las fechas de check-in y check-out son obligatorias");
        }
        if (fechaCheckOut.isBefore(fechaCheckIn)) {
            throw new IllegalStateException("La fecha de check-out debe ser posterior o igual a la de check-in");
        }
        long noches = calcularNoches();
        if (noches < 0) {
            throw new IllegalStateException("La estadía mínima es de 1 noche");
        }
        if (noches > MAX_DIAS_ESTADIA) {
            throw new IllegalStateException("La estadía máxima es de " + MAX_DIAS_ESTADIA + " días");
        }
    }

    /**
     * Valida que el total de la reserva no sea negativo.
     *
     * @throws IllegalStateException si el total es negativo
     */
    public void validarTotal() {
        if (this.total != null && this.total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total de la reserva no puede ser negativo");
        }
    }
}
