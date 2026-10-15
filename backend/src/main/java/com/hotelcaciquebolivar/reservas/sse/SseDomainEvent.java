package com.hotelcaciquebolivar.reservas.sse;

import java.time.LocalDateTime;

/**
 * Evento de dominio que representa una notificación SSE a transmitir a los clientes conectados.
 * Se publica en el contexto de una transacción y es escuchado por {@code SseEventListener}
 * para su distribución a través del servicio de emisores SSE.
 *
 * <p>Contiene el tipo de evento, los datos asociados, una marca de tiempo y opcionalmente
 * un email de usuario que debe ser excluido de la recepción del evento (por ejemplo,
 * el usuario que originó la acción para evitar notificaciones redundantes).</p>
 *
 * @param type          identificador del tipo de evento (ver {@link SseEventTypes})
 * @param data          objeto con los datos específicos del evento (payload)
 * @param timestamp     fecha y hora en que se generó el evento
 * @param emailExcluido email del usuario que no debe recibir el evento, o {@code null} si aplica a todos
 */
public record SseDomainEvent(
    String type,
    Object data,
    LocalDateTime timestamp,
    String emailExcluido
) {
    public SseDomainEvent(String type, Object data) {
        this(type, data, LocalDateTime.now(), null);
    }

    public SseDomainEvent(String type, Object data, LocalDateTime timestamp) {
        this(type, data, timestamp, null);
    }

    public SseDomainEvent(String type, Object data, String emailExcluido) {
        this(type, data, LocalDateTime.now(), emailExcluido);
    }
}
