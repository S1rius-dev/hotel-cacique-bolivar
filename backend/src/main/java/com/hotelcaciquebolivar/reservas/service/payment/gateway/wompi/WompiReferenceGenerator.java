package com.hotelcaciquebolivar.reservas.service.payment.gateway.wompi;

import java.time.Year;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentReferenceGenerator;

/**
 * Genera la referencia única de pago que Wompi exige para cada
 * transacción (Paso 2 de la integración de checkout).
 * Formato: HOTEL{año}{códigoReserva}{uidnano36}
 * Limitada a 255 caracteres para compatibilidad con Wompi.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class WompiReferenceGenerator implements PaymentReferenceGenerator {

    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.WOMPI;
    }

    /**
     * Genera una referencia única y determinista para una transacción Wompi.
     * Combina el año actual, código de reserva (sanitizado) y un identificador único.
     *
     * @param reservaId identificador de la reserva
     * @param codigoReserva código alfanumérico de la reserva
     * @return referencia única de pago (máximo 255 caracteres)
     */
    public String generar(Long reservaId, String codigoReserva) {
        String base = codigoReserva != null
                ? codigoReserva.replaceAll("[^A-Za-z0-9]", "")
                : "RES" + reservaId;
        String anio = String.valueOf(Year.now().getValue());
        String uid = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        String referencia = "HOTEL" + anio + base + uid;
        return referencia.length() > 255 ? referencia.substring(0, 255) : referencia;
    }
}
