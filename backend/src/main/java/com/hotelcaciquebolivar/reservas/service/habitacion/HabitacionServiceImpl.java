package com.hotelcaciquebolivar.reservas.service.habitacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionResponse;
import com.hotelcaciquebolivar.reservas.dto.habitacion.HabitacionUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.habitacion.LimpiarHabitacionesResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.HabitacionEstadoDiario;
import com.hotelcaciquebolivar.reservas.dto.reserva.HuespedResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaActiva;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.HabitacionEstadoDiarioMapper;
import com.hotelcaciquebolivar.reservas.mapper.HabitacionMapper;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.TipoHabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.precio_temporal.PrecioTemporalService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.HabitacionPayload;

/**
 * Implementación del servicio de gestión de habitaciones del sistema hotelero.
 * Maneja la creación, consulta, listado, actualización, eliminación de habitaciones,
 * así como la consulta de estado diario, marcado de habitaciones como limpias
 * y operaciones de limpieza masiva.
 * 
 * <p>Integra con el derivador de estado de ocupación para calcular el estado
 * de cada habitación basándose en las reservas activas para una fecha específica.</p>
 */
@Service
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoRepository;
    private final HabitacionMapper habitacionMapper;
    private final PageMapper pageMapper;
    private final DetalleReservaRepository detalleReservaRepository;
    private final HabitacionEstadoDiarioMapper estadoDiarioMapper;
    private final EstadoOcupacionDerivador estadoOcupacionDerivador;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final PrecioTemporalService precioTemporalService;

    /**
     * Constructor que inyecta todas las dependencias necesarias para el servicio de habitaciones.
     *
     * @param habitacionRepository repositorio de acceso a datos de habitaciones
     * @param tipoRepository repositorio de acceso a tipos de habitación
     * @param habitacionMapper mapper para conversión entre entidades y DTOs de habitaciones
     * @param pageMapper mapper para conversión de páginas a respuestas paginadas
     * @param detalleReservaRepository repositorio de acceso a detalles de reserva
     * @param estadoDiarioMapper mapper para conversión de estados diarios
     * @param estadoOcupacionDerivador componente que deriva el estado de ocupación
     * @param eventPublisher publicador de eventos de dominio para notificaciones SSE
     * @param auditService servicio de registro de auditoría
     * @param precioTemporalService servicio de precios temporales
     */
    public HabitacionServiceImpl(
            HabitacionRepository habitacionRepository,
            TipoHabitacionRepository tipoRepository,
            HabitacionMapper habitacionMapper,
            PageMapper pageMapper,
            DetalleReservaRepository detalleReservaRepository,
            HabitacionEstadoDiarioMapper estadoDiarioMapper,
            EstadoOcupacionDerivador estadoOcupacionDerivador,
            ApplicationEventPublisher eventPublisher,
            AuditService auditService,
            PrecioTemporalService precioTemporalService
    ) {
        this.habitacionRepository = habitacionRepository;
        this.tipoRepository = tipoRepository;
        this.habitacionMapper = habitacionMapper;
        this.pageMapper = pageMapper;
        this.detalleReservaRepository = detalleReservaRepository;
        this.estadoDiarioMapper = estadoDiarioMapper;
        this.estadoOcupacionDerivador = estadoOcupacionDerivador;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.precioTemporalService = precioTemporalService;
    }

    // ============================
    // CREATE
    // ============================
    /**
     * Crea una nueva habitación en el sistema a partir de los datos proporcionados.
     * Valida los campos obligatorios, asigna el tipo de habitación y guarda en la base de datos.
     *
     * @param dto objeto que contiene los datos necesarios para la creación de la habitación
     * @return la habitación creada con el ID asignado
     * @throws ValidationException si los campos obligatorios son inválidos
     * @throws ResourceNotFoundException si el tipo de habitación no existe
     */
    @Override
    public HabitacionResponse crear(HabitacionRequest dto) {

        validarCampos(dto);

        Habitacion habitacion = habitacionMapper.toEntity(dto);

        habitacion.setTipo(
                tipoRepository.findById(dto.tipoId())
                        .orElseThrow(()
                                -> new ResourceNotFoundException("TipoHabitacion", dto.tipoId())
                        )
        );

        Habitacion guardada = habitacionRepository.save(habitacion);

        return habitacionMapper.toAdminResponse(guardada);
    }

    // ============================
    // FIND BY ID
    // ============================
    /**
     * Busca una habitación por su identificador único.
     *
     * @param id identificador único de la habitación a buscar
     * @return la habitación encontrada
     * @throws ResourceNotFoundException si la habitación con el ID especificado no existe
     */
    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse buscarPorId(Long id) {

        Habitacion habitacion = obtenerHabitacion(id);

        return habitacionMapper.toAdminResponse(habitacion);
    }

    // ============================
    // LIST
    // ============================
    /**
     * Lista todas las habitaciones del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de habitaciones
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HabitacionResponse> listarTodos(Pageable pageable) {

        Page<Habitacion> page = habitacionRepository.findAll(pageable);

        return pageMapper.toPageResponse(
                page,
                habitacionMapper::toAdminResponse);
    }

    // ============================
    // UPDATE
    // ============================
    /**
     * Actualiza los datos de una habitación existente identificada por su ID.
     * Valida la unicidad del número de habitación si cambia, y actualiza el tipo si se especifica.
     *
     * @param id identificador único de la habitación a actualizar
     * @param dto objeto que contiene los nuevos datos de la habitación
     * @return la habitación actualizada con los nuevos valores
     * @throws ResourceNotFoundException si la habitación o tipo de habitación no existen
     * @throws ResourceAlreadyExistsException si el número de habitación ya está en uso
     */
    @Override
    public HabitacionResponse actualizar(Long id, HabitacionUpdateRequest dto) {

        Habitacion existente = obtenerHabitacion(id);

        if (!existente.getNumero().equals(dto.numero())) {
            habitacionRepository.findByNumero(dto.numero())
                    .ifPresent(h -> {
                        throw new ResourceAlreadyExistsException(
                                "Habitación",
                                "numero",
                                dto.numero()
                        );
                    });
        }

        habitacionMapper.updateFromDto(dto, existente);

        if (dto.tipoId() != null) {
            existente.setTipo(
                    tipoRepository.findById(dto.tipoId())
                            .orElseThrow(()
                                    -> new ResourceNotFoundException("TipoHabitacion", dto.tipoId())
                            )
            );
        }

        Habitacion actualizada = habitacionRepository.save(existente);

        return habitacionMapper.toAdminResponse(actualizada);
    }

    // ============================
    // DELETE
    // ============================
    /**
     * Elimina una habitación del sistema por su identificador único.
     *
     * @param id identificador único de la habitación a eliminar
     * @throws ResourceNotFoundException si la habitación con el ID especificado no existe
     */
    @Override
    public void eliminar(Long id) {
        habitacionRepository.delete(obtenerHabitacion(id));
    }

    // ============================
    // ESTADO DIARIO
    // ============================
    /**
     * Marca una habitación como limpia, cambiando su estado físico de EN_LIMPIEZA a DISPONIBLE.
     * Publica un evento SSE de cambio de estado y registra un evento de auditoría.
     *
     * @param id identificador único de la habitación a marcar como limpia
     * @return la habitación actualizada con el nuevo estado
     * @throws ResourceNotFoundException si la habitación con el ID especificado no existe
     * @throws ValidationException si la habitación no está en estado EN_LIMPIEZA
     */
    @Override
    public HabitacionResponse marcarComoLimpia(Long id) {
        Habitacion habitacion = obtenerHabitacion(id);

        if (habitacion.getEstado() != EstadoHabitacion.EN_LIMPIEZA) {
            throw new ValidationException("estado",
                    "la habitación debe estar en estado EN_LIMPIEZA para marcarla como limpia");
        }

        EstadoHabitacion estadoAnterior = habitacion.getEstado();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        Habitacion guardada = habitacionRepository.save(habitacion);

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.HABITACION_ESTADO_CAMBIADO,
                new HabitacionPayload(
                        guardada.getId(),
                        guardada.getNumero(),
                        estadoAnterior.name(),
                        guardada.getEstado().name(),
                        guardada.getTipo().getNombre())));

        auditService.log(AuditAction.CLEANED, "Habitacion", guardada.getId(),
                String.format("Limpieza realizada — Habitación %s", guardada.getNumero()));

        return habitacionMapper.toAdminResponse(guardada);
    }

    /**
     * Marca múltiples habitaciones como limpias de forma masiva.
     * Solo procesa habitaciones que existan y estén en estado EN_LIMPIEZA.
     * Registra eventos de auditoría para cada habitación limpiada.
     *
     * @param ids lista de identificadores únicos de las habitaciones a marcar como limpias
     * @return respuesta con la cantidad de habitaciones limpiadas y las omitidas
     */
    @Override
    @Transactional
    public LimpiarHabitacionesResponse limpiar(List<Long> ids) {
        List<Habitacion> encontradas = habitacionRepository.findAllById(ids);
        Set<Long> idsEncontradas = encontradas.stream()
                .map(Habitacion::getId).collect(Collectors.toSet());

        List<Long> omitidas = new ArrayList<>();
        List<Habitacion> aLimpiar = new ArrayList<>();

        for (Long id : ids) {
            if (!idsEncontradas.contains(id)) {
                omitidas.add(id);
                continue;
            }
            Habitacion h = encontradas.stream()
                    .filter(e -> e.getId().equals(id)).findFirst().get();
            if (h.getEstado() != EstadoHabitacion.EN_LIMPIEZA) {
                omitidas.add(id);
                continue;
            }
            h.setEstado(EstadoHabitacion.DISPONIBLE);
            aLimpiar.add(h);
        }

        habitacionRepository.saveAll(aLimpiar);

        for (Habitacion h : aLimpiar) {
            auditService.log(AuditAction.CLEANED, "Habitacion", h.getId(),
                    String.format("Limpieza realizada — Habitación %s", h.getNumero()));
        }

        String mensaje = aLimpiar.size() + " habitaci\u00f3n(es) limpiada(s)";
        if (!omitidas.isEmpty()) {
            mensaje += ", " + omitidas.size() + " omitida(s)";
        }

        return new LimpiarHabitacionesResponse(aLimpiar.size(), omitidas, mensaje);
    }

    /**
     * Obtiene el estado diario de todas las habitaciones para una fecha específica.
     * Calcula el estado derivado de cada habitación basándose en las reservas activas,
     * incluye información de reserva activa si existe y resuelve precios temporales.
     *
     * @param fecha fecha para la que se desea conocer el estado de las habitaciones
     * @return lista de estados diarios de las habitaciones con precios efectivos
     */
    @Override
    @Transactional(readOnly = true)
    public List<HabitacionEstadoDiario> obtenerEstadoDiario(LocalDate fecha) {
        List<Habitacion> habitaciones = habitacionRepository.findAll();

        List<DetalleReserva> todos = detalleReservaRepository.findAllActivosEnFecha(fecha);
        Map<Long, List<DetalleReserva>> detallesPorHabitacion = new HashMap<>();
        for (DetalleReserva dr : todos) {
            detallesPorHabitacion
                    .computeIfAbsent(dr.getHabitacion().getId(), k -> new ArrayList<>())
                    .add(dr);
        }

        List<HabitacionEstadoDiario> resultado = new ArrayList<>();

        for (Habitacion h : habitaciones) {
            List<DetalleReserva> detalles = detallesPorHabitacion.getOrDefault(h.getId(), List.of());
            EstadoHabitacion estado = estadoOcupacionDerivador.derivar(h, detalles, fecha);
            ReservaActiva reservaActiva = null;
            if (!detalles.isEmpty()) {
                List<DetalleReserva> candidatos = detalles;
                if (candidatos.size() > 1) {
                    List<DetalleReserva> noFinalizados = candidatos.stream()
                            .filter(d -> d.getReserva().getEstado() != EstadoReserva.FINALIZADA)
                            .toList();
                    if (!noFinalizados.isEmpty()) {
                        candidatos = noFinalizados;
                    }
                }
                if (candidatos.size() > 1) {
                    List<DetalleReserva> activos = candidatos.stream()
                            .filter(d -> d.getEstado() == EstadoDetalleReserva.OCUPADA
                                      || d.getEstado() == EstadoDetalleReserva.RESERVADA)
                            .toList();
                    if (!activos.isEmpty()) {
                        candidatos = activos;
                    }
                }
                reservaActiva = construirReservaActiva(candidatos.get(0));
            }
            HabitacionEstadoDiario base = estadoDiarioMapper.toEstadoDiarioDTO(h, estado, reservaActiva);
            java.math.BigDecimal efectivoNoche = precioTemporalService.resolverPrecioNoche(h.getTipo(), fecha);
            java.math.BigDecimal efectivoExtra = precioTemporalService.resolverPrecioPersonaExtra(h.getTipo(), fecha);
            HabitacionEstadoDiario conEfectivo = new HabitacionEstadoDiario(
                    base.id(), base.numero(), base.tipoNombre(), base.capacidadBase(), base.capacidadMaxima(),
                    base.estado(), base.precioBase(), base.precioPersonaExtra(),
                    efectivoNoche, efectivoExtra, base.reservaActiva());
            resultado.add(conEfectivo);
        }

        return resultado;
    }

    /**
     * Construye un DTO de reserva activa a partir de un detalle de reserva.
     * Extrae la información del huésped y la reserva activa.
     *
     * @param detalle detalle de reserva del que se construirá la reserva activa
     * @return DTO de reserva activa con la información del huésped
     */
    private ReservaActiva construirReservaActiva(DetalleReserva detalle) {
        List<HuespedResponse> huespedes = detalle.getHuespedes().stream()
                .map(h -> estadoDiarioMapper.toHuespedResponse(h, h.getCliente()))
                .toList();
        return estadoDiarioMapper.toReservaActiva(detalle, huespedes);
    }

    // ============================
    // VALIDACIONES
    // ============================
    /**
     * Valida los campos obligatorios de una habitación: número y tipoId.
     *
     * @param dto objeto que contiene los datos de la habitación a validar
     * @throws ValidationException si algún campo obligatorio es nulo o vacío
     */
    private void validarCampos(HabitacionRequest dto) {

        if (dto.numero() == null || dto.numero().isBlank()) {
            throw new ValidationException("numero", "es obligatorio");
        }

        if (dto.tipoId() == null) {
            throw new ValidationException("tipoId", "es obligatorio");
        }
    }

    // ============================
    // FIND INTERNAL
    // ============================
    /**
     * Busca una habitación por su ID y lanza una excepción si no existe.
     *
     * @param id identificador único de la habitación
     * @return la habitación encontrada
     * @throws ResourceNotFoundException si la habitación con el ID especificado no existe
     */
    private Habitacion obtenerHabitacion(Long id) {
        return habitacionRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Habitación", id)
                );
    }
}
