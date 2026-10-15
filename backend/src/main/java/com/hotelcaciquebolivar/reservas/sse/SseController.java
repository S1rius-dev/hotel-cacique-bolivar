package com.hotelcaciquebolivar.reservas.sse;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST que expone el endpoint de suscripción a Server-Sent Events (SSE).
 * Permite a los clientes autenticados establecer una conexión SSE persistente
 * para recibir notificaciones en tiempo real sobre eventos del dominio del hotel
 * (reservas, pagos, habitaciones, turnos, etc.).
 *
 * <p>La conexión se establece mediante una petición GET autenticada. El nombre
 * de usuario se obtiene del objeto {@link Authentication} del contexto de seguridad
 * para identificar y gestionar las conexiones por usuario.</p>
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;

    /**
     * Endpoint de suscripción a Server-Sent Events.
     * Establece una conexión SSE persistente para el usuario autenticado.
     * Retorna un {@link SseEmitter} que mantiene la conexión abierta y
     * permite el envío de eventos en tiempo real al cliente.
     *
     * @param authentication objeto de autenticación del usuario actual,
     *                       proporcionado por Spring Security
     * @return el emisor SSE configurado para la conexión del usuario
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        return sseEmitterService.subscribe(authentication.getName());
    }
}
