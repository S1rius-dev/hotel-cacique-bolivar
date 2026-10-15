package com.hotelcaciquebolivar.reservas.service.reserva.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.dto.reserva.PricingLine;
import com.hotelcaciquebolivar.reservas.entity.consumo.EstadoConsumo;
import com.hotelcaciquebolivar.reservas.entity.reserva.AjusteReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.repository.consumo.ConsumoRepository;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;
import com.hotelcaciquebolivar.reservas.util.DineroUtil;

@Service
@Transactional(readOnly = true)
public class ReservaPricingServiceImpl implements ReservaPricingService {

    private final PrecioTemporalService precioTemporalService;
    private final ConsumoRepository consumoRepository;

    public ReservaPricingServiceImpl(PrecioTemporalService precioTemporalService,
                                     ConsumoRepository consumoRepository) {
        this.precioTemporalService = precioTemporalService;
        this.consumoRepository = consumoRepository;
    }

    @Override
    public BigDecimal calcularTotalDetalle(DetalleReserva detalle) {
        if (detalle.getReserva() == null) {
            return BigDecimal.ZERO;
        }
        long noches = detalle.getReserva().calcularNoches();
        if (noches <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal[] partes = resolverNochesYExtras(detalle, noches);
        BigDecimal totalNoches = partes[0];
        BigDecimal extras = partes[1];

        // Modo IVA: el precio unitario almacenado ya incluye IVA, así que las
        // noches se escalan por la relación conIva/base del snapshot congelado.
        // El modo sin IVA (o sin snapshot) deja el subtotal en precios base.
        if (Boolean.TRUE.equals(detalle.getPrecioUsadoIncluyeIva())) {
            BigDecimal conIva = detalle.getPrecioUnitarioConIva();
            BigDecimal base = detalle.getPrecioUnitarioBase();
            if (conIva != null && base != null && base.signum() > 0 && conIva.signum() > 0) {
                BigDecimal ratio = conIva.divide(base, 6, RoundingMode.HALF_UP);
                totalNoches = totalNoches.multiply(ratio);
            }
        }
        // COP sin decimales: el ratio conIva/base con escala 6 genera fracciones
        // de peso que, sumadas entre detalles, producían totales como 264.500,04.
        return DineroUtil.redondear(totalNoches.add(extras));
    }

    /**
     * Descompone el total del detalle en noches y personas extra. El desglose
     * congelado guarda ambas partes por noche; el camino dinámico (reservas
     * antiguas sin congelado) las calcula por separado.
     */
    private BigDecimal[] resolverNochesYExtras(DetalleReserva detalle, long noches) {
        // Si hay desglose congelado, usarlo (precios fijos al momento de crear)
        if (detalle.getDesgloseCongelado() != null && !detalle.getDesgloseCongelado().isBlank()) {
            try {
                BigDecimal totalNoches = BigDecimal.ZERO;
                BigDecimal totalExtras = BigDecimal.ZERO;
                // formato: [{"fecha":"2026-12-01","precioNoche":70000,"precioExtra":5000},...]
                String json = detalle.getDesgloseCongelado().trim();
                // parse simple sin librería pesada: extraer precioNoche y precioExtra por objeto
                int extrasCount = 0;
                if (detalle.getHabitacion() != null && detalle.getHabitacion().getTipo() != null) {
                    extrasCount = Math.max(0, detalle.getCantidadOcupantes() - detalle.getHabitacion().getTipo().getCapacidadBase());
                }
                // Contar objetos y sumar
                String[] parts = json.split("\\},");
                for (String part : parts) {
                    BigDecimal precioNoche = extractJsonBigDecimal(part, "precioNoche");
                    BigDecimal precioExtra = extractJsonBigDecimal(part, "precioExtra");
                    if (precioNoche != null) totalNoches = totalNoches.add(precioNoche);
                    if (extrasCount > 0 && precioExtra != null) totalExtras = totalExtras.add(precioExtra.multiply(BigDecimal.valueOf(extrasCount)));
                }
                if (totalNoches.add(totalExtras).compareTo(BigDecimal.ZERO) > 0) {
                    return new BigDecimal[]{totalNoches, totalExtras};
                }
            } catch (Exception ignored) {
                // fallback a lógica dinámica si el JSON está corrupto
            }
        }
        if (detalle.getHabitacion() == null || detalle.getHabitacion().getTipo() == null) {
            return new BigDecimal[]{detalle.calcularSubtotal(noches), BigDecimal.ZERO};
        }
        // Fallback para reservas antiguas sin congelado: resolver dinámico (compatibilidad)
        BigDecimal totalNoches = precioTemporalService.calcularTotalNoches(
                detalle.getHabitacion().getTipo(),
                detalle.getPrecioPorNoche(),
                detalle.getReserva().getFechaCheckIn(),
                detalle.getReserva().getFechaCheckOut());
        return new BigDecimal[]{totalNoches, calcularExtrasDetalle(detalle)};
    }

    @Override
    public BigDecimal calcularExtrasDetalle(DetalleReserva detalle) {
        if (detalle.getReserva() == null) {
            return BigDecimal.ZERO;
        }
        long noches = detalle.getReserva().calcularNoches();
        if (noches <= 0) {
            return BigDecimal.ZERO;
        }
        if (detalle.getHabitacion() == null || detalle.getHabitacion().getTipo() == null) {
            return BigDecimal.ZERO;
        }
        // Si hay congelado, los extras ya se suman en calcularTotalDetalle via JSON, evitar doble conteo
        if (detalle.getDesgloseCongelado() != null && !detalle.getDesgloseCongelado().isBlank()) {
            return BigDecimal.ZERO;
        }

        int capBase = detalle.getHabitacion().getTipo().getCapacidadBase();
        int extras = Math.max(0, detalle.getCantidadOcupantes() - capBase);

        if (extras <= 0) {
            return BigDecimal.ZERO;
        }
        return precioTemporalService.calcularTotalPersonasExtra(
                detalle.getHabitacion().getTipo(),
                detalle.getHabitacion().getTipo().getPrecioPersonaExtra(),
                extras,
                detalle.getReserva().getFechaCheckIn(),
                detalle.getReserva().getFechaCheckOut());
    }

    @Override
    public void congelarPreciosDetalle(DetalleReserva detalle, LocalDate checkIn, LocalDate checkOut) {
        if (detalle.getHabitacion() == null || detalle.getHabitacion().getTipo() == null || checkIn == null || checkOut == null) {
            return;
        }
        var tipo = detalle.getHabitacion().getTipo();
        // resolver precio por noche usando tarifas vigentes hoy (precio temporal o base)
        BigDecimal precioBaseNoche = precioTemporalService.resolverPrecioNoche(tipo, checkIn);
        BigDecimal precioExtraSnap = precioTemporalService.resolverPrecioPersonaExtra(tipo, checkIn);
        // Si hay tarifa temporal que varía por noche, snapshot del primer día no basta;
        // guardamos desglose por noche para soporte cruce de temporadas
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (LocalDate fecha = checkIn; fecha.isBefore(checkOut); fecha = fecha.plusDays(1)) {
            BigDecimal precioNoche = precioTemporalService.resolverPrecioNoche(tipo, fecha);
            BigDecimal precioExtra = precioTemporalService.resolverPrecioPersonaExtra(tipo, fecha);
            if (!first) sb.append(",");
            sb.append("{\"fecha\":\"").append(fecha).append("\",\"precioNoche\":").append(precioNoche).append(",\"precioExtra\":").append(precioExtra).append("}");
            first = false;
        }
        sb.append("]");
        detalle.setDesgloseCongelado(sb.toString());
        detalle.setPrecioPersonaExtraSnapshot(precioExtraSnap);
        // precioPorNoche se mantiene como precio de la primera noche (compat) o promedio; el desglose manda
        if (detalle.getPrecioPorNoche() == null) {
            detalle.setPrecioPorNoche(precioBaseNoche);
        }
        // Snapshot base/con IVA: alimenta el toggle de modo de IVA (ReservaService#togglePrecioModo).
        // Se resuelve siempre con la tarifa base vigente, nunca desde precioPorNoche: ese campo lo
        // muta el toggle (queda en el precio aplicado), y derivar el snapshot de él corrompería la
        // relación base/con-IVA al re-congelar tras un cambio de modo.
        detalle.setPrecioUnitarioBase(precioBaseNoche);
        detalle.setPrecioUnitarioConIva(precioTemporalService.resolverPrecioNocheConIva(tipo, checkIn));
        if (detalle.getPrecioUsadoIncluyeIva() == null) {
            detalle.setPrecioUsadoIncluyeIva(false);
        }
    }

    private BigDecimal extractJsonBigDecimal(String jsonPart, String key) {
        String search = "\"" + key + "\":";
        int idx = jsonPart.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        int end = start;
        while (end < jsonPart.length() && (Character.isDigit(jsonPart.charAt(end)) || jsonPart.charAt(end) == '.' || jsonPart.charAt(end) == '-')) {
            end++;
        }
        if (start >= end) return null;
        try {
            return new BigDecimal(jsonPart.substring(start, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public BigDecimal calcularTotalReserva(Reserva reserva) {
        return calcularDesglose(reserva).totalFinal();
    }

    @Override
    public PricingBreakdown calcularDesglose(Reserva reserva) {
        BigDecimal subtotal = calcularSubtotal(reserva);
        // Consumos (tienda) cargados a la reserva
        BigDecimal totalConsumos = BigDecimal.ZERO;
        try {
            if (reserva.getId() != null) {
                BigDecimal c = consumoRepository.sumByReservaIdAndEstadoIn(
                        reserva.getId(), List.of(EstadoConsumo.PENDIENTE, EstadoConsumo.FACTURADO));
                if (c != null) totalConsumos = c;
            }
        } catch (Exception ignored) {}
        List<PricingLine> lineas = new ArrayList<>();

        BigDecimal totalCargos = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        boolean exencionAplicada = false;
        BigDecimal totalFijo = null;

        if (reserva.getAjustes() != null) {
            for (AjusteReserva ajuste : reserva.getAjustes()) {
                if (!ajuste.getActivo()) {
                    continue;
                }

                if (ajuste.getModoCalculo() == com.hotelcaciquebolivar.reservas.entity.reserva.ModoCalculo.TOTAL) {
                    totalFijo = ajuste.getValor() != null ? ajuste.getValor() : BigDecimal.ZERO;
                    lineas.add(new PricingLine(
                            ajuste.getId(), ajuste.getTipoAjuste(),
                            ajuste.getModoCalculo(), ajuste.getConcepto(),
                            ajuste.getValor(), totalFijo));
                    continue;
                }

                BigDecimal impacto = calcularImpacto(ajuste, subtotal);
                lineas.add(new PricingLine(
                        ajuste.getId(), ajuste.getTipoAjuste(),
                        ajuste.getModoCalculo(), ajuste.getConcepto(),
                        ajuste.getValor(), impacto));

                switch (ajuste.getTipoAjuste()) {
                    case CARGO ->
                        totalCargos = totalCargos.add(impacto);
                    case DESCUENTO ->
                        totalDescuentos = totalDescuentos.add(impacto);
                    case CORTESIA ->
                        exencionAplicada = true;
                }
            }
        }

        BigDecimal totalFinal;
        BigDecimal totalRecepcion;
        if (totalFijo != null) {
            // El total fijado por el usuario es el monto final de la reserva y ya
            // incluye todo; los consumos nunca se suman en esta rama, por lo que
            // la reserva entera pertenece a recepción.
            totalFinal = totalFijo;
            totalRecepcion = totalFijo;
        } else if (exencionAplicada) {
            // Exención: no hay nada que cobrar, tampoco por consumos.
            totalFinal = BigDecimal.ZERO;
            totalRecepcion = BigDecimal.ZERO;
        } else {
            totalRecepcion = subtotal.add(totalCargos).subtract(totalDescuentos);
            totalFinal = totalRecepcion.add(totalConsumos);
        }
        // COP sin decimales: defensa final (los consumos pueden traer centavos
        // si un precio de producto tiene escala 2 fraccionaria).
        totalRecepcion = DineroUtil.redondear(totalRecepcion);
        totalFinal = DineroUtil.redondear(totalFinal);

        return new PricingBreakdown(
                subtotal, totalCargos, totalDescuentos,
                exencionAplicada, totalFinal, lineas, totalConsumos, totalRecepcion);
    }

    private BigDecimal calcularSubtotal(Reserva reserva) {
        if (reserva.getDetalles() == null || reserva.getDetalles().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return reserva.getDetalles().stream()
                .filter(d -> d.getEstado() != EstadoDetalleReserva.CANCELADA)
                .map(this::calcularTotalDetalle)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularImpacto(AjusteReserva ajuste, BigDecimal subtotal) {
        return switch (ajuste.getModoCalculo()) {
            case FIJO ->
                ajuste.getValor() != null ? ajuste.getValor() : BigDecimal.ZERO;
            case PORCENTUAL -> {
                if (ajuste.getValor() == null) {
                    yield BigDecimal.ZERO;
                }
                yield DineroUtil.redondear(
                subtotal.multiply(ajuste.getValor())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            }
            case TOTAL ->
                BigDecimal.ZERO;
        };
    }
}
