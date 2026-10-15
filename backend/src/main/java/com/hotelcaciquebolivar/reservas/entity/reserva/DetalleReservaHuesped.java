package com.hotelcaciquebolivar.reservas.entity.reserva;

import java.time.LocalDateTime;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

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
 * Entidad que representa un huésped asignado a un detalle de reserva.
 * Vincula un cliente específico con una habitación reservada, registrando
 * las fechas de ingreso y salida del huésped.
 *
 * <p>Tabla: {@code detalles_reserva_huespedes}</p>
 */
@Entity
@Table(name = "detalles_reserva_huespedes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleReservaHuesped {

    /** Identificador único del huésped en el detalle. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Detalle de reserva al que pertenece el huésped. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_reserva_id", nullable = false)
    private DetalleReserva detalleReserva;

    /** Cliente que se aloja como huésped. Relación many-to-one obligatoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Fecha y hora de ingreso real del huésped. Puede ser nula si aún no ha llegado. */
    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    /** Fecha y hora de salida real del huésped. Puede ser nulo si aún no ha salido. */
    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

}