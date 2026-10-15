package com.hotelcaciquebolivar.reservas.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad de redondeo monetario para valores en COP.
 * <p>
 * La moneda colombiana no usa decimales en la práctica: todos los montos que
 * se persisten o se muestran al cliente deben quedar en pesos enteros.
 */
public final class DineroUtil {

    private DineroUtil() {}

    /**
     * Redondea un monto a peso entero con HALF_UP.
     *
     * @param monto monto a redondear; puede ser nulo
     * @return el monto redondeado a 0 decimales, o {@code BigDecimal.ZERO} si es nulo
     */
    public static BigDecimal redondear(BigDecimal monto) {
        if (monto == null) {
            return BigDecimal.ZERO;
        }
        return monto.setScale(0, RoundingMode.HALF_UP);
    }
}
