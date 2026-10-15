package com.hotelcaciquebolivar.reservas.controller.reserva;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.reserva.ActualizarDetalleRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AgregarHuespedesRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.AjusteResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleCheckInRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.DisponibilidadResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ExtenderEstadiaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRapidaRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
import com.hotelcaciquebolivar.reservas.service.reserva.ReservaService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST principal para la gestión de reservas del hotel. Proporciona
 * operaciones CRUD completas, gestión de check-in/check-out, disponibilidad,
 * detalles de reserva, huéspedes, ajustes y extensiones de estadía. Este es el
 * controlador más completo del sistema, gestionando todo el ciclo de vida de
 * una reserva desde su creación hasta su finalización.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Crea una nueva reserva en el sistema.
     *
     * @param dto datos de la reserva a crear, validados con @Valid
     * @return ResponseEntity con la reserva creada, código 201 Created
     */
    @Operation(operationId = "crearReserva")
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(
            @Valid @RequestBody ReservaRequest dto) {
        ReservaResponse response = reservaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca una reserva específica por su identificador.
     *
     * @param id identificador de la reserva
     * @return la reserva encontrada
     */
    @Operation(operationId = "buscarReservaPorId")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    /**
     * Lista reservas con múltiples filtros opcionales y paginación.
     *
     * @param codigo filtro por código de reserva (opcional)
     * @param cliente filtro por nombre del cliente (opcional)
     * @param estado filtro por estado de reserva (opcional)
     * @param origen filtro por origen de reserva (opcional)
     * @param checkInDesde fecha de check-in desde (opcional, formato ISO)
     * @param checkInHasta fecha de check-in hasta (opcional, formato ISO)
     * @param checkOutDesde fecha de check-out desde (opcional, formato ISO)
     * @param checkOutHasta fecha de check-out hasta (opcional, formato ISO)
     * @param pageable parámetros de paginación (tamaño por defecto: 14,
     * ordenado por id descendente)
     * @return página de reservas que coinciden con los filtros
     */
    @Operation(operationId = "listarReservas")
    @GetMapping
    public ResponseEntity<PageResponse<ReservaResponse>> listar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) OrigenReserva origen,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutHasta,
            @PageableDefault(size = 14, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reservaService.listarTodos(
                codigo, cliente, estado, origen,
                checkInDesde, checkInHasta,
                checkOutDesde, checkOutHasta, pageable));
    }

    /**
     * Actualiza los datos de una reserva existente.
     *
     * @param id identificador de la reserva a actualizar
     * @param dto datos actualizados de la reserva, validados con @Valid
     * @return la reserva actualizada
     */
    @Operation(operationId = "actualizarReserva")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReservaUpdateRequest dto) {
        return ResponseEntity.ok(reservaService.actualizar(id, dto));
    }

    /**
     * Elimina una reserva del sistema.
     *
     * @param id identificador de la reserva a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @Operation(operationId = "eliminarReserva")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta la disponibilidad de habitaciones para un rango de fechas
     * específico.
     *
     * @param checkIn fecha de check-in (formato ISO: YYYY-MM-DD)
     * @param checkOut fecha de check-out (formato ISO: YYYY-MM-DD)
     * @return lista de habitaciones disponibles con sus detalles
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(reservaService.consultarDisponibilidad(checkIn, checkOut));
    }

    /**
     * Realiza el check-in de una reserva completa.
     *
     * @param id identificador de la reserva
     * @return la reserva con estado actualizado a CHECK_IN
     */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<ReservaResponse> realizarCheckIn(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.realizarCheckIn(id));
    }

    /**
     * Realiza el check-out de una reserva completa.
     *
     * @param id identificador de la reserva
     * @return la reserva con estado actualizado a CHECK_OUT
     */
    @PostMapping("/{id}/check-out")
    public ResponseEntity<ReservaResponse> realizarCheckOut(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.realizarCheckOut(id));
    }

    /**
     * Crea una reserva rápida con datos simplificados.
     *
     * @param dto datos de la reserva rápida, validados con @Valid
     * @return ResponseEntity con la reserva creada, código 201 Created
     */
    @PostMapping("/rapida")
    public ResponseEntity<ReservaResponse> crearReservaRapida(
            @Valid @RequestBody ReservaRapidaRequest dto) {
        ReservaResponse response = reservaService.crearReservaRapida(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Realiza el check-in de un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle
     * @param dto datos del check-in del detalle, validados con @Valid
     * @return la reserva actualizada
     */
    @PostMapping("/{reservaId}/detalles/{detalleId}/check-in")
    public ResponseEntity<ReservaResponse> checkInPorDetalle(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId,
            @Valid @RequestBody DetalleCheckInRequest dto) {
        return ResponseEntity.ok(reservaService.checkInPorDetalle(reservaId, detalleId, dto));
    }

    /**
     * Cambia el modo de IVA (con/sin IVA) para un detalle de reserva existente.
     */
    @PatchMapping("/{reservaId}/detalles/{detalleId}/precio-modo")
    public ResponseEntity<ReservaResponse> togglePrecioModo(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId,
            @RequestParam boolean incluyeIva) {
        return ResponseEntity.ok(reservaService.togglePrecioModo(reservaId, detalleId, incluyeIva));
    }

    /**
     * Realiza el check-out de un detalle espec��fico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle
     * @return la reserva actualizada
     */
    @PostMapping("/{reservaId}/detalles/{detalleId}/check-out")
    public ResponseEntity<ReservaResponse> checkOutPorDetalle(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId) {
        return ResponseEntity.ok(reservaService.checkOutPorDetalle(reservaId, detalleId));
    }

    /**
     * Extiende la estadía de una reserva cambiando la fecha de check-out.
     *
     * @param id identificador de la reserva
     * @param dto datos con la nueva fecha de check-out, validados con @Valid
     * @return la reserva con la fecha de check-out actualizada
     */
    @PutMapping("/{id}/extender")
    public ResponseEntity<ReservaResponse> extenderEstadia(
            @PathVariable Long id,
            @Valid @RequestBody ExtenderEstadiaRequest dto) {
        return ResponseEntity.ok(reservaService.extenderEstadia(id, dto.nuevaFechaCheckOut()));
    }

    /**
     * Agrega huéspedes a un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle
     * @param dto datos con los IDs de los huéspedes a agregar, validados con
     * @Valid
     * @return la reserva actualizada
     */
    @PostMapping("/{reservaId}/detalles/{detalleId}/huespedes")
    public ResponseEntity<ReservaResponse> agregarHuespedes(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId,
            @Valid @RequestBody AgregarHuespedesRequest dto) {
        return ResponseEntity.ok(reservaService.agregarHuespedes(reservaId, detalleId, dto.huespedesIds()));
    }

    /**
     * Remueve un huésped de un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle
     * @param detalleHuespedId identificador de la relación detalle-huésped
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{reservaId}/detalles/{detalleId}/huespedes/{detalleHuespedId}")
    public ResponseEntity<Void> removerHuesped(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId,
            @PathVariable Long detalleHuespedId) {
        reservaService.removerHuesped(reservaId, detalleId, detalleHuespedId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remueve un detalle de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle a remover
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{reservaId}/detalles/{detalleId}")
    public ResponseEntity<Void> removerDetalle(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId) {
        reservaService.removerDetalle(reservaId, detalleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega un nuevo detalle (habitación) a una reserva existente.
     *
     * @param reservaId identificador de la reserva
     * @param dto datos del nuevo detalle, validados con @Valid
     * @return la reserva actualizada con el nuevo detalle
     */
    @PostMapping("/{reservaId}/detalles")
    public ResponseEntity<ReservaResponse> agregarDetalle(
            @PathVariable Long reservaId,
            @Valid @RequestBody DetalleReservaRequest dto) {
        return ResponseEntity.ok(reservaService.agregarDetalle(reservaId, dto));
    }

    /**
     * Actualiza un detalle específico de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param detalleId identificador del detalle a actualizar
     * @param dto datos actualizados del detalle, validados con @Valid
     * @return la reserva actualizada
     */
    @PutMapping("/{reservaId}/detalles/{detalleId}")
    public ResponseEntity<ReservaResponse> actualizarDetalle(
            @PathVariable Long reservaId,
            @PathVariable Long detalleId,
            @Valid @RequestBody ActualizarDetalleRequest dto) {
        return ResponseEntity.ok(reservaService.actualizarDetalle(reservaId, detalleId, dto));
    }

    /**
     * Agrega un ajuste (cargo adicional o descuento) a una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param dto datos del ajuste a agregar, validados con @Valid
     * @return ResponseEntity con el ajuste creado, código 201 Created
     */
    @PostMapping("/{reservaId}/ajustes")
    public ResponseEntity<AjusteResponse> agregarAjuste(
            @PathVariable Long reservaId,
            @Valid @RequestBody AjusteRequest dto) {
        AjusteResponse response = reservaService.agregarAjuste(reservaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Edita un ajuste existente de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId identificador del ajuste a editar
     * @param dto datos actualizados del ajuste, validados con @Valid
     * @return el ajuste actualizado
     */
    @PutMapping("/{reservaId}/ajustes/{ajusteId}")
    public ResponseEntity<AjusteResponse> editarAjuste(
            @PathVariable Long reservaId,
            @PathVariable Long ajusteId,
            @Valid @RequestBody AjusteRequest dto) {
        return ResponseEntity.ok(reservaService.editarAjuste(reservaId, ajusteId, dto));
    }

    /**
     * Elimina un ajuste de una reserva.
     *
     * @param reservaId identificador de la reserva
     * @param ajusteId identificador del ajuste a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{reservaId}/ajustes/{ajusteId}")
    public ResponseEntity<Void> eliminarAjuste(
            @PathVariable Long reservaId,
            @PathVariable Long ajusteId) {
        reservaService.eliminarAjuste(reservaId, ajusteId);
        return ResponseEntity.noContent().build();
    }
}
