package com.hotelcaciquebolivar.reservas.service.habitacion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;

/**
 * Componente que deriva el estado de ocupación de una habitación para una fecha específica.
 * Analiza el estado físico de la habitación y los detalles de reserva asociados para
 * determinar si la habitación está DISPONIBLE, OCUPADA, RESERVADA o EN_LIMPIEZA.
 * 
 * <p>La lógica de derivación considera múltiples niveles de prioridad:
 * primero verifica estados físicos especiales (MANTENIMIENTO, INHABILITADA),
 * luego filtra detalles no finalizados, y finalmente evalúa el estado
 * de cada detalle de reserva para determinar el estado resultante.</p>
 */
@Component
public class EstadoOcupacionDerivador {

    /**
     * Deriva el estado de ocupación de una habitación para una fecha específica.
     * Considera el estado físico actual de la habitación y los detalles de reserva asociados.
     * 
     * <p>Lógica de derivación:
     * <ul>
     *   <li>Si la habitación está en MANTENIMIENTO o INHABILITADA, retorna ese estado</li>
     *   <li>Si no hay detalles de reserva, retorna DISPONIBLE o EN_LIMPIEZA según el estado físico</li>
     *   <li>Para detalles con reserva CANCELADA o NO_SHOW, retorna DISPONIBLE o EN_LIMPIEZA</li>
     *   <li>Para detalles con reserva FINALIZADA, evalúa el estado del detalle</li>
     *   <li>Para otros estados, evalúa RESERVADA, OCUPADA o LIBERADA según la fecha</li>
     * </ul>
     *
     * @param habitacion habitación cuyo estado se desea derivar
     * @param detalles lista de detalles de reserva asociados a la habitación
     * @param fecha fecha para la que se desea conocer el estado
     * @return estado derivado de la habitación para la fecha especificada
     */
    public EstadoHabitacion derivar(Habitacion habitacion, List<DetalleReserva> detalles, LocalDate fecha) {
        EstadoHabitacion estadoFisico = habitacion.getEstado();

        if (estadoFisico == EstadoHabitacion.MANTENIMIENTO || estadoFisico == EstadoHabitacion.INHABILITADA) {
            return estadoFisico;
        }

        if (!fecha.equals(LocalDate.now()) && estadoFisico == EstadoHabitacion.EN_LIMPIEZA) {
            estadoFisico = EstadoHabitacion.DISPONIBLE;
        }

        if (detalles == null || detalles.isEmpty()) {
            if (fecha.isBefore(LocalDate.now())) {
                return EstadoHabitacion.DISPONIBLE;
            }
            if (estadoFisico == EstadoHabitacion.EN_LIMPIEZA) {
                return EstadoHabitacion.EN_LIMPIEZA;
            }
            return EstadoHabitacion.DISPONIBLE;
        }

        // Nivel 1: preferir no finalizados
        List<DetalleReserva> activos = detalles;
        if (detalles.size() > 1) {
            List<DetalleReserva> noFinalizados = detalles.stream()
                    .filter(d -> d.getReserva().getEstado() != EstadoReserva.FINALIZADA)
                    .toList();
            if (!noFinalizados.isEmpty()) {
                activos = noFinalizados;
            }
        }

        // Nivel 2: preferir OCUPADA/RESERVADA sobre LIBERADA
        if (activos.size() > 1) {
            List<DetalleReserva> ocupadosReservados = activos.stream()
                    .filter(d -> d.getEstado() == EstadoDetalleReserva.OCUPADA
                              || d.getEstado() == EstadoDetalleReserva.RESERVADA)
                    .toList();
            if (!ocupadosReservados.isEmpty()) {
                activos = ocupadosReservados;
            }
        }

        DetalleReserva detalle = activos.get(0);
        EstadoReserva estadoReserva = detalle.getReserva().getEstado();

        if (estadoReserva == EstadoReserva.CANCELADA
                || estadoReserva == EstadoReserva.NO_SHOW) {
            return estadoFisico == EstadoHabitacion.EN_LIMPIEZA
                    ? EstadoHabitacion.EN_LIMPIEZA
                    : EstadoHabitacion.DISPONIBLE;
        }

        if (estadoReserva == EstadoReserva.FINALIZADA) {
            if (detalle.getEstado() == EstadoDetalleReserva.OCUPADA
                    || detalle.getEstado() == EstadoDetalleReserva.RESERVADA
                || (detalle.getEstado() == EstadoDetalleReserva.LIBERADA
                    && fecha.isBefore(detalle.getReserva().getFechaCheckOut())
                    && fecha.isBefore(LocalDate.now()))) {
                return EstadoHabitacion.OCUPADA;
            }
            return estadoFisico == EstadoHabitacion.EN_LIMPIEZA
                    ? EstadoHabitacion.EN_LIMPIEZA
                    : EstadoHabitacion.DISPONIBLE;
        }

        return switch (detalle.getEstado()) {
            case RESERVADA -> EstadoHabitacion.RESERVADA;
            case OCUPADA -> {
                LocalDate checkOut = detalle.getReserva().getFechaCheckOut();
                if (fecha.isBefore(checkOut)) {
                    yield EstadoHabitacion.OCUPADA;
                }
                if (!fecha.isBefore(LocalDate.now()) && !checkOut.isBefore(fecha)) {
                    yield EstadoHabitacion.OCUPADA;
                }
                yield estadoFisico == EstadoHabitacion.EN_LIMPIEZA
                        ? EstadoHabitacion.EN_LIMPIEZA
                        : EstadoHabitacion.DISPONIBLE;
            }
            case LIBERADA -> {
                LocalDate checkOut = detalle.getReserva().getFechaCheckOut();
                if (fecha.isBefore(checkOut) && fecha.isBefore(LocalDate.now())) {
                    yield EstadoHabitacion.OCUPADA;
                }
                yield estadoFisico == EstadoHabitacion.EN_LIMPIEZA
                        ? EstadoHabitacion.EN_LIMPIEZA
                        : EstadoHabitacion.DISPONIBLE;
            }
            default -> estadoFisico == EstadoHabitacion.EN_LIMPIEZA
                    ? EstadoHabitacion.EN_LIMPIEZA
                    : EstadoHabitacion.DISPONIBLE;
        };
    }
}
