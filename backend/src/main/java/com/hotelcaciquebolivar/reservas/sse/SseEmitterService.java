package com.hotelcaciquebolivar.reservas.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Interfaz del servicio de gestión de conexiones Server-Sent Events (SSE).
 * Define las operaciones para suscribir clientes, transmitir eventos a todos los
 * conectados y gestionar el ciclo de vida de las conexiones SSE.
 *
 * <p>Las implementaciones de esta interfaz deben manejar la gestión concurrente
 * de múltiples conexiones por usuario, la limpieza automática de conexiones
 * caídas o expiradas, y la serialización de eventos a formato JSON.</p>
 */
public interface SseEmitterService {

    /**
     * Registra una nueva conexión SSE para el usuario identificado por su email.
     * Si el usuario ya alcanzó el límite máximo de conexiones simultáneas,
     * retorna un emisor con un evento de error y lo completa inmediatamente.
     *
     * @param email identificador (email) del usuario que se suscribe
     * @return el {@link SseEmitter} configurado para la conexión, o un emisor de error si se excede el límite
     */
    SseEmitter subscribe(String email);

    /**
     * Transmite un evento a todos los clientes conectados sin exclusiones.
     *
     * @param type identificador del tipo de evento (ver {@link SseEventTypes})
     * @param data objeto con los datos del evento a transmitir
     */
    void broadcast(String type, Object data);

    /**
     * Transmite un evento a todos los clientes conectados, excluyendo al usuario
     * identificado por el email proporcionado.
     *
     * @param type          identificador del tipo de evento (ver {@link SseEventTypes})
     * @param data          objeto con los datos del evento a transmitir
     * @param emailExcluido email del usuario que no debe recibir el evento, o {@code null} para no excluir a nadie
     */
    void broadcast(String type, Object data, String emailExcluido);
}
