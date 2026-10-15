package com.hotelcaciquebolivar.reservas.service.reserva;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;

/**
 * Utilidad estática para gestionar las transiciones de estado de una reserva.
 * Proporciona lógica centralizada de validación y aplicación de cambios de estado
 * según las reglas de negocio del sistema de reservas.
 */
public final class EstadoUtils {

    private EstadoUtils() {
    }

    /**
     * Aplica una transición de estado a una reserva según las reglas de negocio establecidas.
     * Valida que el estado destino sea alcanzable desde el estado actual y ejecuta
     * las acciones asociadas al cambio de estado.
     *
     * <p>Estados y transiciones permitidas:
     * <ul>
     *   <li>{@code CONFIRMADA}: confirma la reserva</li>
     *   <li>{@code CANCELADA}: cancela la reserva (no permitido desde FINALIZADA, CANCELADA, EN_CURSO o NO_SHOW)</li>
     *   <li>{@code NO_SHOW}: marca la reserva como no presentada</li>
     *   <li>{@code EN_CURSO}: inicia hospedaje (solo desde CONFIRMADA, PENDIENTE o PENDIENTE_PAGO)</li>
     *   <li>{@code FINALIZADA}: finaliza la reserva (solo desde EN_CURSO)</li>
     * </ul>
     *
     * <p>Cuando se cancela una reserva o se marca como NO_SHOW, las habitaciones asociadas
     * que estén en estado RESERVADA pasan a DISPONIBLE.
     *
     * @param reserva   la reserva a la que se le aplicará el cambio de estado
     * @param nuevoEstado el estado destino a aplicar
     * @throws ValidationException si la transición no es válida según las reglas de negocio
     */
    public static void aplicarTransicionEstado(Reserva reserva, EstadoReserva nuevoEstado) {
        EstadoReserva actual = reserva.getEstado();

        switch (nuevoEstado) {
            case CONFIRMADA -> {
                reserva.confirmar();
                return;
            }
            case CANCELADA -> {
                if (actual == EstadoReserva.FINALIZADA || actual == EstadoReserva.CANCELADA
                        || actual == EstadoReserva.EN_CURSO || actual == EstadoReserva.NO_SHOW) {
                    String motivo = switch (actual) {
                        case FINALIZADA -> "finalizada";
                        case CANCELADA -> "ya cancelada";
                        case EN_CURSO -> "en curso. Debe realizar check-out primero";
                        case NO_SHOW -> "marcada como NO_SHOW";
                        default -> actual.name();
                    };
                    throw new ValidationException("estado",
                            "no se puede cancelar una reserva " + motivo);
                }
                reserva.cancelar();
            }
            case NO_SHOW -> {
                reserva.marcarNoShow();
            }
            case EN_CURSO -> {
                if (actual != EstadoReserva.CONFIRMADA && actual != EstadoReserva.PENDIENTE && actual != EstadoReserva.PENDIENTE_PAGO) {
                    throw new ValidationException("estado",
                            "solo se puede iniciar hospedaje con una reserva CONFIRMADA, PENDIENTE o PENDIENTE_PAGO");
                }
                reserva.setEstado(nuevoEstado);
            }
            case FINALIZADA -> {
                if (actual != EstadoReserva.EN_CURSO) {
                    throw new ValidationException("estado",
                            "solo se puede finalizar una reserva en estado EN_CURSO");
                }
                reserva.setEstado(nuevoEstado);
            }
            default ->
                throw new ValidationException("estado", "estado no reconocido: " + nuevoEstado);
        }

        if (nuevoEstado == EstadoReserva.CANCELADA || nuevoEstado == EstadoReserva.NO_SHOW) {
            reserva.getDetalles().forEach(d -> {
                EstadoHabitacion h = d.getHabitacion().getEstado();
                if (h == EstadoHabitacion.RESERVADA) {
                    d.getHabitacion().setEstado(EstadoHabitacion.DISPONIBLE);
                }
            });
        }
    }
}
