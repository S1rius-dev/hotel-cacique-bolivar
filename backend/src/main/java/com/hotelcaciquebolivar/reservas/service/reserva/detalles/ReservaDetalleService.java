package com.hotelcaciquebolivar.reservas.service.reserva.detalles;

import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.reserva.ActualizarDetalleRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;

public interface ReservaDetalleService {

    ReservaResponse agregarHuespedes(Long reservaId, Long detalleId, List<Long> huespedesIds);

    ReservaResponse removerHuesped(Long reservaId, Long detalleId, Long detalleHuespedId);

    ReservaResponse removerDetalle(Long reservaId, Long detalleId);

    ReservaResponse agregarDetalle(Long reservaId, DetalleReservaRequest dto);

    ReservaResponse actualizarDetalle(Long reservaId, Long detalleId, ActualizarDetalleRequest dto);
}
