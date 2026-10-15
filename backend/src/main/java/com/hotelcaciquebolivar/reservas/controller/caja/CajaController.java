package com.hotelcaciquebolivar.reservas.controller.caja;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.caja.AbrirTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.CerrarTurnoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.ConciliacionResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.GastoRequest;
import com.hotelcaciquebolivar.reservas.dto.caja.MovimientoResponse;
import com.hotelcaciquebolivar.reservas.dto.caja.TurnoResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.caja.CajaService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de caja y turnos de caja.
 * Proporciona endpoints para abrir/cerrar turnos, registrar gastos,
 * consultar historial y ver movimientos sin turno asociado.
 * Todos los endpoints aceptan un parámetro de caja con valor por defecto "RECEPCION".
 */
@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @PostMapping("/turno/abrir")
    public ResponseEntity<TurnoResponse> abrirTurno(
            Authentication authentication,
            @RequestParam(defaultValue = "RECEPCION") String caja,
            @Valid @RequestBody AbrirTurnoRequest dto) {
        TurnoResponse response = cajaService.abrirTurno(caja, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/turno/cerrar")
    public ResponseEntity<TurnoResponse> cerrarTurno(
            Authentication authentication,
            @RequestParam(defaultValue = "RECEPCION") String caja,
            @Valid @RequestBody CerrarTurnoRequest dto) {
        TurnoResponse response = cajaService.cerrarTurno(caja, dto, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/turno-actual")
    public ResponseEntity<TurnoResponse> obtenerTurnoActivo(
            @RequestParam(defaultValue = "RECEPCION") String caja) {
        TurnoResponse response = cajaService.obtenerTurnoActivo(caja);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/historial")
    public ResponseEntity<PageResponse<TurnoResponse>> obtenerHistorial(
            @RequestParam(defaultValue = "RECEPCION") String caja,
            @PageableDefault(size = 15, sort = "fechaApertura", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<TurnoResponse> response = cajaService.obtenerHistorial(caja, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/gastos")
    public ResponseEntity<MovimientoResponse> registrarGasto(
            Authentication authentication,
            @RequestParam(defaultValue = "RECEPCION") String caja,
            @Valid @RequestBody GastoRequest dto) {
        MovimientoResponse response = cajaService.registrarGasto(caja, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/movimientos-sin-turno")
    public ResponseEntity<Map<String, Object>> movimientosSinTurno(
            @RequestParam(defaultValue = "RECEPCION") String caja) {
        long count = cajaService.countMovimientosSinTurno(caja);
        java.math.BigDecimal monto = cajaService.sumMovimientosSinTurno(caja);
        return ResponseEntity.ok(Map.of("caja", caja, "count", count, "monto", monto));
    }

    @GetMapping("/movimientos-sin-turno/detalle")
    public ResponseEntity<List<MovimientoResponse>> listarMovimientosSinTurno(
            @RequestParam(defaultValue = "RECEPCION") String caja) {
        List<MovimientoResponse> movimientos = cajaService.listarMovimientosSinTurno(caja);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoResponse>> listarTodosLosMovimientos(
            @RequestParam(defaultValue = "RECEPCION") String caja) {
        List<MovimientoResponse> movimientos = cajaService.listarTodosLosMovimientos(caja);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * Cuadra las cajas del hotel en un período, con ingresos y gastos por caja.
     * Se calcula sobre los movimientos de caja, de modo que incluye tanto el
     * efectivo cobrado a las reservas como la venta de mostrador en tienda.
     *
     * @param inicio fecha de inicio del período (ISO-8601, por ejemplo 2026-10-01T00:00)
     * @param fin fecha de fin del período (ISO-8601, por ejemplo 2026-10-31T23:59)
     * @return el cuadre por caja y los totales acumulados
     */
    @Operation(
        operationId = "conciliarCajas",
        summary = "Cuadra las cajas del hotel en un período",
        description = "Devuelve ingresos, gastos y balance de cada caja, más los totales "
                + "acumulados de recepción y tienda. El rango es inclusivo en ambos extremos. "
                + "Incluye los movimientos registrados sin turno activo.")
    @GetMapping("/conciliacion")
    public ResponseEntity<ConciliacionResponse> conciliar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(cajaService.conciliar(inicio, fin));
    }
}
