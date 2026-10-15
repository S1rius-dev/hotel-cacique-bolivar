package com.hotelcaciquebolivar.reservas.security.ratelimit;

/**
 * Excepción lanzada cuando una solicitud excede el límite de tasa configurado.
 * 
 * Esta excepción se utiliza en el sistema de rate limiting para indicar que
 * un cliente (por IP o usuario) ha realizado demasiadas solicitudes en un
 * período de tiempo determinado.
 * 
 * <p>La excepción contiene información sobre cuándo el cliente puede reintentar
 * la solicitud, lo cual es útil para generar respuestas HTTP 429 (Too Many Requests)
 * con headers {@code Retry-After} y {@code X-RateLimit-Reset}.</p>
 * 
 * <p>Esta excepción es capturada por los filtros de rate limiting para generar
 * respuestas adecuadas al cliente.</p>
 * 
 * @author Hotel Cacique Bolívar
 * @version 1.0
 * @see RateLimitFilter
 * @see com.hotelcaciquebolivar.reservas.util.RateLimiter
 */
public class RateLimitExceededException extends RuntimeException {

    private final int retryAfterSeconds;
    private final long resetTime;

    /**
     * Constructor que crea una excepción con información de reintento.
     * 
     * @param retryAfterSeconds Tiempo en segundos que el cliente debe esperar antes de reintentar
     * @param resetTime Marca de tiempo en milisegundos (epoch) cuando se restablece el límite
     */
    public RateLimitExceededException(int retryAfterSeconds, long resetTime) {
        super("Demasiadas solicitudes. Intente de nuevo en " + retryAfterSeconds + " segundos.");
        this.retryAfterSeconds = retryAfterSeconds;
        this.resetTime = resetTime;
    }

    /**
     * Obtiene el tiempo de espera en segundos antes de poder reintentar la solicitud.
     * 
     * @return Número de segundos que el cliente debe esperar
     */
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * Obtiene la marca de tiempo en milisegundos (epoch) cuando se restablece el límite de tasa.
     * 
     * @return Marca de tiempo en milisegundos cuando el límite se restablece
     */
    public long getResetTime() {
        return resetTime;
    }
}
