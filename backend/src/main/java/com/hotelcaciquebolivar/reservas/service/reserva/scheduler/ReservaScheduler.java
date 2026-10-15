package com.hotelcaciquebolivar.reservas.service.reserva.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.service.reserva.ReservaService;
import com.hotelcaciquebolivar.reservas.service.reserva.auto.AutoFinalizacionService;
import com.hotelcaciquebolivar.reservas.service.reserva.auto.NoShowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaScheduler {

    private final NoShowService noShowService;
    private final AutoFinalizacionService autoFinalizacionService;
    private final ReservaService reservaService;

    @Scheduled(cron = "0 */5 * * * *")
    public void finalizarReservasVencidas() {
        try {
            int finalizadas = autoFinalizacionService.finalizarReservasVencidas();
            if (finalizadas > 0) {
                log.info("Auto-checkout automático: {} reservas finalizadas", finalizadas);
            }
        } catch (Exception e) {
            log.error("Error en auto-checkout automático", e);
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void procesarCierreDiario() {
        log.info("=== INICIO CIERRE DIARIO ===");
        try {
            int noShow = noShowService.procesarNoShow();
            reservaService.procesarDetallesVencidos();
            log.info("=== FIN CIERRE DIARIO: {} NO_SHOW ===", noShow);
        } catch (Exception e) {
            log.error("Error en cierre diario", e);
        }
    }
}
