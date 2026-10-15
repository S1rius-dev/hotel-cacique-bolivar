package com.hotelcaciquebolivar.reservas.exception.reserva;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando se intenta reservar una habitación que no está disponible
 * en el rango de fechas solicitado. Indica conflicto con reservas existentes o
 * mantenimiento programado. Retorna error HTTP 409 (Conflict).
 * Hereda de {@link BusinessException} con código de error "ROOM_NOT_AVAILABLE".
 */
public class RoomNotAvailableException extends BusinessException {

    /**
     * Constructor de RoomNotAvailableException.
     *
     * @param roomNumber número de la habitación no disponible
     * @param start      fecha de inicio del rango solicitado
     * @param end        fecha de fin del rango solicitado
     */
    public RoomNotAvailableException(
            String roomNumber,
            LocalDate start,
            LocalDate end
    ) {
        super(
                "ROOM_NOT_AVAILABLE",
                "La habitacion " + roomNumber
                + " no esta disponible entre " + start
                + " y " + end,
                HttpStatus.CONFLICT
        );
    }
}
