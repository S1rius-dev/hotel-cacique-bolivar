package com.hotelcaciquebolivar.reservas.controller.empleado;

import java.net.URI;

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
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoRequest;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoResponse;
import com.hotelcaciquebolivar.reservas.dto.empleado.EmpleadoUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.empleado.EmpleadoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de empleados del hotel.
 * Proporciona operaciones CRUD completas para el alta, consulta,
 * actualización y baja de empleados.
 */
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    /**
     * Crea un nuevo empleado en el sistema.
     *
     * @param empleadoDTO datos del empleado a crear, validados con @Valid
     * @return ResponseEntity con el empleado creado y código 201 Created,
     *         incluyendo la URI del recurso creado en el header Location
     */
    @PostMapping
    public ResponseEntity<EmpleadoResponse> crearEmpleado(
            @Valid @RequestBody EmpleadoRequest empleadoDTO) {

        EmpleadoResponse nuevo = empleadoService.crear(empleadoDTO);

        return ResponseEntity.created(
                URI.create("/api/empleados/" + nuevo.id())
        ).body(nuevo);
    }

    /**
     * Lista todos los empleados con paginación.
     *
     * @param pageable parámetros de paginación (tamaño por defecto: 14, ordenado por id descendente)
     * @return página de empleados
     */
    @GetMapping
    public ResponseEntity<PageResponse<EmpleadoResponse>> listarEmpleados(
            @PageableDefault(size = 14, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(empleadoService.listarTodos(pageable));
    }

    /**
     * Obtiene un empleado específico por su identificador.
     *
     * @param id identificador del empleado
     * @return el empleado encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> obtenerEmpleado(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.buscarPorId(id));
    }

    /**
     * Actualiza los datos de un empleado existente.
     *
     * @param id           identificador del empleado a actualizar
     * @param empleadoDTO  datos actualizados del empleado, validados con @Valid
     * @return el empleado actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> actualizarEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoUpdateRequest empleadoDTO) {

        EmpleadoResponse actualizado = empleadoService.actualizar(id, empleadoDTO);

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un empleado del sistema.
     *
     * @param id identificador del empleado a eliminar
     * @return ResponseEntity con código 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
