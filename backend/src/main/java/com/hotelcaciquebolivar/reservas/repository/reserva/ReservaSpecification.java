package com.hotelcaciquebolivar.reservas.repository.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;

import jakarta.persistence.criteria.Predicate;

/**
 * Clase de especificaciones para la búsqueda avanzada de reservas.
 * Proporciona métodos estáticos para construir consultas dinámicas usando JPA Specifications.
 */
public class ReservaSpecification {

    /**
     * Crea una especificación para buscar reservas cuyo código contenga el texto indicado.
     *
     * @param codigo texto a buscar en el código de reserva
     * @return especificación para filtro por código
     */
    public static Specification<Reserva> codigoContiene(String codigo) {
        return (root, query, cb) -> {
            if (codigo == null || codigo.isBlank())
                return cb.conjunction();
            return cb.like(root.get("codigoReserva"), "%" + codigo + "%");
        };
    }

    /**
     * Crea una especificación para buscar reservas por nombre, apellido o cédula del titular.
     * La búsqueda es case-insensitive y soporta múltiples palabras.
     *
     * @param query término de búsqueda
     * @return especificación para búsqueda de cliente
     */
    public static Specification<Reserva> clienteContiene(String query) {
        return (root, q, cb) -> {
            if (query == null || query.isBlank())
                return cb.conjunction();
            String[] palabras = query.toLowerCase().trim().split("\\s+");
            List<Predicate> predicados = new ArrayList<>();
            for (String palabra : palabras) {
                String likePattern = "%" + palabra + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("titularReserva").get("nombreCompleto")), likePattern),
                        cb.like(cb.lower(root.get("titularReserva").get("apellidoCompleto")), likePattern),
                        cb.like(root.get("titularReserva").get("cedula"), palabra + "%")));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    /**
     * Crea una especificación para filtrar reservas por estado específico.
     *
     * @param estado estado de la reserva
     * @return especificación para filtro por estado
     */
    public static Specification<Reserva> estadoIgual(EstadoReserva estado) {
        return (root, query, cb) -> {
            if (estado == null)
                return cb.conjunction();
            return cb.equal(root.get("estado"), estado);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por origen específico.
     *
     * @param origen origen de la reserva (WEB, TELEFONO, WALK_IN, etc.)
     * @return especificación para filtro por origen
     */
    public static Specification<Reserva> origenIgual(OrigenReserva origen) {
        return (root, query, cb) -> {
            if (origen == null)
                return cb.conjunction();
            return cb.equal(root.get("origen"), origen);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por rango de fechas de check-in.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return especificación para filtro por fecha de check-in
     */
    public static Specification<Reserva> fechaCheckInEntre(LocalDate desde, LocalDate hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null)
                return cb.conjunction();
            if (desde != null && hasta != null)
                return cb.between(root.get("fechaCheckIn"), desde, hasta);
            if (desde != null)
                return cb.greaterThanOrEqualTo(root.get("fechaCheckIn"), desde);
            return cb.lessThanOrEqualTo(root.get("fechaCheckIn"), hasta);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por rango de fechas de check-out.
     *
     * @param desde fecha de inicio del rango
     * @param hasta fecha de fin del rango
     * @return especificación para filtro por fecha de check-out
     */
    public static Specification<Reserva> fechaCheckOutEntre(LocalDate desde, LocalDate hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null)
                return cb.conjunction();
            if (desde != null && hasta != null)
                return cb.between(root.get("fechaCheckOut"), desde, hasta);
            if (desde != null)
                return cb.greaterThanOrEqualTo(root.get("fechaCheckOut"), desde);
            return cb.lessThanOrEqualTo(root.get("fechaCheckOut"), hasta);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por rango de fechas de creación.
     *
     * @param desde fecha y hora de inicio del rango
     * @param hasta fecha y hora de fin del rango
     * @return especificación para filtro por fecha de creación
     */
    public static Specification<Reserva> fechaCreacionEntre(LocalDateTime desde, LocalDateTime hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null)
                return cb.conjunction();
            if (desde != null && hasta != null)
                return cb.between(root.get("fechaCreacion"), desde, hasta);
            if (desde != null)
                return cb.greaterThanOrEqualTo(root.get("fechaCreacion"), desde);
            return cb.lessThanOrEqualTo(root.get("fechaCreacion"), hasta);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por ID del titular.
     *
     * @param titularId identificador del titular de la reserva
     * @return especificación para filtro por titular
     */
    public static Specification<Reserva> titularIdEquals(Long titularId) {
        return (root, query, cb) -> {
            if (titularId == null) return cb.conjunction();
            return cb.equal(root.get("titularReserva").get("id"), titularId);
        };
    }

    /**
     * Crea una especificación para filtrar reservas por una lista de estados.
     *
     * @param estados lista de estados de reserva a incluir
     * @return especificación para filtro por lista de estados
     */
    public static Specification<Reserva> estadoIn(List<EstadoReserva> estados) {
        return (root, query, cb) -> {
            if (estados == null || estados.isEmpty()) return cb.conjunction();
            return root.get("estado").in(estados);
        };
    }

    /**
     * Crea una especificación para filtrar reservas con fecha de check-out igual o posterior a una fecha.
     *
     * @param desde fecha mínima de check-out
     * @return especificación para filtro por check-out desde
     */
    public static Specification<Reserva> checkOutDesde(LocalDate desde) {
        return (root, query, cb) -> {
            if (desde == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("fechaCheckOut"), desde);
        };
    }
}
