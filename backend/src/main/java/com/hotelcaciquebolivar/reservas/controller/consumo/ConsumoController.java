package com.hotelcaciquebolivar.reservas.controller.consumo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoRequest;
import com.hotelcaciquebolivar.reservas.dto.consumo.ConsumoResponse;
import com.hotelcaciquebolivar.reservas.service.consumo.ConsumoService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de consumos asociados a una reserva.
 * Permite agregar, listar y anular consumos (productos/servicios) utilizados
 * durante la estancia del huésped.
 */
@RestController
@RequestMapping("/api/reservas/{reservaId}/consumos")
@RequiredArgsConstructor
public class ConsumoController {

    private final ConsumoService consumoService;

    /**
     * Agrega un nuevo consumo a una reserva específica.
     *
     * @param reservaId      identificador de la reserva
     * @param dto            datos del consumo a agregar, validados con @Valid
     * @param authentication objeto de autenticación del usuario actual (puede ser null)
     * @return ResponseEntity con el consumo creado, código 201 Created
     */
    @Operation(operationId = "agregarConsumo")
    @PostMapping
    public ResponseEntity<ConsumoResponse> agregar(
            @PathVariable Long reservaId,
            @Valid @RequestBody ConsumoRequest dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(consumoService.agregarConsumo(reservaId, dto, email));
    }

    /**
     * Lista todos los consumos asociados a una reserva específica.
     *
     * @param reservaId identificador de la reserva
     * @return lista de consumos de la reserva
     */
    @Operation(operationId = "listarConsumos")
    @GetMapping
    public ResponseEntity<List<ConsumoResponse>> listar(@PathVariable Long reservaId) {
        return ResponseEntity.ok(consumoService.listarPorReserva(reservaId));
    }

    /**
     * Anula un consumo específico de una reserva.
     *
     * @param reservaId  identificador de la reserva
     * @param consumoId  identificador del consumo a anular
     * @param authentication objeto de autenticación del usuario actual (puede ser null)
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "anularConsumo")
    @DeleteMapping("/{consumoId}")
    public ResponseEntity<Void> anular(
            @PathVariable Long reservaId,
            @PathVariable Long consumoId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        consumoService.anularConsumo(reservaId, consumoId, email);
        return ResponseEntity.noContent().build();
    }
}
