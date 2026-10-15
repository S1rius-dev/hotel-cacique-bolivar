package com.hotelcaciquebolivar.reservas.service.payment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

/**
 * Fábrica de generadores de referencia por pasarela.
 */
@Component
public class PaymentReferenceGeneratorFactory {

    private final Map<PaymentGatewayType, PaymentReferenceGenerator> generators;
    private final PaymentReferenceGenerator fallback;

    public PaymentReferenceGeneratorFactory(List<PaymentReferenceGenerator> beans) {
        this.generators = new EnumMap<>(PaymentGatewayType.class);
        PaymentReferenceGenerator first = null;
        for (PaymentReferenceGenerator g : beans) {
            if (g.type() != null) {
                generators.put(g.type(), g);
            }
            if (first == null) first = g;
        }
        this.fallback = first;
    }

    public PaymentReferenceGenerator para(PaymentGatewayType tipo) {
        PaymentReferenceGenerator g = generators.get(tipo);
        if (g != null) return g;
        if (fallback != null) return fallback;
        throw new IllegalStateException("No hay generador de referencia para " + tipo);
    }
}
