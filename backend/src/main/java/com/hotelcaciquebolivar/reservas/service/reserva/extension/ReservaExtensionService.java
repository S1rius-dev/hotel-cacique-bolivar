package com.hotelcaciquebolivar.reservas.service.reserva.extension;

import java.time.LocalDate;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;

public interface ReservaExtensionService {
    ReservaResponse extenderEstadia(Long reservaId, LocalDate nuevaFechaCheckOut);
}
