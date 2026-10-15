package com.hotelcaciquebolivar.reservas.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad para extraer información de la solicitud HTTP del cliente.
 * <p>
 * Proporciona métodos estáticos para obtener datos relevantes de la petición
 * sin necesidad de instanciar la clase, reforzado mediante un constructor privado.
 */
public final class RequestUtils {

    private RequestUtils() {}

    /**
     * Obtiene la dirección IP real del cliente que realizó la solicitud HTTP.
     * <p>
     * El método verifica, en orden de prioridad, los siguientes encabezados de proxy conocidos:
     * {@code X-Forwarded-For}, {@code X-Real-IP}, {@code Proxy-Client-IP},
     * {@code WL-Proxy-Client-IP}, {@code HTTP_CLIENT_IP} y {@code HTTP_X_FORWARDED_FOR}.
     * <p>
     * Si alguno de estos encabezados contiene un valor válido (no nulo, no vacío y distinto de
     * {@code "unknown"}), se extrae la primera dirección IP en caso de que el encabezado contenga
     * múltiples direcciones separadas por comas.
     * <p>
     * Si ningún encabezado de proxy está presente, se recurre directamente a
     * {@link HttpServletRequest#getRemoteAddr()}. Las direcciones de loopback
     * {@code 0:0:0:0:0:0:0:1} (IPv6) y {@code 127.0.0.1} (IPv4) se normalizan a
     * {@code "127.0.0.1"}.
     *
     * @param request la solicitud HTTP del cliente.
     * @return la dirección IP del cliente como cadena de texto.
     */
    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                int commaIndex = value.indexOf(',');
                return (commaIndex != -1) ? value.substring(0, commaIndex).trim() : value.trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "127.0.0.1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }
}
