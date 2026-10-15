package com.hotelcaciquebolivar.reservas.service.alerta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaRequest;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaResponse;
import com.hotelcaciquebolivar.reservas.dto.alerta.AlertaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.alerta.Alerta;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.mapper.AlertaMapper;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.AlertaRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.AlertaPayload;

/**
 * Implementación del servicio de gestión de alertas del sistema hotelero.
 * Proporciona operaciones CRUD completas para alertas asociadas a clientes,
 * incluyendo publicación de eventos SSE y generación de notificaciones
 * para usuarios con rol ADMIN y RECEPCIONISTA.
 * 
 * <p>Esta clase maneja la lógica de negocio para la creación, consulta,
 * listado, actualización y eliminación de alertas, así como la
 * publicación de eventos de dominio para notificaciones en tiempo real.</p>
 */
@Service
@Transactional
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepository;
    private final ClienteRepository clienteRepository;
    private final AlertaMapper alertaMapper;
    private final PageMapper pageMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    /**
     * Constructor que inyecta todas las dependencias necesarias para el servicio de alertas.
     *
     * @param alertaRepository repositorio de acceso a datos de alertas
     * @param clienteRepository repositorio de acceso a datos de clientes
     * @param alertaMapper mapper para conversión entre entidades y DTOs de alertas
     * @param pageMapper mapper para conversión de páginas a respuestas paginadas
     * @param eventPublisher publicador de eventos de dominio para notificaciones SSE
     * @param notificacionService servicio de gestión de notificaciones
     * @param notificacionMessageBuilder constructor de mensajes de notificación
     */
    public AlertaServiceImpl(
            AlertaRepository alertaRepository,
            ClienteRepository clienteRepository,
            AlertaMapper alertaMapper,
            PageMapper pageMapper,
            ApplicationEventPublisher eventPublisher,
            NotificacionService notificacionService,
            NotificacionMessageBuilder notificacionMessageBuilder
    ) {
        this.alertaRepository = alertaRepository;
        this.clienteRepository = clienteRepository;
        this.alertaMapper = alertaMapper;
        this.pageMapper = pageMapper;
        this.eventPublisher = eventPublisher;
        this.notificacionService = notificacionService;
        this.notificacionMessageBuilder = notificacionMessageBuilder;
    }

    /**
     * Crea una nueva alerta en el sistema a partir de los datos proporcionados.
     * Valida que el cliente exista, establece el estado como activo, guarda la alerta
     * en la base de datos, publica un evento SSE de alerta creada y genera una
     * notificación para los usuarios con rol ADMIN y RECEPCIONISTA.
     *
     * @param dto objeto que contiene los datos necesarios para la creación de la alerta
     * @return la alerta creada con el ID asignado y los datos completos
     * @throws ResourceNotFoundException si el cliente especificado no existe
     */
    @Override
    public AlertaResponse crear(AlertaRequest dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.clienteId()));

        Alerta alerta = alertaMapper.toEntity(dto);
        alerta.setCliente(cliente);
        alerta.setEstado(true);

        Alerta guardada = alertaRepository.save(alerta);

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.ALERTA_CREADA,
                new AlertaPayload(
                        guardada.getId(),
                        guardada.getCliente().getId(),
                        guardada.getTipo().name(),
                        guardada.getMensaje()),
                actor.email()));

        String tipoAlerta = guardada.getTipo().name().replace("_", " ").toLowerCase();
        tipoAlerta = Character.toUpperCase(tipoAlerta.charAt(0)) + tipoAlerta.substring(1);
        String clienteNombre = guardada.getCliente().getNombreCompleto()
                + " " + guardada.getCliente().getApellidoCompleto();
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("tipoAlerta", guardada.getTipo().name());
        notificacionService.notificarATodos(TipoNotificacion.ALERTA_CLIENTE,
                actor.prefijo() + " " + actor.nombre()
                + " creó una alerta de tipo " + tipoAlerta + " para " + clienteNombre + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "CLIENTE", guardada.getCliente().getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return alertaMapper.toResponse(guardada);
    }

    /**
     * Busca una alerta por su identificador único.
     *
     * @param id identificador único de la alerta a buscar
     * @return la alerta encontrada con todos sus datos
     * @throws ResourceNotFoundException si la alerta con el ID especificado no existe
     */
    @Override
    @Transactional(readOnly = true)
    public AlertaResponse buscarPorId(Long id) {
        Alerta alerta = obtenerAlerta(id);
        return alertaMapper.toResponse(alerta);
    }

    /**
     * Lista todas las alertas del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de alertas
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlertaResponse> listarTodos(Pageable pageable) {
        Page<Alerta> page = alertaRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, alertaMapper::toResponse);
    }

    /**
     * Lista todas las alertas asociadas a un cliente específico de forma paginada.
     *
     * @param clienteId identificador único del cliente
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene las alertas del cliente especificado
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlertaResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        Page<Alerta> page = alertaRepository.findByClienteId(clienteId, pageable);
        return pageMapper.toPageResponse(page, alertaMapper::toResponse);
    }

    /**
     * Actualiza los datos de una alerta existente identificada por su ID.
     *
     * @param id identificador único de la alerta a actualizar
     * @param dto objeto que contiene los nuevos datos de la alerta
     * @return la alerta actualizada con los nuevos valores
     * @throws ResourceNotFoundException si la alerta con el ID especificado no existe
     */
    @Override
    public AlertaResponse actualizar(Long id, AlertaUpdateRequest dto) {
        Alerta alerta = obtenerAlerta(id);
        alertaMapper.updateFromDto(dto, alerta);
        Alerta actualizada = alertaRepository.save(alerta);
        return alertaMapper.toResponse(actualizada);
    }

    /**
     * Elimina una alerta del sistema por su identificador único.
     *
     * @param id identificador único de la alerta a eliminar
     * @throws ResourceNotFoundException si la alerta con el ID especificado no existe
     */
    @Override
    public void eliminar(Long id) {
        Alerta alerta = obtenerAlerta(id);
        alertaRepository.delete(alerta);
    }

    /**
     * Método privado que busca una alerta por su ID y lanza una excepción si no existe.
     *
     * @param id identificador único de la alerta
     * @return la alerta encontrada
     * @throws ResourceNotFoundException si la alerta con el ID especificado no existe
     */
    private Alerta obtenerAlerta(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
    }

}
