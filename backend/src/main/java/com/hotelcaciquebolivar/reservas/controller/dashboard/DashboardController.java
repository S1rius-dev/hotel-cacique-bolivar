package com.hotelcaciquebolivar.reservas.controller.dashboard;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.dashboard.DashboardResumen;
import com.hotelcaciquebolivar.reservas.dto.dashboard.IngresosResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.OcupacionResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.UltimaReserva;
import com.hotelcaciquebolivar.reservas.service.dashboard.DashboardService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para el panel de administración (dashboard).
 * Proporciona datos resumidos de métricas del hotel como resumen general,
 * ingresos, ocupación y últimas reservas.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene un resumen general del dashboard con métricas clave del hotel.
     *
     * @return resumen del dashboard
     */
    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumen> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }

    /**
     * Obtiene los ingresos del hotel para un rango de tiempo específico.
     *
     * @param rango rango de tiempo para los ingresos (por defecto: "HOY")
     * @return respuesta con los ingresos del rango especificado
     */
    @GetMapping("/ingresos")
    public ResponseEntity<IngresosResponse> obtenerIngresos(
            @RequestParam(defaultValue = "HOY") String rango) {
        return ResponseEntity.ok(dashboardService.obtenerIngresos(rango));
    }

    /**
     * Obtiene la tasa de ocupación del hotel para un rango de tiempo específico.
     *
     * @param rango rango de tiempo para la ocupación (por defecto: "SEMANA")
     * @return respuesta con la ocupación del rango especificado
     */
    @GetMapping("/ocupacion")
    public ResponseEntity<OcupacionResponse> obtenerOcupacion(
            @RequestParam(defaultValue = "SEMANA") String rango) {
        return ResponseEntity.ok(dashboardService.obtenerOcupacion(rango));
    }

    /**
     * Obtiene las últimas reservas registradas en el sistema.
     *
     * @param limite número máximo de reservas a retornar (por defecto: 5)
     * @return lista de las últimas reservas
     */
    @GetMapping("/ultimas-reservas")
    public ResponseEntity<List<UltimaReserva>> obtenerUltimasReservas(
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(dashboardService.obtenerUltimasReservas(limite));
    }
}
