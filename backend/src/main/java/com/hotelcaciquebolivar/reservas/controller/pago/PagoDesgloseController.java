package com.hotelcaciquebolivar.reservas.controller.pago;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.pago.PagoDesgloseLineaResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoDesgloseResponse;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoDesglose;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoDesgloseRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.service.pago.PagoDesgloseService;

import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para consultar el reparto de un pago entre las cajas.
 *
 * <p>Un pago de una reserva cubre a la vez la habitación y los consumos de
 * tienda, que pertenecen a cajas distintas. Estos endpoints exponen ese reparto:
 * una vista previa antes de cobrar y el desglose ya registrado después.</p>
 *
 * <p>La conciliación de ambas cajas vive en
 * {@link com.hotelcaciquebolivar.reservas.controller.caja.CajaController} porque
 * se calcula sobre los movimientos de caja.</p>
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoDesgloseController {

    private final PagoDesgloseService desgloseService;
    private final PagoDesgloseRepository desgloseRepository;
    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    /**
     * Muestra cómo se repartiría un pago entre las cajas, sin registrar nada.
     * Permite a recepción ver el desglose antes de confirmar el cobro.
     *
     * @param reservaId reserva a la que se aplicaría el pago
     * @param monto monto que se piensa cobrar
     * @return las líneas del reparto, cuya suma es igual al monto indicado
     * @throws ResourceNotFoundException si la reserva no existe
     */
    @Operation(
        operationId = "previewDesglosePago",
        summary = "Vista previa del reparto de un pago entre cajas",
        description = "Calcula, sin persistir, cómo se repartiría el monto indicado entre "
                + "las cajas de recepción y tienda, considerando lo ya cobrado en pagos "
                + "anteriores de la reserva. Se aplica primero recepción y con el remanente "
                + "se cubren los consumos de tienda.")
    @GetMapping("/reserva/{reservaId}/preview-desglose")
    public ResponseEntity<List<PagoDesgloseLineaResponse>> previewDesglose(
            @PathVariable Long reservaId,
            @RequestParam BigDecimal monto) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        List<PagoDesgloseLineaResponse> lineas = desgloseService.calcularDesglose(reserva, monto).stream()
                .map(l -> new PagoDesgloseLineaResponse(l.origen(), l.monto(), l.concepto(), l.consumoId()))
                .toList();
        return ResponseEntity.ok(lineas);
    }

    /**
     * Consulta el reparto ya registrado de un pago.
     *
     * @param pagoId pago a consultar
     * @return las líneas de desglose del pago; vacío si el pago no se cobró en efectivo
     * @throws ResourceNotFoundException si el pago no existe
     */
    @Operation(
        operationId = "obtenerDesglosePago",
        summary = "Consulta el reparto de un pago ya registrado",
        description = "Devuelve las líneas persistidas del pago, indicando qué monto "
                + "ingresó a cada caja. Solo los pagos en efectivo tienen desglose, "
                + "porque son los únicos que generan movimiento de caja.")
    @GetMapping("/{pagoId}/desglose")
    public ResponseEntity<List<PagoDesgloseResponse>> obtenerDesglose(@PathVariable Long pagoId) {
        if (!pagoRepository.existsById(pagoId)) {
            throw new ResourceNotFoundException("Pago", pagoId);
        }
        List<PagoDesgloseResponse> lineas = desgloseRepository
                .findByPagoIdOrderByOrigenAsc(pagoId).stream()
                .map(PagoDesgloseController::toResponse)
                .toList();
        return ResponseEntity.ok(lineas);
    }

    private static PagoDesgloseResponse toResponse(PagoDesglose desglose) {
        return new PagoDesgloseResponse(
                desglose.getId(),
                desglose.getOrigen(),
                desglose.getMonto(),
                desglose.getConcepto(),
                desglose.getConsumo() != null ? desglose.getConsumo().getId() : null);
    }
}
