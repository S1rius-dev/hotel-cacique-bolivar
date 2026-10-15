package com.hotelcaciquebolivar.reservas.exception.reserva;

import org.springframework.http.HttpStatus;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;

/**
 * Excepción que se lanza cuando el sistema de reservas en línea está deshabilitado
 * temporalmente por razones administrativas. Incluye información de contacto
 * (teléfono y WhatsApp) para que los usuarios puedan reservar por otros medios.
 * Retorna error HTTP 409 (Conflict).
 * Hereda de {@link BusinessException} con código de error "RESERVAS_BLOQUEADAS".
 */
public class ReservasBloqueadasException extends BusinessException {

    private static final String TELEFONO_CONTACTO = "+57 300 0000000";
    private static final String WHATSAPP_CONTACTO = "573000000000";

    /**
     * Constructor de ReservasBloqueadasException.
     *
     * @param motivo razón por la cual las reservas están bloqueadas (puede ser null o vacío)
     */
    public ReservasBloqueadasException(String motivo) {
        super(
                "RESERVAS_BLOQUEADAS",
                "Las reservas en l\u00ednea est\u00e1n deshabilitadas temporalmente. Motivo: "
                        + motivoOrDefault(motivo)
                        + ". Si deseas reservar con nosotros, comun\u00edcate al "
                        + TELEFONO_CONTACTO
                        + " o por WhatsApp al "
                        + WHATSAPP_CONTACTO + ".",
                HttpStatus.CONFLICT
        );
    }

    private static String motivoOrDefault(String motivo) {
        return (motivo == null || motivo.isBlank()) ? "no especificado" : motivo;
    }
}