package com.hotelcaciquebolivar.reservas.util;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Limitador de tasa de solicitudes basado en ventanas deslizantes.
 * <p>
 * Implementa un algoritmo de ventana deslizante que almacena timestamps de cada solicitud
 * en un {@link ConcurrentLinkedDeque} asociado a una clave única. Las solicitudes fuera de
 * la ventana temporal se eliminan automáticamente antes de evaluar la nueva solicitud.
 * <p>
 * El almacén de claves utiliza {@link ConcurrentHashMap} para garantizar la concurrencia
 * a nivel de entrada, mientras que las operaciones sobre cada deque se sincronizan
 * individualmente para evitar condiciones de carrera en la misma clave.
 * <p>
 * Un tarea programada ejecuta la limpieza periódica cada 30 segundos, eliminando
 * entradas cuyos timestamps tengan más de 120 segundos de antigüedad.
 */
@Component
public class RateLimiter {

    /** Umbral en milisegundos a partir del cual las entradas se consideran expiradas para la limpieza programada. */
    private static final long CLEANUP_THRESHOLD_MS = 120_000L;

    /** Almacén concurrente que mapea cada clave a un deque de timestamps de solicitudes. */
    private final ConcurrentHashMap<String, Deque<Long>> cache = new ConcurrentHashMap<>();

    /**
     * Intenta consumir una solicitud para la clave dada dentro de la ventana temporal especificada.
     * <p>
     * Si la cantidad de solicitudes registradas en la ventana es menor que el límite máximo,
     * se agrega el timestamp actual y se retorna {@code true}. De lo contrario, la solicitud
     * es rechazada retornando {@code false}.
     *
     * @param key         identificador único del recurso o cliente a limitar.
     * @param maxRequests número máximo de solicitudes permitidas dentro de la ventana.
     * @param windowMs    duración de la ventana deslizante en milisegundos.
     * @return {@code true} si la solicitud fue permitida; {@code false} si se excedió el límite.
     */
    public boolean tryConsume(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        Deque<Long> deque = cache.compute(key, (k, existing) -> {
            Deque<Long> d = (existing != null) ? existing : new ConcurrentLinkedDeque<>();
            d.removeIf(t -> t < windowStart);
            return d;
        });

        synchronized (deque) {
            if (deque.size() >= maxRequests) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    /**
     * Retorna la cantidad de solicitudes disponibles restantes para la clave dentro de la ventana temporal.
     * <p>
     * Elimina los timestamps que han excedido la ventana antes de calcular el resultado.
     * Si no existe entrada para la clave, retorna el máximo de solicitudes permitidas.
     *
     * @param key         identificador único del recurso o cliente.
     * @param maxRequests número máximo de solicitudes permitidas dentro de la ventana.
     * @param windowMs    duración de la ventana deslizante en milisegundos.
     * @return número de solicitudes restantes disponibles (mínimo 0).
     */
    public long getRemainingRequests(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;
        Deque<Long> deque = cache.get(key);
        if (deque == null) return maxRequests;
        synchronized (deque) {
            deque.removeIf(t -> t < windowStart);
            return Math.max(0, maxRequests - deque.size());
        }
    }

    /**
     * Calcula el tiempo en milisegundos desde el que se restablecerá el contador de solicitudes.
     * <p>
     * El tiempo de restablecimiento corresponde al timestamp del registro más antiguo en la ventana
     * más la duración de la ventana. Si no hay registros, retorna el tiempo actual.
     *
     * @param key      identificador único del recurso o cliente.
     * @param windowMs duración de la ventana deslizante en milisegundos.
     * @return timestamp en milisegundos en el que se reiniciará la ventana para esta clave.
     */
    public long getResetTime(String key, long windowMs) {
        long now = System.currentTimeMillis();
        Deque<Long> deque = cache.get(key);
        if (deque == null || deque.isEmpty()) return now;
        synchronized (deque) {
            Long oldest = deque.peekFirst();
            return (oldest != null) ? oldest + windowMs : now;
        }
    }

    /**
     * Tarea programada que limpia el almacén de solicitudes expiradas.
     * <p>
     * Se ejecuta cada 30 segundos. Recorre todas las entradas del caché y elimina los timestamps
     * con más de 120 segundos de antigüedad. Si un deque queda vacío tras la limpieza,
     * se elimina la entrada completa del mapa para liberar memoria.
     */
    @Scheduled(fixedRate = 30_000)
    public void cleanup() {
        long threshold = System.currentTimeMillis() - CLEANUP_THRESHOLD_MS;
        cache.entrySet().removeIf(entry -> {
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                deque.removeIf(t -> t < threshold);
                return deque.isEmpty();
            }
        });
    }
}
