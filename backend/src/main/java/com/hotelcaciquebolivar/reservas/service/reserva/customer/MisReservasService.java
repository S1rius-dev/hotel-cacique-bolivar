package com.hotelcaciquebolivar.reservas.service.reserva.customer;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;

import com.hotelcaciquebolivar.reservas.dto.reserva.customer.MisReservasResponse;

public interface MisReservasService {

    List<MisReservasResponse> listarMisReservas(Authentication authentication);

    MisReservasResponse cancelarMiReserva(Authentication authentication, Long reservaId);

    MisReservasResponse editarFechasMiReserva(Authentication authentication, Long reservaId,
            LocalDate nuevaFechaCheckIn, LocalDate nuevaFechaCheckOut);
}
