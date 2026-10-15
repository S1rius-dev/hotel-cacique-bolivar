package com.hotelcaciquebolivar.reservas.service.reserva.disponibilidad;

import java.time.LocalDate;
import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;

public interface DisponibilidadService {
    List<DisponibilidadResponse> consultarDisponibilidad(LocalDate checkIn, LocalDate checkOut);
}
