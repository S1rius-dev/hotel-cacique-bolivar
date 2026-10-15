// package com.hotelcaciquebolivar.reservas.service.reserva;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;

// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AdminReservaRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.DetalleCheckInRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AdminReservaRequestDTO.DetalleReservaRequest;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AdminReservaResponseDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AdminReservaUpdateDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.DisponibilidadResponseDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.TitularInfoDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.ActualizarDetalleRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AgregarDetalleRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.AgregarHuespedesRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.adminDTO.reserva.ReservaRapidaRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.pagination.PageResponse;
// import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
// import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;
// import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
// import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
// import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
// import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
// import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;
// import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoDetalleReserva;
// import com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva;
// import com.hotelcaciquebolivar.reservas.entity.reserva.OrigenReserva;
// import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
// import com.hotelcaciquebolivar.reservas.exception.reserva.RoomNotAvailableException;
// import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
// import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
// import com.hotelcaciquebolivar.reservas.mapper.HabitacionMapper;
// import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
// import com.hotelcaciquebolivar.reservas.mapper.ReservaMapper;
// import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
// import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
// import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
// import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
// import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
// import com.hotelcaciquebolivar.reservas.service.reserva.ReservaServiceImpl;

// @ExtendWith(MockitoExtension.class)
// class ReservaServiceImplTest {

//     @Mock
//     private ReservaRepository reservaRepository;
//     @Mock
//     private ClienteRepository clienteRepository;
//     @Mock
//     private HabitacionRepository habitacionRepository;
//     @Mock
//     private ReservaMapper reservaMapper;
//     @Mock
//     private PageMapper pageMapper;
//     @Mock
//     private HabitacionMapper habitacionMapper;
//     @Mock
//     private AuditService auditService;
//     @Mock
//     private DetalleReservaRepository detalleReservaRepository;

//     @InjectMocks
//     private ReservaServiceImpl reservaService;

//     private Cliente cliente;
//     private Habitacion habitacion;
//     private TipoHabitacion tipoHabitacion;

//     @BeforeEach
//     void setUp() {
//         TipoIdentificacion tipoId = new TipoIdentificacion();
//         tipoId.setId(1L);
//         tipoId.setNombre("CC");

//         cliente = new Cliente();
//         cliente.setId(1L);
//         cliente.setNombreCompleto("Juan");
//         cliente.setApellidoCompleto("Perez");
//         cliente.setCedula("1234567890");
//         cliente.setTipoIdentificacion(tipoId);
//         cliente.setTelefono("3001234567");

//         tipoHabitacion = new TipoHabitacion();
//         tipoHabitacion.setId(1L);
//         tipoHabitacion.setNombre("Suite");
//         tipoHabitacion.setCapacidadBase(2);
//         tipoHabitacion.setCapacidadMaxima(4);
//         tipoHabitacion.setPrecioBase(new BigDecimal("200"));
//         tipoHabitacion.setPrecioPersonaExtra(new BigDecimal("50"));

//         habitacion = new Habitacion();
//         habitacion.setId(1L);
//         habitacion.setNumero("101");
//         habitacion.setTipo(tipoHabitacion);
//         habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
//     }

//     // ============================================================
//     // CREAR
//     // ============================================================
//     @Test
//     void crear_deberiaCrearReservaCorrectamente() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(4);

//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, checkIn, checkOut, OrigenReserva.WEB, "sin observaciones",
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, "nada")));

//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));

//         Reserva reservaGuardada = Reserva.builder()
//                 .id(1L)
//                 .titularReserva(cliente)
//                 .fechaCheckIn(checkIn)
//                 .fechaCheckOut(checkOut)
//                 .origen(OrigenReserva.WEB)
//                 .estado(EstadoReserva.PENDIENTE)
//                 .build();

//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, "2026-00001",
//                 new TitularInfoDTO(1L, "1234567890", "CC", "Juan", "Perez", "3001234567"),
//                 null, checkIn, checkOut, 3L,
//                 EstadoReserva.PENDIENTE, OrigenReserva.WEB,
//                 new BigDecimal("750"), null, null, "sin observaciones", List.of());
 
//          when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.crear(dto);

//         assertNotNull(result);
//         assertEquals(1L, result.id());
//         assertEquals(EstadoReserva.PENDIENTE, result.estado());
//         assertEquals(EstadoHabitacion.RESERVADA, habitacion.getEstado());
//         verify(reservaRepository).save(any(Reserva.class));
//     }

//     @Test
//     void crear_checkInPasado_deberiaLanzarExcepcion() {
//         LocalDate checkIn = LocalDate.now().minusDays(1);
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, checkIn, LocalDate.now().plusDays(2), OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, null)));

//         assertThrows(ValidationException.class, () -> reservaService.crear(dto));
//         verify(reservaRepository, never()).save(any());
//     }

//     @Test
//     void crear_checkOutIgualACheckIn_deberiaLanzarExcepcion() {
//         LocalDate fecha = LocalDate.now().plusDays(1);
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, fecha, fecha, OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, null)));

//         assertThrows(ValidationException.class, () -> reservaService.crear(dto));
//     }

//     @Test
//     void crear_estadiaExcedeMaximo_deberiaLanzarExcepcion() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, checkIn, checkIn.plusDays(31), OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, null)));

//         assertThrows(ValidationException.class, () -> reservaService.crear(dto));
//     }

//     @Test
//     void crear_clienteNoEncontrado_deberiaLanzarExcepcion() {
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 99L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
//                 OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, null)));

//         when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

//         assertThrows(ResourceNotFoundException.class, () -> reservaService.crear(dto));
//     }

//     @Test
//     void crear_habitacionNoDisponible_deberiaLanzarExcepcion() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(4);

//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, checkIn, checkOut, OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 2, null)));

//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.empty());
//         when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

//         assertThrows(RoomNotAvailableException.class, () -> reservaService.crear(dto));
//     }

//     @Test
//     void crear_precioMenorAlBase_deberiaLanzarExcepcion() {
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
//                 OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("100"), 2, null)));

//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L,
//                 LocalDate.now().plusDays(1), LocalDate.now().plusDays(4)))
//                 .thenReturn(Optional.of(habitacion));

//         assertThrows(ValidationException.class, () -> reservaService.crear(dto));
//     }

//     @Test
//     void crear_capacidadExcedida_deberiaLanzarExcepcion() {
//         AdminReservaRequestDTO dto = new AdminReservaRequestDTO(
//                 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
//                 OrigenReserva.WEB, null,
//                 List.of(new DetalleReservaRequest(1L, new BigDecimal("250"), 5, null)));

//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L,
//                 LocalDate.now().plusDays(1), LocalDate.now().plusDays(4)))
//                 .thenReturn(Optional.of(habitacion));

//         assertThrows(ValidationException.class, () -> reservaService.crear(dto));
//     }

//     // ============================================================
//     // BUSCAR POR ID
//     // ============================================================
//     @Test
//     void buscarPorId_deberiaRetornarReserva() {
//         Reserva reserva = new Reserva();
//         reserva.setId(1L);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, "2026-00001", null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
 
//          when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//          when(reservaMapper.toResponse(reserva)).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.buscarPorId(1L);
//         assertEquals(1L, result.id());
//     }

//     @Test
//     void buscarPorId_noEncontrado_deberiaLanzarExcepcion() {
//         when(reservaRepository.findById(1L)).thenReturn(Optional.empty());
//         assertThrows(ResourceNotFoundException.class, () -> reservaService.buscarPorId(1L));
//     }

//     // ============================================================
//     // LISTAR TODOS
//     // ============================================================
//     @Test
//     void listarTodos_deberiaRetornarPagina() {
//         Pageable pageable = PageRequest.of(0, 14);
//         Page<Reserva> page = new PageImpl<>(List.of(new Reserva(), new Reserva()));
//         @SuppressWarnings("unchecked")
//         PageResponse<AdminReservaResponseDTO> pageResponse = mock(PageResponse.class);

//         when(reservaRepository.findAll(pageable)).thenReturn(page);
//         doReturn(pageResponse).when(pageMapper).toPageResponse(any(Page.class), any());

//         PageResponse<AdminReservaResponseDTO> result = reservaService.listarTodos(pageable);
//         assertNotNull(result);
//     }

//     // ============================================================
//     // ACTUALIZAR
//     // ============================================================
//     @Test
//     void actualizar_deberiaActualizarCheckOut() {
//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, LocalDate.now().plusDays(5), null, null, null, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, "2026-00001", null, null,
//                 reserva.getFechaCheckIn(), LocalDate.now().plusDays(5), 4L,
//                 EstadoReserva.PENDIENTE, OrigenReserva.RECEPCION, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.actualizar(1L, dto);
//         assertNotNull(result);
//         assertEquals(LocalDate.now().plusDays(5), result.fechaCheckOut());
//     }

//     @Test
//     void actualizar_estadoTransicionValida_deberiaCambiarEstado() {
//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, EstadoReserva.CONFIRMADA, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.CONFIRMADA, null, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizar(1L, dto);
//         assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
//     }

//     @Test
//     void actualizar_estadoTransicionInvalida_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, EstadoReserva.CANCELADA, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         assertThrows(ValidationException.class, () -> reservaService.actualizar(1L, dto));
//     }

//     // ============================================================
//     // ELIMINAR
//     // ============================================================
//     @Test
//     void eliminar_deberiaEliminarReserva() {
//         Reserva reserva = Reserva.builder().id(1L).titularReserva(cliente).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         reservaService.eliminar(1L);
//         verify(reservaRepository).delete(reserva);
//     }

//     @Test
//     void eliminar_reservaEnCurso_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class, () -> reservaService.eliminar(1L));
//         verify(reservaRepository, never()).delete(any());
//     }

//     // ============================================================
//     // CHECK-IN GLOBAL
//     // ============================================================
//     @Test
//     void realizarCheckIn_deberiaCambiarEstados() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200"))
//                 .cantidadOcupantes(2)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now())
//                 .fechaCheckOut(LocalDate.now().plusDays(2))
//                 .detalles(List.of(detalle))
//                 .build();
//         reserva.setEstado(EstadoReserva.CONFIRMADA);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.EN_CURSO, null, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.realizarCheckIn(1L);

//         assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
//         assertEquals(EstadoDetalleReserva.OCUPADA, detalle.getEstado());
//         assertEquals(EstadoHabitacion.OCUPADA, habitacion.getEstado());
//         verify(habitacionRepository).saveAll(anyList());
//     }

//     @Test
//     void realizarCheckIn_estadoInvalido_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         assertThrows(ValidationException.class, () -> reservaService.realizarCheckIn(1L));
//     }

//     // ============================================================
//     // CHECK-OUT GLOBAL
//     // ============================================================
//     @Test
//     void realizarCheckOut_deberiaLiberarHabitacion() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200"))
//                 .cantidadOcupantes(2)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.OCUPADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().minusDays(2))
//                 .fechaCheckOut(LocalDate.now())
//                 .detalles(List.of(detalle))
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.FINALIZADA, null, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.realizarCheckOut(1L);

//         assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
//         assertEquals(EstadoDetalleReserva.LIBERADA, detalle.getEstado());
//         assertEquals(EstadoHabitacion.EN_LIMPIEZA, habitacion.getEstado());
//         verify(habitacionRepository).saveAll(anyList());
//     }

//     // ============================================================
//     // CHECK-IN POR DETALLE
//     // ============================================================
//     @Test
//     void checkInPorDetalle_deberiaAsignarHuespedesYCambiarEstados() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L)
//                 .habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200"))
//                 .cantidadOcupantes(4)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().minusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(2))
//                 .detalles(List.of(detalle))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         DetalleCheckInRequestDTO dto = new DetalleCheckInRequestDTO(List.of(1L));

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.EN_CURSO, null, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.checkInPorDetalle(1L, 10L, dto);

//         assertEquals(1, detalle.getHuespedes().size());
//         assertEquals(EstadoDetalleReserva.OCUPADA, detalle.getEstado());
//         assertEquals(EstadoHabitacion.OCUPADA, habitacion.getEstado());
//         assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
//         verify(habitacionRepository).save(habitacion);
//         verify(reservaRepository).save(reserva);
//     }

//     // ============================================================
//     // CHECK-OUT POR DETALLE
//     // ============================================================
//     @Test
//     void checkOutPorDetalle_deberiaLiberarHabitacionYSincronizarReserva() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L)
//                 .habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200"))
//                 .cantidadOcupantes(2)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.OCUPADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().minusDays(2))
//                 .fechaCheckOut(LocalDate.now())
//                 .detalles(List.of(detalle))
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.FINALIZADA, null, null, null, null, null, List.of());

//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.checkOutPorDetalle(1L, 10L);

//         assertEquals(EstadoDetalleReserva.LIBERADA, detalle.getEstado());
//         assertEquals(EstadoHabitacion.EN_LIMPIEZA, habitacion.getEstado());
//         assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
//         verify(habitacionRepository).save(habitacion);
//         verify(reservaRepository).save(reserva);
//     }

//     // ============================================================
//     // CONSULTAR DISPONIBILIDAD
//     // ============================================================
//     @Test
//     void consultarDisponibilidad_deberiaRetornarHabitaciones() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(3);

//         Habitacion h2 = new Habitacion();
//         h2.setId(2L);
//         h2.setNumero("102");
//         h2.setTipo(tipoHabitacion);
//         h2.setEstado(EstadoHabitacion.DISPONIBLE);

//         when(habitacionRepository.findAllDisponibles(checkIn, checkOut))
//                 .thenReturn(List.of(habitacion, h2));
//         when(habitacionMapper.toDisponibilidadResponseDTO(habitacion))
//                 .thenReturn(new DisponibilidadResponseDTO(1L, "101", "Suite", BigDecimal.valueOf(200), 2, 4, BigDecimal.valueOf(30)));
//         when(habitacionMapper.toDisponibilidadResponseDTO(h2))
//                 .thenReturn(new DisponibilidadResponseDTO(2L, "102", "Suite", BigDecimal.valueOf(200), 2, 4, BigDecimal.valueOf(30)));

//         List<DisponibilidadResponseDTO> result = reservaService.consultarDisponibilidad(checkIn, checkOut);

//         assertEquals(2, result.size());
//         assertEquals("101", result.get(0).numero());
//         assertEquals("102", result.get(1).numero());
//     }

//     @Test
//     void consultarDisponibilidad_fechasInvalidas_deberiaLanzarExcepcion() {
//         assertThrows(ValidationException.class,
//                 () -> reservaService.consultarDisponibilidad(
//                         LocalDate.now().plusDays(1), LocalDate.now().plusDays(1)));
//     }

//     // ============================================================
//     // ACTUALIZAR DETALLES - NUEVO DETALLE
//     // ============================================================
//     @Test
//     void actualizar_conNuevoDetalle_deberiaAgregarlo() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(3);

//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(checkIn)
//                 .fechaCheckOut(checkOut)
//                 .detalles(new java.util.ArrayList<>())
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AdminReservaUpdateDTO.DetalleReservaUpdateRequest nuevoDetalleDto
//                 = new AdminReservaUpdateDTO.DetalleReservaUpdateRequest(
//                         null, 1L, new BigDecimal("250"), 2, "nuevo");

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, null, null, List.of(nuevoDetalleDto));

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
 
//          when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);
 
//          reservaService.actualizar(1L, dto);

//         assertEquals(1, reserva.getDetalles().size());
//         verify(reservaRepository).save(reserva);
//     }

//     // ============================================================
//     // AGREGAR HUESPEDES
//     // ============================================================
//     @Test
//     void agregarHuespedes_deberiaAgregarHuespedes() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(4)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.agregarHuespedes(1L, 10L, List.of(1L));

//         assertEquals(1, detalle.getHuespedes().size());
//         verify(reservaRepository).save(reserva);
//     }

//     @Test
//     void agregarHuespedes_reservaFinalizada_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder().id(10L).habitacion(habitacion).build();
//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.agregarHuespedes(1L, 10L, List.of(1L)));
//     }

//     @Test
//     void agregarHuespedes_clienteNoEncontrado_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder().id(10L).habitacion(habitacion).build();
//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

//         assertThrows(ResourceNotFoundException.class,
//                 () -> reservaService.agregarHuespedes(1L, 10L, List.of(99L)));
//     }

//     // ============================================================
//     // REMOVER HUESPED
//     // ============================================================
//     @Test
//     void removerHuesped_deberiaRemoverHuesped() {
//         DetalleReservaHuesped drh = DetalleReservaHuesped.builder()
//                 .id(100L).cliente(cliente).build();

//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .huespedes(new java.util.ArrayList<>(List.of(drh)))
//                 .build();
//         drh.setDetalleReserva(detalle);

//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.removerHuesped(1L, 10L, 100L);

//         assertTrue(detalle.getHuespedes().isEmpty());
//         verify(reservaRepository).save(reserva);
//     }

//     @Test
//     void removerHuesped_reservaCancelada_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).detalles(new java.util.ArrayList<>()).build();
//         reserva.setEstado(EstadoReserva.CANCELADA);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.removerHuesped(1L, 10L, 100L));
//     }

//     @Test
//     void removerHuesped_noEncontrado_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .huespedes(new java.util.ArrayList<>())
//                 .build();

//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ResourceNotFoundException.class,
//                 () -> reservaService.removerHuesped(1L, 10L, 999L));
//     }

//     // ============================================================
//     // REMOVER DETALLE
//     // ============================================================
//     @Test
//     void removerDetalle_deberiaCancelarDetalleYLiberarHabitacion() {
//         Habitacion hab1 = new Habitacion();
//         hab1.setId(1L);
//         hab1.setNumero("101");
//         hab1.setTipo(tipoHabitacion);
//         hab1.setEstado(EstadoHabitacion.RESERVADA);

//         Habitacion hab2 = new Habitacion();
//         hab2.setId(2L);
//         hab2.setNumero("102");
//         hab2.setTipo(tipoHabitacion);
//         hab2.setEstado(EstadoHabitacion.RESERVADA);

//         DetalleReserva detalle1 = DetalleReserva.builder()
//                 .id(10L).habitacion(hab1).build();
//         DetalleReserva detalle2 = DetalleReserva.builder()
//                 .id(20L).habitacion(hab2).precioPorNoche(new BigDecimal("200"))
//                 .cantidadOcupantes(2).build();

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(new java.util.ArrayList<>(List.of(detalle1, detalle2)))
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle1.setReserva(reserva);
//         detalle2.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.removerDetalle(1L, 10L);

//         assertEquals(EstadoDetalleReserva.CANCELADA, detalle1.getEstado());
//         assertEquals(EstadoHabitacion.DISPONIBLE, hab1.getEstado());
//     }

//     @Test
//     void removerDetalle_unicoDetalle_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion).build();
//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.removerDetalle(1L, 10L));
//     }

//     @Test
//     void removerDetalle_estadoEnCurso_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder().id(10L).habitacion(habitacion).build();
//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.removerDetalle(1L, 10L));
//     }

//     // ============================================================
//     // AGREGAR DETALLE
//     // ============================================================
//     @Test
//     void agregarDetalle_deberiaAgregarNuevoDetalle() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(3);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(new java.util.ArrayList<>())
//                 .fechaCheckIn(checkIn).fechaCheckOut(checkOut)
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AgregarDetalleRequestDTO dto = new AgregarDetalleRequestDTO(
//                 1L, new BigDecimal("250"), 2, "nota");

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.agregarDetalle(1L, dto);

//         assertEquals(1, reserva.getDetalles().size());
//         assertEquals(new BigDecimal("250"), reserva.getDetalles().get(0).getPrecioPorNoche());
//         verify(reservaRepository).save(reserva);
//     }

//     @Test
//     void agregarDetalle_sinPrecio_deberiaUsarPrecioBase() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(3);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(new java.util.ArrayList<>())
//                 .fechaCheckIn(checkIn).fechaCheckOut(checkOut)
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AgregarDetalleRequestDTO dto = new AgregarDetalleRequestDTO(
//                 1L, null, 2, null);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.agregarDetalle(1L, dto);

//         assertEquals(1, reserva.getDetalles().size());
//         assertEquals(new BigDecimal("200"), reserva.getDetalles().get(0).getPrecioPorNoche());
//     }

//     @Test
//     void agregarDetalle_estadoFinalizada_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         AgregarDetalleRequestDTO dto = new AgregarDetalleRequestDTO(1L, null, 2, null);
//         assertThrows(ValidationException.class,
//                 () -> reservaService.agregarDetalle(1L, dto));
//     }

//     // ============================================================
//     // ACTUALIZAR DETALLE
//     // ============================================================
//     @Test
//     void actualizarDetalle_deberiaActualizarOcupantes() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(2)
//                 .huespedes(new java.util.ArrayList<>())
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(List.of(detalle))
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizarDetalle(1L, 10L, new ActualizarDetalleRequestDTO(3, null));

//         assertEquals(3, detalle.getCantidadOcupantes());
//         verify(reservaRepository).save(reserva);
//     }

//     @Test
//     void actualizarDetalle_reservaFinalizada_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizarDetalle(1L, 10L,
//                         new ActualizarDetalleRequestDTO(3, null)));
//     }

//     @Test
//     void actualizarDetalle_detalleCancelado_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion).build();
//         detalle.setEstado(EstadoDetalleReserva.CANCELADA);

//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizarDetalle(1L, 10L,
//                         new ActualizarDetalleRequestDTO(3, null)));
//     }

//     @Test
//     void actualizarDetalle_capacidadExcedida_deberiaLanzarExcepcion() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .cantidadOcupantes(2)
//                 .huespedes(new java.util.ArrayList<>())
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle)).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizarDetalle(1L, 10L,
//                         new ActualizarDetalleRequestDTO(5, null)));
//     }

//     @Test
//     void actualizarDetalle_ocupantesMenorQueHuespedes_deberiaLanzarExcepcion() {
//         Cliente c2 = new Cliente();
//         c2.setId(2L);
//         c2.setNombreCompleto("Maria");
//         c2.setCedula("987654321");

//         DetalleReservaHuesped drh = DetalleReservaHuesped.builder()
//                 .id(100L).cliente(c2).build();

//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(3)
//                 .huespedes(new java.util.ArrayList<>(List.of(drh)))
//                 .build();
//         drh.setDetalleReserva(detalle);
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder().id(1L).detalles(List.of(detalle))
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizarDetalle(1L, 10L,
//                         new ActualizarDetalleRequestDTO(0, null)));
//     }

//     @Test
//     void actualizarDetalle_deberiaActualizarObservaciones() {
//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(2)
//                 .huespedes(new java.util.ArrayList<>())
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(List.of(detalle))
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizarDetalle(1L, 10L,
//                 new ActualizarDetalleRequestDTO(2, "nuevas notas"));

//         assertEquals("nuevas notas", detalle.getObservaciones());
//     }

//     // ============================================================
//     // EXTENDER ESTADIA
//     // ============================================================
//     @Test
//     void extenderEstadia_deberiaExtenderCheckOut() {
//         LocalDate checkOutActual = LocalDate.now().plusDays(1);
//         LocalDate nuevoCheckOut = LocalDate.now().plusDays(3);

//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(2)
//                 .build();

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(List.of(detalle))
//                 .fechaCheckIn(LocalDate.now()).fechaCheckOut(checkOutActual)
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(detalleReservaRepository.findConflictoExtension(
//                 habitacion.getId(), detalle.getId(), checkOutActual, nuevoCheckOut))
//                 .thenReturn(Optional.empty());
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, nuevoCheckOut, 3L,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.extenderEstadia(1L, nuevoCheckOut);

//         assertEquals(nuevoCheckOut, result.fechaCheckOut());
//         verify(reservaRepository).save(reserva);
//     }

//     @Test
//     void extenderEstadia_estadoFinalizada_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.FINALIZADA);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.extenderEstadia(1L, LocalDate.now().plusDays(3)));
//     }

//     @Test
//     void extenderEstadia_fechaAnterior_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L)
//                 .fechaCheckIn(LocalDate.now().minusDays(2))
//                 .fechaCheckOut(LocalDate.now())
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.extenderEstadia(1L, LocalDate.now().minusDays(1)));
//     }

//     @Test
//     void extenderEstadia_excedeMaximo_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L)
//                 .fechaCheckIn(LocalDate.now())
//                 .fechaCheckOut(LocalDate.now().plusDays(1))
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.extenderEstadia(1L, LocalDate.now().plusDays(32)));
//     }

//     @Test
//     void extenderEstadia_conflictoConOtraReserva_deberiaLanzarExcepcion() {
//         LocalDate checkOutActual = LocalDate.now().plusDays(1);
//         LocalDate nuevoCheckOut = LocalDate.now().plusDays(3);

//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habitacion).build();

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(List.of(detalle))
//                 .fechaCheckIn(LocalDate.now()).fechaCheckOut(checkOutActual)
//                 .build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);
//         detalle.setReserva(reserva);

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(detalleReservaRepository.findConflictoExtension(
//                 habitacion.getId(), detalle.getId(), checkOutActual, nuevoCheckOut))
//                 .thenReturn(Optional.of(new DetalleReserva()));

//         assertThrows(RoomNotAvailableException.class,
//                 () -> reservaService.extenderEstadia(1L, nuevoCheckOut));
//     }

//     // ============================================================
//     // ACTUALIZAR - FECHA CHECK-IN Y TITULAR
//     // ============================================================
//     @Test
//     void actualizar_fechaCheckInEnCurso_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 LocalDate.now().plusDays(5), null, null, null, null, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizar(1L, dto));
//     }

//     @Test
//     void actualizar_titularReservaIdEnCurso_deberiaLanzarExcepcion() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.EN_CURSO);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, 2L, null, null, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

//         assertThrows(ValidationException.class,
//                 () -> reservaService.actualizar(1L, dto));
//     }

//     @Test
//     void actualizar_deberiaCambiarFechaCheckInYTitular() {
//         Reserva reserva = Reserva.builder()
//                 .id(1L)
//                 .fechaCheckIn(LocalDate.now().plusDays(1))
//                 .fechaCheckOut(LocalDate.now().plusDays(3))
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         LocalDate nuevoCheckIn = LocalDate.now().plusDays(2);
//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 nuevoCheckIn, null, 2L, null, null, null, List.of());

//         Cliente nuevoTitular = new Cliente();
//         nuevoTitular.setId(2L);
//         nuevoTitular.setNombreCompleto("Maria");
//         nuevoTitular.setCedula("987654321");

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(clienteRepository.findById(2L)).thenReturn(Optional.of(nuevoTitular));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, nuevoCheckIn, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.actualizar(1L, dto);

//         assertEquals(nuevoCheckIn, result.fechaCheckIn());
//         assertEquals(2L, reserva.getTitularReserva().getId());
//     }

//     @Test
//     void actualizar_deberiaCambiarEstadoCancelar() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, EstadoReserva.CANCELADA, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.CANCELADA, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizar(1L, dto);

//         assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
//     }

//     @Test
//     void actualizar_deberiaCambiarEstadoNoShow() {
//         Reserva reserva = Reserva.builder().id(1L).build();
//         reserva.setEstado(EstadoReserva.CONFIRMADA);

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, EstadoReserva.NO_SHOW, null, List.of());

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.NO_SHOW, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizar(1L, dto);

//         assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
//     }

//     // ============================================================
//     // CREAR RESERVA RAPIDA
//     // ============================================================
//     @Test
//     void crearReservaRapida_deberiaCrearNuevaReserva() {
//         LocalDate checkIn = LocalDate.now();
//         LocalDate checkOut = LocalDate.now().plusDays(1);

//         ReservaRapidaRequestDTO dto = new ReservaRapidaRequestDTO(
//                 1L, checkIn, checkOut, 2, List.of(1L));

//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));
//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
//             Reserva r = inv.getArgument(0);
//             r.setId(1L);
//             return r;
//         });

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 EstadoReserva.EN_CURSO, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         AdminReservaResponseDTO result = reservaService.crearReservaRapida(dto);

//         assertEquals(EstadoReserva.EN_CURSO, result.estado());
//         verify(reservaRepository).save(any(Reserva.class));
//     }

//     @Test
//     void crearReservaRapida_habitacionNoDisponible_deberiaLanzarExcepcion() {
//         ReservaRapidaRequestDTO dto = new ReservaRapidaRequestDTO(
//                 1L, LocalDate.now(), LocalDate.now().plusDays(1), 2, List.of(1L));

//         when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, LocalDate.now(), LocalDate.now().plusDays(1)))
//                 .thenReturn(Optional.empty());
//         when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

//         assertThrows(RoomNotAvailableException.class,
//                 () -> reservaService.crearReservaRapida(dto));
//     }

//     // ============================================================
//     // ACTUALIZAR DETALLES - CAMBIAR HABITACION
//     // ============================================================
//     @Test
//     void actualizar_cambiarHabitacionEnDetalle_deberiaLiberarAnterior() {
//         LocalDate checkIn = LocalDate.now().plusDays(1);
//         LocalDate checkOut = LocalDate.now().plusDays(3);

//         Habitacion habVieja = new Habitacion();
//         habVieja.setId(2L);
//         habVieja.setNumero("102");
//         habVieja.setTipo(tipoHabitacion);
//         habVieja.setEstado(EstadoHabitacion.RESERVADA);

//         DetalleReserva detalle = DetalleReserva.builder()
//                 .id(10L).habitacion(habVieja)
//                 .precioPorNoche(new BigDecimal("200")).cantidadOcupantes(2)
//                 .build();
//         detalle.setEstado(EstadoDetalleReserva.RESERVADA);

//         Reserva reserva = Reserva.builder()
//                 .id(1L).detalles(new java.util.ArrayList<>(List.of(detalle)))
//                 .fechaCheckIn(checkIn).fechaCheckOut(checkOut)
//                 .build();
//         reserva.setEstado(EstadoReserva.PENDIENTE);
//         detalle.setReserva(reserva);

//         AdminReservaUpdateDTO.DetalleReservaUpdateRequest updateReq
//                 = new AdminReservaUpdateDTO.DetalleReservaUpdateRequest(
//                         10L, 1L, null, 2, "cambio habitacion");

//         AdminReservaUpdateDTO dto = new AdminReservaUpdateDTO(
//                 null, null, null, null, null, null, List.of(updateReq));

//         when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
//         when(habitacionRepository.findDisponiblePorIdConLock(1L, checkIn, checkOut))
//                 .thenReturn(Optional.of(habitacion));
//         when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

//         AdminReservaResponseDTO responseMock = new AdminReservaResponseDTO(
//                 1L, null, null, null, null, null, null,
//                 null, null, null, null, null, null, List.of());
//         when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseMock);

//         reservaService.actualizar(1L, dto);

//         assertEquals(EstadoHabitacion.DISPONIBLE, habVieja.getEstado());
//         assertEquals(habitacion.getId(), detalle.getHabitacion().getId());
//     }
// }
