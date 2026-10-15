package com.hotelcaciquebolivar.reservas.repository.registro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;

import jakarta.persistence.criteria.Predicate;

/**
 * Clase de especificaciones para la búsqueda avanzada de huéspedes registrados.
 * Proporciona métodos estáticos para construir consultas dinámicas usando JPA Specifications.
 */
public class HuespedRegistroSpecification {

    /**
     * Crea una especificación para filtrar huéspedes por rango de fechas de check-in.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return especificación para filtro por fecha de check-in
     */
    public static Specification<DetalleReservaHuesped> fechaCheckInEntre(LocalDate desde, LocalDate hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null)
                return cb.conjunction();
            var reservaJoin = root.get("detalleReserva").get("reserva");
            if (desde != null && hasta != null) {
                return cb.and(
                        cb.lessThanOrEqualTo(reservaJoin.get("fechaCheckIn"), hasta),
                        cb.greaterThanOrEqualTo(reservaJoin.get("fechaCheckOut"), desde));
            }
            if (desde != null) {
                return cb.greaterThanOrEqualTo(reservaJoin.get("fechaCheckOut"), desde);
            }
            return cb.lessThanOrEqualTo(reservaJoin.get("fechaCheckIn"), hasta);
        };
    }

    /**
     * Crea una especificación para filtrar huéspedes por rango de fechas de check-out.
     * Actualmente retorna una conjugación sin filtros (método pendiente de implementación).
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return especificación para filtro por fecha de check-out
     */
    public static Specification<DetalleReservaHuesped> fechaCheckOutEntre(LocalDate desde, LocalDate hasta) {
        return (root, query, cb) -> cb.conjunction();
    }

    /**
     * Crea una especificación para filtrar huéspedes por número de habitación.
     *
     * @param numero número de la habitación
     * @return especificación para filtro por número de habitación
     */
    public static Specification<DetalleReservaHuesped> numeroHabitacionIgual(String numero) {
        return (root, query, cb) -> {
            if (numero == null || numero.isBlank())
                return cb.conjunction();
            return cb.equal(root.get("detalleReserva").get("habitacion").get("numero"), numero);
        };
    }

    /**
     * Crea una especificación para filtrar huéspedes por tipo de habitación.
     *
     * @param tipoId identificador del tipo de habitación
     * @return especificación para filtro por tipo de habitación
     */
    public static Specification<DetalleReservaHuesped> tipoHabitacionIgual(Long tipoId) {
        return (root, query, cb) -> {
            if (tipoId == null)
                return cb.conjunction();
            return cb.equal(root.get("detalleReserva").get("habitacion").get("tipo").get("id"), tipoId);
        };
    }

    /**
     * Crea una especificación para búsqueda global de huéspedes por nombre, apellido o cédula.
     * La búsqueda es case-insensitive y soporta múltiples palabras.
     *
     * @param texto término de búsqueda
     * @return especificación para búsqueda global de huéspedes
     */
    public static Specification<DetalleReservaHuesped> busquedaHuesped(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank())
                return cb.conjunction();
            String[] palabras = texto.toLowerCase().trim().split("\\s+");
            List<Predicate> predicados = new ArrayList<>();
            var clienteJoin = root.get("cliente");
            for (String palabra : palabras) {
                String likePattern = "%" + palabra + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(clienteJoin.get("nombreCompleto")), likePattern),
                        cb.like(cb.lower(clienteJoin.get("apellidoCompleto")), likePattern),
                        cb.like(clienteJoin.get("cedula"), palabra + "%")));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    /**
     * Crea una especificación para filtrar huéspedes por estado del detalle de reserva.
     *
     * @param estado estado del detalle de reserva (ASIGNADO, CHECK_IN, CHECK_OUT, etc.)
     * @return especificación para filtro por estado del detalle
     */
    public static Specification<DetalleReservaHuesped> estadoDetalleIgual(String estado) {
        return (root, query, cb) -> {
            if (estado == null || estado.isBlank())
                return cb.conjunction();
            return cb.equal(root.get("detalleReserva").get("estado"), estado);
        };
    }
}
