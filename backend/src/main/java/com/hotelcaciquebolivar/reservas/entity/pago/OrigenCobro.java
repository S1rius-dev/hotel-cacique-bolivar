package com.hotelcaciquebolivar.reservas.entity.pago;

/**
 * Caja destino del dinero de un pago.
 *
 * <p>Una reserva se cobra en un soloacto por el huésped, pero ese monto
 * corresponde a dos flujos de caja distintos: la habitación y los servicios
 * que pertenecen a {@code RECEPCION}, y los consumos de tienda que pertenecen
 * a {@code TIENDA}. Este enum permite etiquetar cada línea de un pago para que
 * el movimiento de caja se asiente en la caja correcta.</p>
 */
public enum OrigenCobro {

    /** Habitación, servicios y ajustes manuales de la reserva. */
    RECEPCION,

    /** Productos de tienda cargados a la reserva como consumos. */
    TIENDA
}
