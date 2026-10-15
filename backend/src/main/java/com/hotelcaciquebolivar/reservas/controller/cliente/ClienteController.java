package com.hotelcaciquebolivar.reservas.controller.cliente;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.cliente.ClienteService;
import com.hotelcaciquebolivar.reservas.service.migration.ClienteImportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de clientes del hotel.
 * Proporciona operaciones CRUD completas, búsqueda con múltiples filtros,
 * búsqueda rápida y migración de clientes desde datos externos.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteImportService clienteImportService;

    /**
     * Crea un nuevo cliente en el sistema.
     *
     * @param clienteDTO datos del cliente a crear, validados con @Valid
     * @return ResponseEntity con el cliente creado y código 201 Created,
     *         incluyendo la URI del recurso creado en el header Location
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(
            @Valid @RequestBody ClienteRequest clienteDTO) {
        ClienteResponse nuevo = clienteService.crear(clienteDTO);
        return ResponseEntity.created(URI.create("/api/clientes/" + nuevo.id())).body(nuevo);
    }

    /**
     * Ejecuta la migración de clientes desde datos externos (CSV).
     *
     * @return ResponseEntity con el resultado de la importación
     */
    @PostMapping("/migracion")
    public ResponseEntity<ClienteImportService.ImportResult> migrarClientes() {
        ClienteImportService.ImportResult result = clienteImportService.importar();
        return ResponseEntity.ok(result);
    }

    /**
     * Lista clientes con filtros opcionales y paginación.
     *
     * @param nombre               filtro por nombre del cliente (opcional)
     * @param cedula               filtro por número de cédula (opcional)
     * @param telefono             filtro por número de teléfono (opcional)
     * @param tipoIdentificacionId filtro por tipo de identificación (opcional)
     * @param activo               filtro por estado activo/inactivo (opcional)
     * @param pageable             parámetros de paginación (tamaño por defecto: 15, ordenado por id descendente)
     * @return página de clientes que coinciden con los filtros
     */
    @GetMapping
    public ResponseEntity<PageResponse<ClienteResponse>> listarClientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Long tipoIdentificacionId,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ClienteResponse> result = clienteService.listarConFiltros(
                nombre, cedula, telefono, tipoIdentificacionId, activo, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Busca un cliente por tipo de identificación y número de cédula.
     *
     * @param tipoIdentificacionId identificador del tipo de identificación
     * @param cedula               número de cédula del cliente
     * @return el cliente encontrado
     */
    @GetMapping("/search")
    public ResponseEntity<ClienteResponse> buscarClientePorTipoYCedula(
            @RequestParam Long tipoIdentificacionId,
            @RequestParam String cedula) {
        ClienteResponse cliente = clienteService.buscarPorTipoYCedula(tipoIdentificacionId, cedula);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Realiza una búsqueda rápida de clientes por término de búsqueda.
     *
     * @param q     término de búsqueda para filtrar clientes
     * @param limit número máximo de resultados a retornar (por defecto: 20)
     * @return lista de clientes que coinciden con el término de búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscarClientes(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(clienteService.buscarRapido(q, limit));
    }

    /**
     * Obtiene un cliente específico por su identificador.
     *
     * @param id identificador del cliente
     * @return el cliente encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param id                 identificador del cliente a actualizar
     * @param clienteActualizado datos actualizados del cliente, validados con @Valid
     * @return el cliente actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteUpdateRequest clienteActualizado) {
        ClienteResponse actualizado = clienteService.actualizar(id, clienteActualizado);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un cliente del sistema.
     *
     * @param id identificador del cliente a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}