package com.hotelcaciquebolivar.reservas.repository.cliente;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;

import jakarta.persistence.criteria.Predicate;

/**
 * Clase de especificaciones para la búsqueda avanzada de clientes.
 * Proporciona métodos estáticos para construir consultas dinámicas usando JPA Specifications.
 */
public class ClienteSpecification {

    /**
     * Crea una especificación para búsqueda global de clientes por nombre, apellido, cédula o teléfono.
     * La búsqueda es case-insensitive y soporta múltiples palabras.
     *
     * @param q término de búsqueda
     * @return especificación para búsqueda global
     */
    public static Specification<Cliente> busquedaGlobal(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank())
                return cb.conjunction();
            String[] palabras = q.toLowerCase().trim().split("\\s+");
            List<Predicate> predicados = new ArrayList<>();
            for (String palabra : palabras) {
                String likePattern = "%" + palabra + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("nombreCompleto")), likePattern),
                        cb.like(cb.lower(root.get("apellidoCompleto")), likePattern),
                        cb.like(root.get("cedula"), palabra + "%"),
                        cb.like(root.get("telefono"), likePattern)));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    /**
     * Crea una especificación para buscar clientes cuyo nombre o apellido contenga el texto indicado.
     * La búsqueda es case-insensitive y soporta múltiples palabras.
     *
     * @param nombre nombre o apellido a buscar
     * @return especificación para filtro por nombre
     */
    public static Specification<Cliente> nombreContiene(String nombre) {
        return (root, query, cb) -> {
            if (nombre == null || nombre.isBlank())
                return cb.conjunction();
            String[] palabras = nombre.toLowerCase().trim().split("\\s+");
            List<Predicate> predicados = new ArrayList<>();
            for (String palabra : palabras) {
                String likePattern = "%" + palabra + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("nombreCompleto")), likePattern),
                        cb.like(cb.lower(root.get("apellidoCompleto")), likePattern)));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    /**
     * Crea una especificación para buscar clientes cuya cédula comience con el prefijo indicado.
     *
     * @param cedula prefijo de cédula a buscar
     * @return especificación para filtro por cédula
     */
    public static Specification<Cliente> cedulaEmpiezaCon(String cedula) {
        return (root, query, cb) -> {
            if (cedula == null || cedula.isBlank())
                return cb.conjunction();
            return cb.like(root.get("cedula"), cedula + "%");
        };
    }

    /**
     * Crea una especificación para buscar clientes cuyo teléfono contenga el texto indicado.
     * La búsqueda es case-insensitive.
     *
     * @param telefono teléfono a buscar
     * @return especificación para filtro por teléfono
     */
    public static Specification<Cliente> telefonoContiene(String telefono) {
        return (root, query, cb) -> {
            if (telefono == null || telefono.isBlank())
                return cb.conjunction();
            return cb.like(root.get("telefono"), "%" + telefono + "%");
        };
    }

    /**
     * Crea una especificación para buscar clientes por tipo de identificación específico.
     *
     * @param tipoId identificador del tipo de identificación
     * @return especificación para filtro por tipo de identificación
     */
    public static Specification<Cliente> tipoIdentificacionIgual(Long tipoId) {
        return (root, query, cb) -> {
            if (tipoId == null)
                return cb.conjunction();
            return cb.equal(root.get("tipoIdentificacion").get("id"), tipoId);
        };
    }

    /**
     * Crea una especificación para buscar clientes por estado de actividad.
     *
     * @param activo true para clientes activos, false para inactivos
     * @return especificación para filtro por estado de actividad
     */
    public static Specification<Cliente> activoIgual(Boolean activo) {
        return (root, query, cb) -> {
            if (activo == null)
                return cb.conjunction();
            return cb.equal(root.get("activo"), activo);
        };
    }
}
