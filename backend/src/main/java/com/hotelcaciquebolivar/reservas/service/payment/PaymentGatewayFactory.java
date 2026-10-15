package com.hotelcaciquebolivar.reservas.service.payment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.config.payment.PaymentProperties;
import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;
import com.hotelcaciquebolivar.reservas.exception.payment.PaymentGatewayException;

/**
 * Fábrica de pasarelas de pago que registra las implementaciones disponibles
 * y permite obtener la pasarela activa según la configuración o por tipo específico.
 *
 * @author Hotel Cacique Bolívar
 */
@Component
public class PaymentGatewayFactory {

    private final PaymentProperties properties;
    private final Map<PaymentGatewayType, PaymentGateway> gateways;

    /**
     * Construye la fábrica registrando todas las implementaciones de {@link PaymentGateway}
     * inyectadas en un mapa indexado por tipo.
     *
     * @param properties propiedades de configuración de pagos
     * @param gatewayBeans lista de implementaciones de pasarela disponibles
     */
    public PaymentGatewayFactory(
            PaymentProperties properties,
            List<PaymentGateway> gatewayBeans) {
        this.properties = properties;
        this.gateways = new EnumMap<>(PaymentGatewayType.class);
        for (PaymentGateway gateway : gatewayBeans) {
            this.gateways.put(gateway.type(), gateway);
        }
    }

    /**
     * Retorna la pasarela de pago configurada como activa en properties.
     *
     * @return implementación de la pasarela activa
     * @throws PaymentGatewayException si no existe implementación para el tipo configurado
     */
    public PaymentGateway activa() {
        PaymentGatewayType tipo = properties.getGateway();
        PaymentGateway gateway = gateways.get(tipo);
        if (gateway == null) {
            throw new PaymentGatewayException(
                    "No hay implementación registrada para la pasarela " + tipo);
        }
        return gateway;
    }

    /**
     * Retorna la implementación de pasarela para un tipo específico.
     *
     * @param tipo tipo de pasarela a buscar
     * @return implementación de la pasarela solicitada
     * @throws PaymentGatewayException si no existe implementación para el tipo
     */
    public PaymentGateway porTipo(PaymentGatewayType tipo) {
        PaymentGateway gateway = gateways.get(tipo);
        if (gateway == null) {
            throw new PaymentGatewayException(
                    "No hay implementación registrada para la pasarela " + tipo);
        }
        return gateway;
    }
}
