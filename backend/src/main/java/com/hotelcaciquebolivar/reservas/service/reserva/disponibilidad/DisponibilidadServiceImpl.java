package com.hotelcaciquebolivar.reservas.service.reserva.disponibilidad;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.HabitacionMapper;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final HabitacionRepository habitacionRepository;
    private final HabitacionMapper habitacionMapper;
    private final PrecioTemporalService precioTemporalService;

    @Override
    public List<DisponibilidadResponse> consultarDisponibilidad(LocalDate checkIn, LocalDate checkOut) {
        validarFechasDisponibilidad(checkIn, checkOut);
        LocalDate queryCheckOut = checkIn.equals(checkOut) ? checkOut.plusDays(1) : checkOut;
        List<Habitacion> disponibles = habitacionRepository.findAllDisponibles(checkIn, queryCheckOut);
        return disponibles.stream()
                .map(h -> new DisponibilidadResponse(
                        h.getId(),
                        h.getNumero(),
                        h.getTipo().getNombre(),
                        precioTemporalService.resolverPrecioNoche(h.getTipo(), checkIn),
                        h.getTipo().getCapacidadBase(),
                        h.getTipo().getCapacidadMaxima(),
                        precioTemporalService.resolverPrecioPersonaExtra(h.getTipo(), checkIn)))
                .toList();
    }

    private void validarFechasDisponibilidad(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut == null || checkIn == null) {
            throw new ValidationException("fechas", "las fechas son obligatorias");
        }
        if (checkOut.isBefore(checkIn)) {
            throw new ValidationException("fechaCheckOut", "debe ser posterior o igual a fechaCheckIn");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("fechaCheckIn", "no puede ser una fecha pasada");
        }
        long noches = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (noches < 0) {
            throw new ValidationException("fechas", "la estadía mínima es de 1 noche");
        }
        if (noches > Reserva.MAX_DIAS_ESTADIA) {
            throw new ValidationException("fechas",
                    "la estadía máxima es de " + Reserva.MAX_DIAS_ESTADIA + " días");
        }
    }
}
