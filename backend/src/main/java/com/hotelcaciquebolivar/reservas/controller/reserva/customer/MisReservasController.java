package com.hotelcaciquebolivar.reservas.controller.reserva.customer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hotelcaciquebolivar.reservas.dto.reserva.customer.EditarFechasRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.customer.MisReservasResponse;
import com.hotelcaciquebolivar.reservas.service.reserva.customer.MisReservasService;

import jakarta.validation.Valid;

/**
 * Controlador REST para que los clientes autenticados gestionen sus propias reservas.
 * Permite listar, cancelar y editar las fechas de check-in/check-out de las reservas
 * asociadas al usuario autenticado.
 */
@RestController
@RequestMapping("/api/reservas/mis-reservas")
public class MisReservasController {

    private final MisReservasService misReservasService;

    /**
     * Constructor que inyecta el servicio de reservas del cliente.
     *
     * @param misReservasService servicio de gestión de reservas propias del cliente
     */
    public MisReservasController(MisReservasService misReservasService) {
        this.misReservasService = misReservasService;
    }

    /**
     * Lista todas las reservas del usuario autenticado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @return lista de reservas del usuario
     */
    @GetMapping
    public ResponseEntity<List<MisReservasResponse>> listarMisReservas(Authentication authentication) {
        return ResponseEntity.ok(misReservasService.listarMisReservas(authentication));
    }

    /**
     * Cancela una reserva específica del usuario autenticado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @param id             identificador de la reserva a cancelar
     * @return la reserva con estado actualizado a CANCELADA
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<MisReservasResponse> cancelar(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(misReservasService.cancelarMiReserva(authentication, id));
    }

    /**
     * Edita las fechas de check-in y check-out de una reserva del usuario autenticado.
     *
     * @param authentication objeto de autenticación del usuario actual
     * @param id             identificador de la reserva a editar
     * @param request        datos con las nuevas fechas, validados con @Valid
     * @return la reserva con las fechas actualizadas
     */
    @PutMapping("/{id}/fechas")
    public ResponseEntity<MisReservasResponse> editarFechas(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody EditarFechasRequest request) {
        return ResponseEntity.ok(misReservasService.editarFechasMiReserva(
                authentication, id, request.fechaCheckIn(), request.fechaCheckOut()));
    }
}
