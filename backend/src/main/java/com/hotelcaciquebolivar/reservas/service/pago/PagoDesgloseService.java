package com.hotelcaciquebolivar.reservas.service.pago;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.entity.consumo.Consumo;
import com.hotelcaciquebolivar.reservas.entity.consumo.EstadoConsumo;
import com.hotelcaciquebolivar.reservas.entity.pago.OrigenCobro;
import com.hotelcaciquebolivar.reservas.entity.pago.EstadoPago;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoDesgloseRepository;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reparte el monto de un pago entre las cajas de recepción y tienda.
 *
 * <p>El huésped paga la reserva una sola vez, pero ese monto cubre dos flujos de
 * caja: la habitación, que pertenece a recepción, y los consumos de tienda. Este
 * servicio calcula cuántas líneas de cada origen componen el pago, de modo que
 * cada línea se asiente como un movimiento de caja en la caja correcta.</p>
 *
 * <p>El reparto se apoya en los saldos por origen, no en el total bruto de la
 * reserva. Esto es lo que permite que una reserva cobrada en varias cuotas
 * termine asignando cada peso a la caja que le corresponde, en lugar de
 * asignarlo siempre a recepción.</p>
 *
 * <p>La estrategia de reparto es "primero recepción": cada pago cubre primero
 * lo pendiente de habitación y con el remanente cubre los consumos de tienda.
 * Si una reserva queda con consumos por cobrar, es porque se pagó a medias.</p>
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoDesgloseService {

    /** Longitud máxima del campo concepto, igual que en la entidad y la columna. */
    private static final int MAX_CONCEPTO = 200;

    /** Estados de consumo que siguen debiéndose al huésped. */
    private static final List<EstadoConsumo> CONSUMOS_COBRABLES =
            List.of(EstadoConsumo.PENDIENTE, EstadoConsumo.FACTURADO);

    private final PagoDesgloseRepository desgloseRepository;
    private final ConsumoRepository consumoRepository;
    private final ReservaPricingService pricingService;

    /**
     * Calcula el reparto de un monto entre las cajas de una reserva.
     *
     * <p>Método de solo lectura: no persiste nada, sirve tanto para el registro
     * del pago como para la vista previa que la interfaz muestra antes de cobrar.
     * Las líneas se calculan sobre los saldos pendientes en el momento de la
     * llamada, por lo que debe ejecutarse dentro de la misma transacción que
     * persiste el pago.</p>
     *
     * @param reserva reserva a la que pertenece el pago
     * @param montoPago monto a repartir
     * @return lista de líneas cuya suma es igual a {@code montoPago}, o vacía si el monto es nulo o no positivo
     */
    @Transactional(readOnly = true)
    public List<PagoDesgloseLinea> calcularDesglose(Reserva reserva, BigDecimal montoPago) {
        if (reserva == null || reserva.getId() == null
                || montoPago == null || montoPago.signum() <= 0) {
            return List.of();
        }

        SaldosPorOrigen saldos = calcularSaldos(reserva);

        BigDecimal saldo = montoPago;
        List<PagoDesgloseLinea> lineas = new ArrayList<>();

        BigDecimal aRecepcion = saldos.recepcionPendiente().min(saldo);
        if (aRecepcion.signum() > 0) {
            lineas.add(new PagoDesgloseLinea(
                    OrigenCobro.RECEPCION, aRecepcion, conceptoRecepcion(reserva), null));
            saldo = saldo.subtract(aRecepcion);
        }

        BigDecimal aTienda = saldos.tiendaPendiente().min(saldo);
        if (aTienda.signum() > 0) {
            lineas.addAll(repartirEntreConsumos(reserva.getId(), aTienda));
            saldo = saldo.subtract(aTienda);
        }

        if (saldo.signum() > 0) {
            // El registro del pago ya impide pagar más que el saldo total, así que
            // llegar aquí significa que la reserva cambió entre el cálculo y el cobro.
            // Se asigna a recepción para no perder ni inventar dinero, y se avisa.
            log.warn("Reserva {}: sobran {} sin origen asignado; se imputan a RECEPCION",
                    reserva.getId(), saldo);
            lineas.add(new PagoDesgloseLinea(
                    OrigenCobro.RECEPCION, saldo, conceptoRecepcion(reserva), null));
        }

        return lineas;
    }

    /**
     * Calcula cuánto queda por cobrar en cada caja, descontando lo ya asignado
     * por pagos anteriores de la reserva.
     *
     * @param reserva reserva a evaluar
     * @return saldos pendientes por origen, nunca negativos
     */
    private SaldosPorOrigen calcularSaldos(Reserva reserva) {
        PricingBreakdown desglose = pricingService.calcularDesglose(reserva);

        BigDecimal totalRecepcion = desglose.totalRecepcion() != null
                ? desglose.totalRecepcion() : BigDecimal.ZERO;
        BigDecimal totalTienda = desglose.totalFinal() != null
                ? desglose.totalFinal().subtract(totalRecepcion) : BigDecimal.ZERO;

        BigDecimal pagadoRecepcion = pagadoEn(reserva.getId(), OrigenCobro.RECEPCION);
        BigDecimal pagadoTienda = pagadoEn(reserva.getId(), OrigenCobro.TIENDA);

        return new SaldosPorOrigen(
                maxZero(totalRecepcion.subtract(pagadoRecepcion)),
                maxZero(totalTienda.subtract(pagadoTienda)));
    }

    /**
     * Suma lo ya asignado a un origen por los pagos completados de la reserva.
     * Los pagos reembolsados o cancelados no se descuentan del pendiente porque
     * su movimiento de caja sigue registrado.
     */
    private BigDecimal pagadoEn(Long reservaId, OrigenCobro origen) {
        BigDecimal total = desgloseRepository
                .sumByReservaIdAndOrigenAndPagoEstado(reservaId, origen, EstadoPago.COMPLETADO);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Reparte el monto asignado a tienda entre los consumos que aún no se han
     * cobrado. Los consumos se recorren en orden de id y se salta lo ya atribuido,
     * de forma que un consumo nunca se cobra dos veces.
     *
     * @param reservaId reserva a la que pertenecen los consumos
     * @param montoTienda monto a repartir entre consumos
     * @return líneas de origen TIENDA, una por consumo cubierto
     */
    private List<PagoDesgloseLinea> repartirEntreConsumos(Long reservaId, BigDecimal montoTienda) {
        List<Consumo> consumos = consumoRepository
                .findByReservaIdAndEstadoInOrderByIdAsc(reservaId, CONSUMOS_COBRABLES);

        List<PagoDesgloseLinea> lineas = new ArrayList<>();
        BigDecimal restante = montoTienda;
        for (Consumo consumo : consumos) {
            if (restante.signum() <= 0) {
                break;
            }
            BigDecimal yaAtribuido = consumo.getId() != null
                    ? atribuidoAlConsumo(consumo.getId()) : BigDecimal.ZERO;
            BigDecimal pendiente = maxZero(
                    consumo.getMontoTotal() != null ? consumo.getMontoTotal() : BigDecimal.ZERO)
                    .subtract(yaAtribuido);
            if (pendiente.signum() <= 0) {
                continue;
            }
            BigDecimal asignado = pendiente.min(restante);
            lineas.add(new PagoDesgloseLinea(
                    OrigenCobro.TIENDA, asignado, conceptoConsumo(consumo), consumo.getId()));
            restante = restante.subtract(asignado);
        }
        if (restante.signum() > 0) {
            // El total de tienda no coincide con la suma de consumos (por ejemplo si
            // un consumo fue anulado después del cálculo). Se agrupa para no perder el monto.
            log.warn("Reserva {}: {} de tienda sin consumo asignado; se agrupa en una línea",
                    reservaId, restante);
            lineas.add(new PagoDesgloseLinea(
                    OrigenCobro.TIENDA, restante, "Consumos de tienda (ajuste)", null));
        }
        return lineas;
    }

    /** Suma ya atribuida a un consumo por pagos completados. */
    private BigDecimal atribuidoAlConsumo(Long consumoId) {
        BigDecimal total = desgloseRepository
                .sumByConsumoIdAndPagoEstado(consumoId, EstadoPago.COMPLETADO);
        return total != null ? total : BigDecimal.ZERO;
    }

    private String conceptoConsumo(Consumo consumo) {
        String nombre = consumo.getProducto() != null ? consumo.getProducto().getNombre() : "Producto";
        String cantidad = consumo.getCantidad() != null && consumo.getCantidad() > 1
                ? " x" + consumo.getCantidad() : "";
        return limitar("Consumo: " + nombre + cantidad);
    }

    /** Concepto de la parte de recepción: la reserva y sus habitaciones. */
    private String conceptoRecepcion(Reserva reserva) {
        String habitaciones = reserva.getDetalles() == null ? "" : reserva.getDetalles().stream()
                .map(DetalleReserva::getHabitacion)
                .filter(Objects::nonNull)
                .map(h -> h.getNumero() != null ? h.getNumero() : h.getId().toString())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        String base = "Reserva " + reserva.getCodigoReserva();
        return limitar(habitaciones.isBlank() ? base : base + " - habitacion(es) " + habitaciones);
    }

    private String limitar(String concepto) {
        return concepto.length() <= MAX_CONCEPTO ? concepto : concepto.substring(0, MAX_CONCEPTO);
    }

    private static BigDecimal maxZero(BigDecimal valor) {
        return valor.signum() < 0 ? BigDecimal.ZERO : valor;
    }

    /** Saldos pendientes por origen de una reserva. */
    private record SaldosPorOrigen(BigDecimal recepcionPendiente, BigDecimal tiendaPendiente) {
    }
}
