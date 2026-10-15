package com.hotelcaciquebolivar.reservas.sse.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener que escucha eventos de dominio {@link SseDomainEvent} y los distribuye
 * a todos los clientes conectados vía Server-Sent Events.
 *
 * <p>Opera de forma asíncrona ({@link Async}) y solo procesa los eventos después
 * de que la transacción de base de datos se ha comprometido exitosamente
 * ({@link TransactionPhase#AFTER_COMMIT}), garantizando que solo se notifiquen
 * cambios confirmados.</p>
 *
 * <p>La distribución se realiza a través de {@link SseEmitterService#broadcast},
 * que transmite el evento a todos los emisores activos, excluyendo opcionalmente
 * al usuario que originó la acción.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventListener {

    private final SseEmitterService sseEmitterService;

    /**
     * Maneja un evento de dominio SSE y lo transmite a los clientes conectados.
     * Se ejecuta de forma asíncrona después del compromiso de la transacción.
     *
     * @param event el evento de dominio que contiene el tipo, los datos y
     *              opcionalmente el email del usuario a excluir
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSseEvent(SseDomainEvent event) {
        log.debug("SSE evento emitido: {} (excluye: {})", event.type(), event.emailExcluido());
        sseEmitterService.broadcast(event.type(), event.data(), event.emailExcluido());
    }
}
