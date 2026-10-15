package com.hotelcaciquebolivar.reservas.service.pqrs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsResponse;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsStateChangeRequest;
import com.hotelcaciquebolivar.reservas.dto.pqrs.PqrsUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.notificacion.TipoNotificacion;
import com.hotelcaciquebolivar.reservas.entity.pqrs.EstadosPqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.Pqrs;
import com.hotelcaciquebolivar.reservas.entity.pqrs.Prioridad;
import com.hotelcaciquebolivar.reservas.entity.pqrs.TipoPqrs;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mail.EmailService;
import com.hotelcaciquebolivar.reservas.mail.dto.PqrsRespuestaData;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.mapper.PqrsMapper;
import com.hotelcaciquebolivar.reservas.repository.pqrs.PqrsRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionMessageBuilder.ActorInfo;
import com.hotelcaciquebolivar.reservas.service.notificacion.NotificacionService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.PqrsPayload;

/**
 * Implementación del servicio de gestión de PQRS.
 * Gestiona el ciclo de vida completo de las PQRS incluyendo creación con código único,
 * máquina de estados con validación de transiciones, notificaciones en tiempo real vía SSE,
 * envío de correo electrónico al responder y asignación de prioridad según el tipo.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class PqrsServiceImpl implements PqrsService {

    private final PqrsRepository pqrsRepository;
    private final UserRepository userRepository;
    private final ReservaRepository reservaRepository;
    private final PqrsMapper pqrsMapper;
    private final PageMapper pageMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final NotificacionService notificacionService;
    private final NotificacionMessageBuilder notificacionMessageBuilder;

    public PqrsServiceImpl(
            PqrsRepository pqrsRepository,
            UserRepository userRepository,
            ReservaRepository reservaRepository,
            PqrsMapper pqrsMapper,
            PageMapper pageMapper,
            ApplicationEventPublisher eventPublisher,
            EmailService emailService,
            NotificacionService notificacionService,
            NotificacionMessageBuilder notificacionMessageBuilder
    ) {
        this.pqrsRepository = pqrsRepository;
        this.userRepository = userRepository;
        this.reservaRepository = reservaRepository;
        this.pqrsMapper = pqrsMapper;
        this.pageMapper = pageMapper;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.notificacionService = notificacionService;
        this.notificacionMessageBuilder = notificacionMessageBuilder;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Flujo de creación:
     * <ol>
     *   <li>Busca el usuario por email (opcional)</li>
     *   <li>Busca la reserva por reservaId (opcional)</li>
     *   <li>Genera código único con formato PQRS-yyyyMMdd-00000</li>
     *   <li>Asigna prioridad según tipo (REPORTE_TECNICO→URGENTE, QUEJA/RECLAMO→ALTA, etc.)</li>
     *   <li>Rellena datos de contacto desde el usuario si no se proporcionaron</li>
     *   <li>Publica evento SSE PQRS_CREADA</li>
     *   <li>Envía notificación a roles ADMIN y RECEPCIONISTA</li>
     * </ol>
     */
    @Override
    public PqrsResponse crear(PqrsRequest dto, String email) {
        User usuario = null;
        if (email != null) {
            usuario = userRepository.findByEmail(email).orElse(null);
        }

        Reserva reserva = null;
        if (dto.reservaId() != null) {
            reserva = reservaRepository.findById(dto.reservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva", dto.reservaId()));
        }

        Pqrs pqrs = pqrsMapper.toEntity(dto);
        pqrs.setUsuario(usuario);
        pqrs.setReserva(reserva);
        pqrs.setCodigo(generarCodigo());
        pqrs.setPrioridad(asignarPrioridadPorTipo(dto.tipo()));

        if (dto.nombreContacto() == null && usuario != null && usuario.getCliente() != null) {
            pqrs.setNombreContacto(usuario.getUsername());
            pqrs.setEmailContacto(usuario.getEmail());
        }

        Pqrs guardada = pqrsRepository.save(pqrs);

        ActorInfo actor = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.PQRS_CREADA,
                new PqrsPayload(guardada.getId(), guardada.getCodigo(),
                        guardada.getTipo().name(), guardada.getEstado().name()),
                actor.email()));

        String tipoPqrs = guardada.getTipo().name().replace("_", " ").toLowerCase();
        tipoPqrs = Character.toUpperCase(tipoPqrs.charAt(0)) + tipoPqrs.substring(1);
        String clienteNombre = guardada.getNombreContacto() != null
                ? guardada.getNombreContacto() : "N/A";
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("clienteNombre", clienteNombre);
        detalles.put("radicado", guardada.getCodigo());
        detalles.put("tipoPQRS", guardada.getTipo().name());
        notificacionService.notificarATodos(TipoNotificacion.PQRS_CREADA,
                actor.prefijo() + " " + (actor.origen().equals("WEB")
                        ? clienteNombre + " envió una PQRS de tipo " + tipoPqrs
                        : actor.nombre() + " registró una PQRS de tipo " + tipoPqrs + " para " + clienteNombre)
                + ". Radicado: " + guardada.getCodigo() + ".",
                actor.origen(), actor.origenNombre(),
                detalles,
                "PQRS", guardada.getId(),
                List.of(Rol.ADMIN, Rol.RECEPCIONISTA),
                actor.email());

        return pqrsMapper.toResponse(guardada);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PqrsResponse buscarPorId(Long id) {
        Pqrs pqrs = obtenerPqrs(id);
        return pqrsMapper.toResponse(pqrs);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PqrsResponse buscarPorCodigo(String codigo) {
        Pqrs pqrs = pqrsRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("PQRS con código", codigo));
        return pqrsMapper.toResponse(pqrs);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PqrsResponse> listarTodos(Pageable pageable) {
        Page<Pqrs> page = pqrsRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, pqrsMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PqrsResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Pqrs> page = pqrsRepository.findByUsuarioId(usuarioId, pageable);
        return pageMapper.toPageResponse(page, pqrsMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PqrsResponse> listarPorEstado(EstadosPqrs estado, Pageable pageable) {
        Page<Pqrs> page = pqrsRepository.findByEstado(estado, pageable);
        return pageMapper.toPageResponse(page, pqrsMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PqrsResponse> listarPorTipo(TipoPqrs tipo, Pageable pageable) {
        Page<Pqrs> page = pqrsRepository.findByTipo(tipo, pageable);
        return pageMapper.toPageResponse(page, pqrsMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Transiciones válidas implementadas:
     * <ul>
     *   <li>ABIERTA → EN_REVISION</li>
     *   <li>EN_REVISION → RESPONDIDA (requiere respuesta no vacía)</li>
     *   <li>EN_REVISION/ABIERTA → RECHAZADA</li>
     *   <li>CERRADA/RECHAZADA → ABIERTA (reabrir)</li>
     *   <li>Cualquier estado → CERRADA</li>
     * </ul>
     * Al cambiar a RESPONDIDA, envía correo electrónico al contacto de la PQRS
     * y publica eventos SSE PQRS_RESPONDIDA y PQRS_ESTADO_CAMBIADO.
     */
    @Override
    public PqrsResponse cambiarEstado(Long id, PqrsStateChangeRequest dto) {
        Pqrs pqrs = obtenerPqrs(id);
        EstadosPqrs nuevoEstado = dto.estado();
        EstadosPqrs actual = pqrs.getEstado();

        if (nuevoEstado == actual) {
            throw new ValidationException("estado", "La PQRS ya se encuentra en estado " + actual.name());
        }

        switch (nuevoEstado) {
            case EN_REVISION -> {
                if (actual != EstadosPqrs.ABIERTA) {
                    throw new ValidationException("estado",
                            "Solo se puede pasar a EN_REVISION desde ABIERTA");
                }
                pqrs.abrirRevision();
            }
            case RESPONDIDA -> {
                if (actual != EstadosPqrs.EN_REVISION) {
                    throw new ValidationException("estado",
                            "Solo se puede responder una PQRS en estado EN_REVISION");
                }
                if (dto.respuesta() == null || dto.respuesta().isBlank()) {
                    throw new ValidationException("respuesta",
                            "Debe proporcionar una respuesta para cambiar a estado RESPONDIDA");
                }
                pqrs.responder(dto.respuesta());
            }
            case CERRADA ->
                pqrs.cerrar();
            case RECHAZADA -> {
                if (actual != EstadosPqrs.EN_REVISION && actual != EstadosPqrs.ABIERTA) {
                    throw new ValidationException("estado",
                            "Solo se puede rechazar una PQRS en estado ABIERTA o EN_REVISION");
                }
                pqrs.rechazar(dto.respuesta());
            }
            case ABIERTA -> {
                if (actual != EstadosPqrs.CERRADA && actual != EstadosPqrs.RECHAZADA) {
                    throw new ValidationException("estado",
                            "Solo se puede reabrir una PQRS cerrada o rechazada");
                }
                pqrs.reabrir();
            }
        }

        Pqrs actualizada = pqrsRepository.save(pqrs);

        if (nuevoEstado == EstadosPqrs.RESPONDIDA) {
            String emailDestino = actualizada.getEmailContacto();
            if (emailDestino == null && actualizada.getUsuario() != null) {
                emailDestino = actualizada.getUsuario().getEmail();
            }
            if (emailDestino != null) {
                String nombreContacto = actualizada.getNombreContacto();
                if (nombreContacto == null && actualizada.getUsuario() != null) {
                    nombreContacto = actualizada.getUsuario().getUsername();
                }
                emailService.sendPqrsResponse(new PqrsRespuestaData(
                        emailDestino,
                        "Respuesta a tu solicitud - " + actualizada.getCodigo(),
                        nombreContacto != null ? nombreContacto : "Usuario",
                        actualizada.getCodigo(),
                        actualizada.getTipo().name(),
                        actualizada.getAsunto(),
                        actualizada.getRespuesta(),
                        LocalDateTime.now()));
            }
            ActorInfo actor = notificacionMessageBuilder.obtenerActor();

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("radicado", actualizada.getCodigo());
            detalles.put("asunto", actualizada.getAsunto());
            notificacionService.notificarATodos(TipoNotificacion.PQRS_RESPONDIDA,
                    actor.prefijo() + " " + actor.nombre()
                    + " respondió la PQRS " + actualizada.getCodigo()
                    + ". Asunto: " + actualizada.getAsunto() + ".",
                    actor.origen(), actor.origenNombre(),
                    detalles,
                    "PQRS", actualizada.getId(),
                    List.of(Rol.ADMIN),
                    actor.email());

            eventPublisher.publishEvent(new SseDomainEvent(
                    SseEventTypes.PQRS_RESPONDIDA,
                    new PqrsPayload(actualizada.getId(), actualizada.getCodigo(),
                            actualizada.getTipo().name(), actualizada.getEstado().name()),
                    actor.email()));
        }

        ActorInfo actorEstado = notificacionMessageBuilder.obtenerActor();

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.PQRS_ESTADO_CAMBIADO,
                new PqrsPayload(actualizada.getId(), actualizada.getCodigo(),
                        actualizada.getTipo().name(), actualizada.getEstado().name()),
                actorEstado.email()));

        return pqrsMapper.toResponse(actualizada);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si el tipo cambia, se recalcula la prioridad automáticamente.
     * No se permite modificar PQRS en estado CERRADA o RECHAZADA.
     */
    @Override
    public PqrsResponse actualizar(Long id, PqrsUpdateRequest dto) {
        Pqrs pqrs = obtenerPqrs(id);

        if (pqrs.getEstado() == EstadosPqrs.CERRADA || pqrs.getEstado() == EstadosPqrs.RECHAZADA) {
            throw new ValidationException("estado",
                    "No se puede modificar una PQRS en estado " + pqrs.getEstado().name());
        }

        if (dto.tipo() != null && dto.tipo() != pqrs.getTipo()) {
            pqrs.setPrioridad(asignarPrioridadPorTipo(dto.tipo()));
        }

        pqrsMapper.updateFromDto(dto, pqrs);
        Pqrs actualizada = pqrsRepository.save(pqrs);

        return pqrsMapper.toResponse(actualizada);
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(Long id) {
        Pqrs pqrs = obtenerPqrs(id);
        pqrsRepository.delete(pqrs);
    }

    /**
     * Obtiene una PQRS por su identificador o lanza excepción si no existe.
     *
     * @param id identificador de la PQRS
     * @return la entidad PQRS
     * @throws ResourceNotFoundException si no se encuentra la PQRS
     */
    private Pqrs obtenerPqrs(Long id) {
        return pqrsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PQRS", id));
    }

    /**
     * Genera un código único para la PQRS con formato PQRS-yyyyMMdd-00000.
     * Verifica la unicidad contra la base de datos y regenera si es necesario.
     *
     * @return código único generado
     */
    private String generarCodigo() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%05d", ThreadLocalRandom.current().nextInt(10000));
        String codigo = "PQRS-" + fecha + "-" + random;

        while (pqrsRepository.existsByCodigo(codigo)) {
            random = String.format("%05d", ThreadLocalRandom.current().nextInt(10000));
            codigo = "PQRS-" + fecha + "-" + random;
        }

        return codigo;
    }

    /**
     * Asigna la prioridad de una PQRS según su tipo.
     * REPORTE_TECNICO → URGENTE, QUEJA/RECLAMO → ALTA, PETICIÓN → MEDIA,
     * SUGERENCIA/FELICITACIÓN → BAJA. Por defecto retorna MEDIA.
     *
     * @param tipo tipo de la PQRS
     * @return la prioridad asignada
     */
    private Prioridad asignarPrioridadPorTipo(TipoPqrs tipo) {
        if (tipo == null) {
            return Prioridad.MEDIA;
        }
        return switch (tipo) {
            case REPORTE_TECNICO ->
                Prioridad.URGENTE;
            case QUEJA, RECLAMO ->
                Prioridad.ALTA;
            case PETICION ->
                Prioridad.MEDIA;
            case SUGERENCIA, FELICITACION ->
                Prioridad.BAJA;
        };
    }
}
