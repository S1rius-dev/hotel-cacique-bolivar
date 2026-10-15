package com.hotelcaciquebolivar.reservas.service.payment.gateway.bold;

import java.time.Year;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.service.payment.PaymentReferenceGenerator;

/**
 * Generador de referencia para Bold (stub).
 * Usa el mismo formato que Wompi hasta definir formato propio de Bold.
 */
@Component
public class BoldReferenceGenerator implements PaymentReferenceGenerator {

    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.BOLD;
    }

    @Override
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
