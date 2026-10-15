package com.hotelcaciquebolivar.reservas.service.cliente;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteResponse;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClienteUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.cliente.ClientePublicResponse;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.mapper.ClienteMapper;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteSpecification;
import com.hotelcaciquebolivar.reservas.repository.cliente.MunicipioRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.TipoIdentificacionRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.sse.SseDomainEvent;
import com.hotelcaciquebolivar.reservas.sse.SseEventTypes;
import com.hotelcaciquebolivar.reservas.sse.payload.ClientePayload;

/**
 * Implementación del servicio de gestión de clientes del sistema hotelero.
 * Maneja la creación, consulta, listado con filtros, búsqueda rápida, actualización,
 * eliminación y vinculación de clientes con cuentas de usuario.
 * 
 * <p>Esta clase registra eventos de auditoría para cada operación de escritura
 * y publica eventos SSE para notificaciones en tiempo real.</p>
 */
@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final MunicipioRepository municipioRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor que inyecta todas las dependencias necesarias para el servicio de clientes.
     *
     * @param clienteRepository repositorio de acceso a datos de clientes
     * @param clienteMapper mapper para conversión entre entidades y DTOs de clientes
     * @param municipioRepository repositorio de acceso a datos de municipios
     * @param tipoIdentificacionRepository repositorio de acceso a tipos de identificación
     * @param userRepository repositorio de acceso a datos de usuarios
     * @param pageMapper mapper para conversión de páginas a respuestas paginadas
     * @param auditService servicio de registro de auditoría
     * @param eventPublisher publicador de eventos de dominio para notificaciones SSE
     */
    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper,
            MunicipioRepository municipioRepository, TipoIdentificacionRepository tipoIdentificacionRepository,
            UserRepository userRepository, PageMapper pageMapper, AuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.municipioRepository = municipioRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.userRepository = userRepository;
        this.pageMapper = pageMapper;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Crea un nuevo cliente en el sistema a partir de los datos proporcionados.
     * Valida la unicidad de la cédula, establece el estado como activo, asigna
     * el municipio de expedición y tipo de identificación, registra un evento
     * de auditoría y publica un evento SSE de cliente creado.
     *
     * @param dto objeto que contiene los datos necesarios para la creación del cliente
     * @return el cliente creado con el ID asignado
     * @throws ResourceAlreadyExistsException si la cédula ya está registrada
     * @throws ResourceNotFoundException si el municipio o tipo de identificación no existen
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    @Override
    public ClienteResponse crear(ClienteRequest dto) {

        validarCreacion(dto);

        Cliente cliente = clienteMapper.toEntity(dto);

        cliente.setActivo(true);

        cliente.setMunicipioExpedicion(
                municipioRepository.findById(dto.municipioExpedicionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Municipio", dto.municipioExpedicionId()))
        );

        cliente.setTipoIdentificacion(
                tipoIdentificacionRepository.findById(dto.tipoIdentificacionId())
                        .orElseThrow(() -> new ResourceNotFoundException("TipoIdentificacion", dto.tipoIdentificacionId()))
        );

        Cliente guardado = clienteRepository.save(cliente);

        auditService.log(AuditAction.CREATE, "Cliente", guardado.getId(),
                String.format("Cliente creado — #%d\nCédula: %s\nNombre: %s %s\nTel: %s",
                        guardado.getId(), guardado.getCedula(), guardado.getNombreCompleto(),
                        guardado.getApellidoCompleto(), guardado.getTelefono()));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CLIENTE_CREADO,
                new ClientePayload(
                        guardado.getId(),
                        guardado.getNombreCompleto() + " " + guardado.getApellidoCompleto(),
                        guardado.getCedula(),
                        null,
                        "RECEPCION")));

        return clienteMapper.toAdminResponse(guardado);
    }

    /**
     * Busca un cliente por su identificador único.
     *
     * @param id identificador único del cliente a buscar
     * @return el cliente encontrado
     * @throws ResourceNotFoundException si el cliente con el ID especificado no existe
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = obtenerCliente(id);
        return clienteMapper.toAdminResponse(cliente);
    }

    /**
     * Lista todos los clientes del sistema de forma paginada.
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene la lista de clientes
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listarTodos(Pageable pageable) {
        Page<Cliente> page = clienteRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, clienteMapper::toAdminResponse);
    }

    /**
     * Lista los clientes del sistema con filtros opcionales de forma paginada.
     * Utiliza especificaciones JPA para construir la consulta dinámicamente.
     *
     * @param nombre nombre o apellido del cliente para filtrar (parcial, case-insensitive)
     * @param cedula número de cédula del cliente para filtrar (parcial)
     * @param telefono número de teléfono del cliente para filtrar (parcial)
     * @param tipoIdentificacionId identificador del tipo de identificación para filtrar
     * @param activo estado del cliente para filtrar (true/false/null para todos)
     * @param pageable configuración de paginación y ordenamiento
     * @return respuesta paginada que contiene los clientes que coinciden con los filtros
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listarConFiltros(
            String nombre, String cedula, String telefono,
            Long tipoIdentificacionId, Boolean activo, Pageable pageable) {

        Specification<Cliente> spec = (root, query, cb) -> cb.conjunction();

        if (nombre != null && !nombre.isBlank()) {
            spec = spec.and(ClienteSpecification.nombreContiene(nombre));
        }
        if (cedula != null && !cedula.isBlank()) {
            spec = spec.and(ClienteSpecification.cedulaEmpiezaCon(cedula));
        }
        if (telefono != null && !telefono.isBlank()) {
            spec = spec.and(ClienteSpecification.telefonoContiene(telefono));
        }
        if (tipoIdentificacionId != null) {
            spec = spec.and(ClienteSpecification.tipoIdentificacionIgual(tipoIdentificacionId));
        }
        if (activo != null) {
            spec = spec.and(ClienteSpecification.activoIgual(activo));
        }

        Page<Cliente> page = clienteRepository.findAll(spec, pageable);
        return pageMapper.toPageResponse(page, clienteMapper::toAdminResponse);
    }

    /**
     * Realiza una búsqueda rápida de clientes por nombre, cédula o teléfono.
     * Retorna un número limitado de resultados ordenados por nombre completo.
     *
     * @param q término de búsqueda general
     * @param limit cantidad máxima de resultados a retornar
     * @return lista de clientes que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarRapido(String q, int limit) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        Specification<Cliente> spec = ClienteSpecification.busquedaGlobal(q);
        Page<Cliente> page = clienteRepository.findAll(spec,
                PageRequest.of(0, limit, Sort.by("nombreCompleto")));
        return page.getContent().stream()
                .map(clienteMapper::toAdminResponse)
                .toList();
    }

    /**
     * Busca un cliente por tipo de identificación y número de cédula.
     * Valida que la cédula no esté vacía, busca el cliente y verifica que esté activo.
     *
     * @param tipoIdentificacionId identificador del tipo de identificación
     * @param cedula número de cédula del cliente
     * @return el cliente encontrado y activo
     * @throws ValidationException si la cédula está vacía
     * @throws ResourceNotFoundException si el cliente no existe o está inactivo
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorTipoYCedula(Long tipoIdentificacionId, String cedula) {
        if (cedula == null || cedula.isBlank()) {
            throw new ValidationException("cedula", "es obligatoria");
        }

        String cedulaTrim = cedula.trim();

        Cliente cliente = clienteRepository
                .findByTipoIdentificacionIdAndCedula(tipoIdentificacionId, cedulaTrim)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente",
                        "tipoIdentificacionId=" + tipoIdentificacionId + ", cedula=" + cedulaTrim));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ResourceNotFoundException("Cliente",
                    "tipoIdentificacionId=" + tipoIdentificacionId + ", cedula=" + cedulaTrim);
        }

        return clienteMapper.toAdminResponse(cliente);
    }

    /**
     * Vincula un cliente existente con una cuenta de usuario.
     * Valida que el usuario no tenga un cliente asignado, que el cliente no esté
     * vinculado a otro usuario, y si el cliente está inactivo lo activa.
     * Registra un evento de auditoría y publica un evento SSE de vinculación.
     *
     * @param clienteId identificador único del cliente a vincular
     * @param email email del usuario al que se vinculará el cliente
     * @return respuesta pública con los datos del cliente vinculado
     * @throws ResourceNotFoundException si el usuario o cliente no existen
     * @throws ValidationException si el usuario ya tiene un cliente o el cliente ya está vinculado
     */
    @Override
    public ClientePublicResponse vincularCliente(Long clienteId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("email", email));

        if (user.getCliente() != null) {
            throw new ValidationException("usuario", "el usuario ya tiene un cliente asignado");
        }

        Cliente cliente = obtenerCliente(clienteId);

        if (cliente.getUsuario() != null) {
            throw new ValidationException("cliente", "el cliente ya está vinculado a otro usuario");
        }

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            cliente.setActivo(true);
        }

        cliente.setUsuario(user);
        Cliente guardado = clienteRepository.save(cliente);

        auditService.log(AuditAction.UPDATE, "Cliente", guardado.getId(),
                String.format("Cliente vinculado a usuario — Cliente #%d, Usuario email: %s",
                        guardado.getId(), email));

        eventPublisher.publishEvent(new SseDomainEvent(
            SseEventTypes.CLIENTE_CREADO,
            new ClientePayload(
                guardado.getId(),
                guardado.getNombreCompleto() + " " + guardado.getApellidoCompleto(),
                guardado.getCedula(),
                email,
                "VINCULACION")));

        return clienteMapper.toClienteResponse(guardado);
    }

    /**
     * Crea un perfil de cliente vinculado a una cuenta de usuario existente.
     * Utilizado para que los usuarios CLIENTE creen su propio perfil de cliente.
     * Valida que el usuario no tenga un cliente asignado, verifica la unicidad
     * de la cédula, asigna el municipio de expedición y tipo de identificación,
     * registra un evento de auditoría y publica un evento SSE.
     *
     * @param dto objeto que contiene los datos del cliente a crear
     * @param email email del usuario autenticado que crea el perfil
     * @return respuesta pública con los datos del cliente creado
     * @throws ResourceNotFoundException si el usuario, municipio o tipo de identificación no existen
     * @throws ResourceAlreadyExistsException si la cédula ya está registrada
     * @throws ValidationException si el usuario ya tiene un cliente asignado o los campos son inválidos
     */
    @Override
    public ClientePublicResponse crearMiPerfil(ClienteRequest dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("email", email));

        if (user.getCliente() != null) {
            throw new ValidationException("usuario", "el usuario ya tiene un cliente asignado");
        }

        validarCamposCliente(dto);

        clienteRepository.findByTipoIdentificacionIdAndCedula(dto.tipoIdentificacionId(), dto.cedula().trim())
                .ifPresent(cliente -> {
                    throw new ResourceAlreadyExistsException("Cliente", "cedula", dto.cedula());
                });

        Cliente cliente = clienteMapper.toEntity(dto);

        cliente.setMunicipioExpedicion(
                municipioRepository.findById(dto.municipioExpedicionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Municipio", dto.municipioExpedicionId()))
        );

        cliente.setTipoIdentificacion(
                tipoIdentificacionRepository.findById(dto.tipoIdentificacionId())
                        .orElseThrow(() -> new ResourceNotFoundException("TipoIdentificacion", dto.tipoIdentificacionId()))
        );

        cliente.setUsuario(user);
        cliente.setActivo(true);

        Cliente guardado = clienteRepository.save(cliente);

        auditService.log(AuditAction.CLIENT_REGISTERED, "Cliente", guardado.getId(),
                String.format("Cliente registrado — #%d\nEmail: %s\nCédula: %s\nNombre: %s %s",
                        guardado.getId(), email, guardado.getCedula(),
                        guardado.getNombreCompleto(), guardado.getApellidoCompleto()));

        eventPublisher.publishEvent(new SseDomainEvent(
                SseEventTypes.CLIENTE_CREADO,
                new ClientePayload(
                        guardado.getId(),
                        guardado.getNombreCompleto() + " " + guardado.getApellidoCompleto(),
                        guardado.getCedula(),
                        email,
                        "MI_PERFIL")));

        return clienteMapper.toClienteResponse(guardado);
    }

    /**
     * Actualiza los datos de un cliente existente identificado por su ID.
     * Registra un evento de auditoría con los cambios realizados.
     *
     * @param id identificador único del cliente a actualizar
     * @param dto objeto que contiene los nuevos datos del cliente
     * @return el cliente actualizado con los nuevos valores
     * @throws ResourceNotFoundException si el cliente con el ID especificado no existe
     */
    @Override
    public ClienteResponse actualizar(Long id, ClienteUpdateRequest dto) {
        Cliente cliente = obtenerCliente(id);
        clienteMapper.updateFromUpdateDto(dto, cliente);
        if (dto.activo() != null) {
            cliente.setActivo(dto.activo());
        }
        Cliente actualizado = clienteRepository.save(cliente);
        auditService.log(AuditAction.UPDATE, "Cliente", actualizado.getId(),
                String.format("Cliente actualizado — #%d\nCédula: %s\nNombre: %s %s\nActivo: %s",
                        actualizado.getId(), actualizado.getCedula(), actualizado.getNombreCompleto(),
                        actualizado.getApellidoCompleto(), actualizado.getActivo()));
        return clienteMapper.toAdminResponse(actualizado);
    }

    /**
     * Elimina un cliente del sistema por su identificador único.
     * Registra un evento de auditoría antes de eliminar el cliente.
     *
     * @param id identificador único del cliente a eliminar
     * @throws ResourceNotFoundException si el cliente con el ID especificado no existe
     */
    @Override
    public void eliminar(Long id) {
        Cliente cliente = obtenerCliente(id);
        auditService.log(AuditAction.DELETE, "Cliente", id,
                String.format("Cliente eliminado — #%d\nCédula: %s\nNombre: %s %s",
                        id, cliente.getCedula(), cliente.getNombreCompleto(), cliente.getApellidoCompleto()));
        clienteRepository.delete(cliente);
    }

    /**
     * Busca un cliente por su ID y lanza una excepción si no existe.
     *
     * @param id identificador único del cliente
     * @return el cliente encontrado
     * @throws ResourceNotFoundException si el cliente con el ID especificado no existe
     */
    private Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    /**
     * Valida los datos de creación de un cliente y verifica la unicidad de la cédula.
     *
     * @param dto objeto que contiene los datos del cliente a validar
     * @throws ResourceAlreadyExistsException si la cédula ya está registrada
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    private void validarCreacion(ClienteRequest dto) {
        validarCampos(dto);
        clienteRepository.findByCedula(dto.cedula())
                .ifPresent(cliente -> {
                    throw new ResourceAlreadyExistsException("Cliente", "cedula", dto.cedula());
                });
    }

    /**
     * Valida los campos obligatorios de un cliente para perfil propio.
     *
     * @param dto objeto que contiene los datos del cliente a validar
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    private void validarCamposCliente(ClienteRequest dto) {
        validarDatosCliente(dto.cedula(), dto.nombreCompleto(), dto.apellidoCompleto());
    }

    /**
     * Valida los campos obligatorios de un cliente para creación desde recepción.
     *
     * @param dto objeto que contiene los datos del cliente a validar
     * @throws ValidationException si los campos obligatorios son inválidos
     */
    private void validarCampos(ClienteRequest dto) {
        validarDatosCliente(dto.cedula(), dto.nombreCompleto(), dto.apellidoCompleto());
    }

    /**
     * Valida los datos básicos de un cliente: cédula, nombre y apellido.
     * Verifica que la cédula no esté vacía, y que nombre y apellido tengan
     * al menos 3 caracteres.
     *
     * @param cedula número de cédula del cliente
     * @param nombreCompleto nombre completo del cliente
     * @param apellidoCompleto apellido completo del cliente
     * @throws ValidationException si algún campo no cumple con los requisitos mínimos
     */
    private void validarDatosCliente(String cedula, String nombreCompleto, String apellidoCompleto) {
        if (cedula == null || cedula.isBlank()) {
            throw new ValidationException("cedula", "es obligatoria");
        }
        if (nombreCompleto == null || nombreCompleto.trim().length() < 3) {
            throw new ValidationException("nombreCompleto", "mínimo 3 caracteres");
        }
        if (apellidoCompleto == null || apellidoCompleto.trim().length() < 3) {
            throw new ValidationException("apellidoCompleto", "mínimo 3 caracteres");
        }
    }
}
