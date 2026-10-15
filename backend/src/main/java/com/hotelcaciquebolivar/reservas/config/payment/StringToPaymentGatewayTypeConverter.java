package com.hotelcaciquebolivar.reservas.config.payment;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.pago.PaymentGatewayType;

/**
 * Convertidor de cadena a {@link PaymentGatewayType}.
 *
 * <p>Implementa {@link Converter} para permitir que Spring convierta
 * cadenas de texto (provenientes de propiedades de configuración) al
 * tipo enum {@link PaymentGatewayType}. Normaliza la entrada pasando
 * a mayúsculas y reemplazando guiones por guiones bajos para mayor
 * flexibilidad en la configuración.</p>
 *
 * @see PaymentGatewayType
 * @see org.springframework.core.convert.converter.Converter
 */
@Component
public class StringToPaymentGatewayTypeConverter implements Converter<String, PaymentGatewayType> {

    /**
     * Convierte una cadena de texto a un valor de {@link PaymentGatewayType}.
     *
     * <p>Normaliza la cadena eliminando espacios en blanco, convirtiendo a
     * mayúsculas y reemplazando guiones ({@code -}) por guiones bajos ({@code _}).
     * Si la cadena es {@code null}, retorna {@code null}.</p>
     *
     * @param source cadena de texto a convertir (ej. "wompi", "WOMPI", "wompi-gateway")
     * @return valor correspondiente de {@link PaymentGatewayType}, o {@code null} si la entrada es nula
     * @throws IllegalArgumentException si la cadena no corresponde a un valor válido del enum
     */
    @Override
    public PaymentGatewayType convert(String source) {
        if (source == null) {
            return null;
        }
        String normalized = source.trim().toUpperCase().replace("-", "_");
        return PaymentGatewayType.valueOf(normalized);
    }
}
