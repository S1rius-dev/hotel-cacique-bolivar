package com.hotelcaciquebolivar.reservas.entity.reserva;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un detalle de reserva, es decir, una habitación específica
 * incluida dentro de una reserva. Cada detalle contiene información sobre la
 * habitación, precios, capacidad y huéspedes asignados.
 *
 * <p>Tabla: {@code detalles_reserva}</p>
 */
@Entity
@Table(
    name = "detalles_reserva",
    indexes = {
        @jakarta.persistence.Index(name = "idx_detalle_habitacion_estado", columnList = "habitacion_id, estado"),
        @jakarta.persistence.Index(name = "idx_detalle_reserva", columnList = "reserva_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetalleReserva {

    /** Identificador único del detalle de reserva. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Reserva a la que pertenece este detalle. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    /** Habitación reservada. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    /** Estado del detalle según el enumerador {@link EstadoDetalleReserva}. Por defecto RESERVADA. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoDetalleReserva estado = EstadoDetalleReserva.RESERVADA;

    /** Precio por noche de la habitación al momento de la reserva. Precisión 10, escala 2. */
    @Column(name = "precio_por_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorNoche;

    /** Indica si el precio aplicado a este detalle incluye IVA. */
    @Column(name = "precio_usado_incluye_iva")
    private Boolean precioUsadoIncluyeIva;

    /** Snapshot del precio unitario sin IVA vigente al momento de congelar precios. Precisión 10, escala 2. */
    @Column(name = "precio_unitario_base", precision = 10, scale = 2)
    private BigDecimal precioUnitarioBase;

    /** Snapshot del precio unitario con IVA vigente al momento de congelar precios. Precisión 10, escala 2. */
    @Column(name = "precio_unitario_con_iva", precision = 10, scale = 2)
    private BigDecimal precioUnitarioConIva;

    /** Precio unitario efectivamente aplicado según el modo de IVA elegido. Precisión 10, escala 2. */
    @Column(name = "precio_unitario_aplicado", precision = 10, scale = 2)
    private BigDecimal precioUnitarioAplicado;

    /** Subtotal del detalle (precio aplicado × noches) calculado al alternar el modo de IVA. Precisión 10, escala 2. */
    @Column(name = "subtotal_detalle", precision = 10, scale = 2)
    private BigDecimal subtotalDetalle;

    /** Monto de IVA del detalle calculado al alternar el modo de IVA. Precisión 10, escala 2. */
    @Column(name = "iva_total_detalle", precision = 10, scale = 2)
    private BigDecimal ivaTotalDetalle;

    /** Total del detalle (subtotal + IVA) calculado al alternar el modo de IVA. Precisión 10, escala 2. */
    @Column(name = "total_detalle", precision = 10, scale = 2)
    private BigDecimal totalDetalle;

    /** Snapshot del precio por persona extra al momento de la reserva. Precisión 10, escala 2. */
    @Column(name = "precio_persona_extra_snapshot", precision = 10, scale = 2)
    private BigDecimal precioPersonaExtraSnapshot;

    /** Desglose congelado de precios en formato JSON. Almacenado como texto. */
    @Column(name = "desglose_congelado", columnDefinition = "TEXT")
    private String desgloseCongelado;

    /** Cantidad de personas que se alojarán en la habitación. */
    @Column(name = "cantidad_ocupantes", nullable = false)
    private Integer cantidadOcupantes;

    /** Observaciones adicionales del detalle. Máximo 255 caracteres. */
    @Column(length = 255)
    private String observaciones;

    /** Lista de huéspedes asignados a esta habitación. Relación uno-a-muchos con cascade. */
    @BatchSize(size = 20)
    @OneToMany(
            mappedBy = "detalleReserva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DetalleReservaHuesped> huespedes = new ArrayList<>();

    //LOGICA DE DOMINIO

    /**
     * Calcula el subtotal del detalle multiplicando el precio por noche por el número de noches.
     *
     * @param noches número de noches
     * @return subtotal calculado
     */
    public BigDecimal calcularSubtotal(long noches) {
        if (noches <= 0) {
            return BigDecimal.ZERO;
        }
        return precioPorNoche.multiply(BigDecimal.valueOf(noches));
    }

    /**
     * Calcula el subtotal utilizando las noches de la reserva asociada.
     *
     * @return subtotal calculado
     */
    public BigDecimal calcularSubtotal() {
        if (this.reserva == null) {
            return BigDecimal.ZERO;
        }
        return calcularSubtotal(this.reserva.calcularNoches());
    }

    /**
     * Calcula el total del detalle incluyendo el cargo por personas extra.
     * El total es: (precio por noche × noches) + (precio persona extra × extras × noches).
     *
     * @return total calculado del detalle
     */
    public BigDecimal calcularTotal() {
        if (this.reserva == null) {
            return BigDecimal.ZERO;
        }
        long noches = this.reserva.calcularNoches();
        BigDecimal total = calcularSubtotal(noches);
        if (habitacion != null && habitacion.getTipo() != null && noches > 0) {
            int extras = Math.max(0, cantidadOcupantes - habitacion.getTipo().getCapacidadBase());
            if (extras > 0) {
                total = total.add(habitacion.getTipo().getPrecioPersonaExtra()
                        .multiply(BigDecimal.valueOf(extras))
                        .multiply(BigDecimal.valueOf(noches)));
            }
        }
        return total;
    }

    /**
     * Valida que la cantidad de ocupantes no exceda la capacidad máxima de la habitación.
     *
     * @throws IllegalStateException si la capacidad es inválida o excede el máximo
     */
    public void validarCapacidad() {
        if (cantidadOcupantes == null || cantidadOcupantes <= 0) {
            throw new IllegalStateException("La cantidad de personas debe ser mayor a 0");
        }
        if (habitacion != null && habitacion.getTipo() != null) {
            if (cantidadOcupantes > habitacion.getTipo().getCapacidadMaxima()) {
                throw new IllegalStateException(
                        "La cantidad de personas (" + cantidadOcupantes
                        + ") supera la capacidad maxima de la habitacion ("
                        + habitacion.getTipo().getCapacidadMaxima() + ")"
                );
            }
        }
    }

    /**
     * Remueve un huésped de la lista de huéspedes del detalle.
     *
     * @param huesped el huésped a remover
     */
    public void removerHuesped(DetalleReservaHuesped huesped) {
        if (this.huespedes != null) {
            this.huespedes.remove(huesped);
            huesped.setDetalleReserva(null);
        }
    }

    /**
     * Agrega un huésped a la lista de huéspedes del detalle.
     * Valida que no haya duplicados y que no se exceda el límite de ocupantes.
     *
     * @param cliente el cliente a agregar como huésped
     * @throws IllegalStateException si el huésped está duplicado o se excede el límite
     */
    public void agregarHuesped(Cliente cliente) {
        if (this.huespedes == null) {
            this.huespedes = new ArrayList<>();
        }

        validarHuespedDuplicado(cliente);
        validarLimiteOcupantes();

        DetalleReservaHuesped huesped = DetalleReservaHuesped.builder()
                .detalleReserva(this)
                .cliente(cliente)
                .build();

        this.huespedes.add(huesped);
    }

    /**
     * Valida que un cliente no esté duplicado como huésped.
     *
     * @param cliente el cliente a validar
     * @throws IllegalStateException si el cliente ya es huésped
     */
    private void validarHuespedDuplicado(Cliente cliente) {
        boolean existe = this.huespedes.stream()
                .anyMatch(h -> h.getCliente().getId().equals(cliente.getId()));
        if (existe) {
            throw new IllegalStateException(
                    "El huesped ya esta registrado en este detalle de reserva"
            );
        }
    }

    /**
     * Valida que no se exceda el límite de ocupantes.
     *
     * @throws IllegalStateException si se alcanza el límite máximo
     */
    private void validarLimiteOcupantes() {
        if (this.huespedes.size() >= this.cantidadOcupantes) {
            throw new IllegalStateException(
                    "No se pueden agregar mas huespedes. Maximo permitido: " + this.cantidadOcupantes
            );
        }
    }

    /**
     * Realiza el check-in del detalle de reserva.
     * Cambia el estado de RESERVADA a OCUPADA.
     *
     * @throws IllegalStateException si el estado no es RESERVADA
     */
    public void realizarCheckIn() {
        if (this.estado == EstadoDetalleReserva.OCUPADA) {
            return;
        }
        if (this.estado != EstadoDetalleReserva.RESERVADA) {
            throw new IllegalStateException(
                    "Solo se puede realizar check-in en reservas en estado RESERVADA"
            );
        }
        this.estado = EstadoDetalleReserva.OCUPADA;
    }

    /**
     * Realiza el check-out del detalle de reserva.
     * Cambia el estado de OCUPADA a LIBERADA.
     *
     * @throws IllegalStateException si el estado no es OCUPADA
     */
    public void realizarCheckOut() {
        if (this.estado != EstadoDetalleReserva.OCUPADA) {
            throw new IllegalStateException(
                    "Solo se puede realizar check-out en habitaciones OCUPADAS"
            );
        }
        this.estado = EstadoDetalleReserva.LIBERADA;
    }

    /**
     * Cancela el detalle de reserva.
     * No se puede cancelar un detalle ya liberado (finalizado).
     *
     * @throws IllegalStateException si el estado es LIBERADA
     */
    public void cancelar() {
        if (this.estado == EstadoDetalleReserva.LIBERADA) {
            throw new IllegalStateException(
                    "No se puede cancelar un detalle de reserva ya FINALIZADO"
            );
        }
        this.estado = EstadoDetalleReserva.CANCELADA;
    }
}
