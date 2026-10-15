package com.hotelcaciquebolivar.reservas.service.reserva.customer.assignment;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;

/**
 * Estrategia de asignación de habitaciones que prioriza las menos utilizadas.
 * Asigna habitaciones ordenando por la fecha de último uso (las que nunca se usaron
 * o las que se usaron hace más tiempo tienen prioridad). Si la cantidad disponible
 * es menor o igual a la requerida, retorna todas sin ordenar.
 */
@Component
public class LeastUsedRoomAssignmentStrategy implements RoomAssignmentStrategy {

    private final DetalleReservaRepository detalleReservaRepository;

    public LeastUsedRoomAssignmentStrategy(DetalleReservaRepository detalleReservaRepository) {
        this.detalleReservaRepository = detalleReservaRepository;
    }

    /**
     * {@inheritDoc}
     * Si la cantidad disponible es menor o igual a la requerida, retorna todas sin ordenar.
     * Consulta la fecha de último uso de cada habitación en la base de datos,
     * ordena por fecha de último uso ascendente (las menos usadas primero)
     * y retorna las primeras N habitaciones.
     *
     * @param disponibles      lista de habitaciones disponibles
     * @param cantidadRequerida cantidad de habitaciones a asignar
     * @return lista de habitaciones asignadas ordenadas por menor uso
     */
    @Override
    public List<Habitacion> asignar(List<Habitacion> disponibles, int cantidadRequerida) {
        if (disponibles.size() <= cantidadRequerida) {
            return disponibles;
        }

        List<Long> ids = disponibles.stream().map(Habitacion::getId).toList();
        Map<Long, LocalDateTime> ultimoUso = detalleReservaRepository
                .findUltimoUsoPorHabitaciones(ids)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (LocalDateTime) arr[1]));

        return disponibles.stream()
                .sorted(Comparator.comparing(
                        h -> ultimoUso.getOrDefault(h.getId(), null),
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .limit(cantidadRequerida)
                .toList();
    }
}
