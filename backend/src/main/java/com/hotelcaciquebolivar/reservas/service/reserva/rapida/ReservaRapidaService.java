package com.hotelcaciquebolivar.reservas.service.reserva.rapida;

import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRapidaRequest;

public interface ReservaRapidaService {
    ReservaResponse crearReservaRapida(ReservaRapidaRequest dto);
}
