package com.hotelcaciquebolivar.reservas.sse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tools.jackson.databind.ObjectMapper;

/**
 * Implementación del servicio de gestión de conexiones Server-Sent Events (SSE).
 * Gestiona de forma concurrente múltiples conexiones SSE por usuario, con un límite
 * máximo de 3 conexiones simultáneas por cada usuario autenticado.
 *
 * <p>Características principales:</p>
 * <ul>
 *   <li>Timeout de conexión de 30 minutos (1.800.000 ms)</li>
 *   <li>Límite de 3 conexiones simultáneas por usuario</li>
 *   <li>Limpieza automática de conexiones caídas, expiradas o con errores</li>
 *   <li>Heartbeat periódico cada 30 segundos para mantener vivas las conexiones</li>
 *   <li>Exclusión de usuarios específicos al transmitir eventos</li>
 * </ul>
 *
 * <p>Utiliza {@link ConcurrentHashMap} y {@link ConcurrentSkipListSet} para
 * garantizar la seguridad en entornos multihilo.</p>
 */
@Service
public class SseEmitterServiceImpl implements SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterServiceImpl.class);
    private static final long SSE_TIMEOUT = 1_800_000L;
    private static final int MAX_CONNECTIONS_PER_USER = 3;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userEmitters = new ConcurrentHashMap<>();
    private final Map<String, String> emitterEmails = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Registra una nueva conexión SSE para el usuario identificado por su email.
     * Si el usuario ya alcanzó el límite de 3 conexiones simultáneas, retorna un emisor
     * con un evento de error tipo "ERROR" y lo completa inmediatamente. De lo contrario,
     * genera un identificador único (UUID), crea el emisor con timeout de 30 minutos,
     * registra las estructuras de control de conexión y envía un evento "CONNECTED" de
     * confirmación al cliente.
     *
     * <p>Se configuran callbacks de limpieza automática en completación, timeout y error
     * para eliminar las referencias del emisor de las estructuras de control cuando la
     * conexión se cierra por cualquier motivo.</p>
     *
     * @param email identificador (email) del usuario que se suscribe
     * @return el {@link SseEmitter} configurado para la conexión, o un emisor de error si se excede el límite
     */
    @Override
    public SseEmitter subscribe(String email) {
        Set<String> userConns = userEmitters.computeIfAbsent(email, k -> new ConcurrentSkipListSet<>());
        if (userConns.size() >= MAX_CONNECTIONS_PER_USER) {
            SseEmitter errorEmitter = new SseEmitter(0L);
            try {
                Map<String, Object> errorPayload = Map.of(
                        "error", "MAX_SSE_CONNECTIONS",
                        "message", "Has alcanzado el límite de " + MAX_CONNECTIONS_PER_USER
                                + " conexiones SSE simultáneas. Cierra alguna antes de abrir una nueva.",
                        "timestamp", LocalDateTime.now().toString());
                errorEmitter.send(SseEmitter.event()
                        .name("ERROR")
                        .data(objectMapper.writeValueAsString(errorPayload)));
            } catch (IOException e) {
                log.warn("Error enviando ERROR al emitter: {}", e.getMessage());
            }
            errorEmitter.complete();
            return errorEmitter;
        }

        String id = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        userConns.add(id);
        emitters.put(id, emitter);
        emitterEmails.put(id, email);

        Runnable cleanup = () -> {
            emitters.remove(id);
            emitterEmails.remove(id);
            Set<String> ue = userEmitters.get(email);
            if (ue != null) {
                ue.remove(id);
                if (ue.isEmpty()) {
                    userEmitters.remove(email);
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> {
            log.debug("SSE error {}: {}", id, e.getMessage());
            cleanup.run();
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        });

        log.debug("SSE conectado: {} (usuario: {}, total: {})", id, email, emitters.size());

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("{\"message\":\"Conectado al servicio de eventos\"}"));
        } catch (IOException e) {
            cleanup.run();
            log.warn("Error enviando CONNECTED a {}: {}", id, e.getMessage());
        }

        return emitter;
    }

    /**
     * Transmite un evento a todos los clientes conectados sin exclusiones.
     * Delega la llamada a {@link #broadcast(String, Object, String)} con {@code emailExcluido} nulo.
     *
     * @param type identificador del tipo de evento (ver {@link SseEventTypes})
     * @param data objeto con los datos del evento a transmitir
     */
    @Override
    public void broadcast(String type, Object data) {
        broadcast(type, data, null);
    }

    /**
     * Transmite un evento a todos los clientes conectados, excluyendo al usuario
     * identificado por el email proporcionado. Serializa el payload a JSON y envía
     * el evento a cada emisor activo. Los emisores que fallen al recibir el evento
     * (conexión caída) son eliminados automáticamente del registro.
     *
     * <p>El payload serializado contiene el tipo de evento, los datos y una marca
     * de tiempo del momento de la transmisión.</p>
     *
     * @param type          identificador del tipo de evento (ver {@link SseEventTypes})
     * @param data          objeto con los datos del evento a transmitir
     * @param emailExcluido email del usuario que no debe recibir el evento, o {@code null} para no excluir a nadie
     */
    @Override
    public void broadcast(String type, Object data, String emailExcluido) {
        if (emitters.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "type", type,
                "data", data,
                "timestamp", LocalDateTime.now().toString());

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error serializando SSE: {}", e.getMessage());
            return;
        }

        emitters.entrySet().removeIf(entry -> {
            if (emailExcluido != null) {
                String emitterEmail = emitterEmails.get(entry.getKey());
                if (emailExcluido.equals(emitterEmail)) {
                    return false;
                }
            }
            try {
                entry.getValue().send(SseEmitter.event()
                        .name(type)
                        .data(json));
                return false;
            } catch (IOException e) {
                log.debug("SSE eliminando cliente caído: {}", entry.getKey());
                return true;
            }
        });
    }

    /**
     * Envía un comentario de heartbeat a todos los emisores activos cada 30 segundos
     * para mantener vivas las conexiones SSE y detectar clientes caídos.
     * Los emisores que fallen al recibir el heartbeat son eliminados automáticamente.
     * Este método se ejecuta de forma programada mediante {@link Scheduled}.
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;

        emitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .comment("heartbeat"));
                return false;
            } catch (IOException e) {
                log.debug("SSE heartbeat falló, eliminando: {}", entry.getKey());
                return true;
            }
        });
    }

    /**
     * Retorna la cantidad actual de conexiones SSE activas registradas en el servicio.
     *
     * @return número total de emisores activos
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
