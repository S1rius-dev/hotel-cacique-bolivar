// package com.hotelcaciquebolivar.reservas.service.user;

// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.Mockito;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import com.hotelcaciquebolivar.reservas.dto.clienteDTO.user.UserRequestDTO;
// import com.hotelcaciquebolivar.reservas.dto.clienteDTO.user.UserResponseDTO;
// import com.hotelcaciquebolivar.reservas.entity.seguridad.Rol;
// import com.hotelcaciquebolivar.reservas.entity.seguridad.User;
// import com.hotelcaciquebolivar.reservas.exception.CustomException;
// import com.hotelcaciquebolivar.reservas.repository.ClienteRepository;
// import com.hotelcaciquebolivar.reservas.repository.UserRepository;
// import com.hotelcaciquebolivar.reservas.service.domain.user.UserServiceImpl;
// import com.hotelcaciquebolivar.reservas.utils.MapperUtil;

// class UserServiceTest {

//     @Mock
//     private UserRepository userRepository;

//     @Mock
//     private ClienteRepository clienteRepository;

//     @Mock
//     private PasswordEncoder passwordEncoder;

//     @InjectMocks
//     private UserServiceImpl userService;

//     private AutoCloseable closeable;

//     @BeforeEach
//     void init() {
//         closeable = MockitoAnnotations.openMocks(this);
//     }

//     @AfterEach
//     void cleanup() throws Exception {
//         closeable.close();
//     }

//     // ============================================================
//     // TEST: CREAR USUARIO - HAPPY PATH
//     // ============================================================
//     @Test
//     void crearUsuario_HappyPath() {

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("juan");
//         dto.setPassword("1234");
//         dto.setRole(Rol.ADMIN);

//         User userMapped = new User();
//         userMapped.setRoles(new java.util.HashSet<>());

//         User savedUser = new User();
//         savedUser.setId(1L);

//         UserResponseDTO responseMock = new UserResponseDTO();
//         responseMock.setId(1L);

//         try (MockedStatic<MapperUtil> mockMapper = Mockito.mockStatic(MapperUtil.class)) {

//             when(userRepository.findByUsername("juan"))
//                     .thenReturn(Optional.empty());

//             mockMapper.when(() -> MapperUtil.toUserEntity(dto))
//                     .thenReturn(userMapped);

//             when(passwordEncoder.encode("1234"))
//                     .thenReturn("ENCODED");

//             when(userRepository.save(userMapped))
//                     .thenReturn(savedUser);

//             mockMapper.when(() -> MapperUtil.toUserDto(savedUser))
//                     .thenReturn(responseMock);

//             UserResponseDTO result = userService.crearUsuario(dto);

//             Assertions.assertEquals(1L, result.getId());
//             verify(userRepository).save(userMapped);
//         }
//     }

//     // ============================================================
//     // CREAR USUARIO - USERNAME VACÍO
//     // ============================================================
//     @Test
//     void crearUsuario_UsernameVacio() {

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("");
//         dto.setPassword("1234");

//         Assertions.assertThrows(CustomException.BadRequestException.class,
//                 () -> userService.crearUsuario(dto));
//     }

//     // ============================================================
//     // CREAR USUARIO - PASSWORD VACÍA
//     // ============================================================
//     @Test
//     void crearUsuario_PasswordVacia() {

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("juan");
//         dto.setPassword("");

//         Assertions.assertThrows(CustomException.BadRequestException.class,
//                 () -> userService.crearUsuario(dto));
//     }

//     // ============================================================
//     // CREAR USUARIO - USERNAME REPETIDO
//     // ============================================================
//     @Test
//     void crearUsuario_UsernameRepetido() {

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("juan");
//         dto.setPassword("1234");

//         when(userRepository.findByUsername("juan"))
//                 .thenReturn(Optional.of(new User()));

//         Assertions.assertThrows(CustomException.EntityAlreadyExistsException.class,
//                 () -> userService.crearUsuario(dto));
//     }

//     // ============================================================
//     // BUSCAR POR ID
//     // ============================================================
//     @Test
//     void buscarPorId_HappyPath() {
//         User user = new User();
//         user.setId(1L);

//         UserResponseDTO dto = new UserResponseDTO();
//         dto.setId(1L);

//         try (MockedStatic<MapperUtil> mockMapper = Mockito.mockStatic(MapperUtil.class)) {

//             when(userRepository.findById(1L)).thenReturn(Optional.of(user));

//             mockMapper.when(() -> MapperUtil.toUserDto(user))
//                     .thenReturn(dto);

//             UserResponseDTO result = userService.buscarPorId(1L);

//             Assertions.assertEquals(1L, result.getId());
//         }
//     }

//     @Test
//     void buscarPorId_NotFound() {
//         when(userRepository.findById(1L))
//                 .thenReturn(Optional.empty());

//         Assertions.assertThrows(CustomException.EntityNotFoundException.class,
//                 () -> userService.buscarPorId(1L));
//     }

//     // ============================================================
//     // LISTAR USUARIOS
//     // ============================================================
//     @Test
//     void listarUsuarios_HappyPath() {

//         User u1 = new User();
//         User u2 = new User();

//         UserResponseDTO d1 = new UserResponseDTO();
//         UserResponseDTO d2 = new UserResponseDTO();

//         try (MockedStatic<MapperUtil> mockMapper = Mockito.mockStatic(MapperUtil.class)) {

//             when(userRepository.findAll())
//                     .thenReturn(List.of(u1, u2));

//             mockMapper.when(() -> MapperUtil.toUserDto(u1)).thenReturn(d1);
//             mockMapper.when(() -> MapperUtil.toUserDto(u2)).thenReturn(d2);

//             List<UserResponseDTO> result = userService.listarUsuarios();

//             Assertions.assertEquals(2, result.size());
//         }
//     }

//     // ============================================================
//     // ACTUALIZAR USUARIO - CAMBIO DE USERNAME PROHIBIDO
//     // ============================================================
//     @Test
//     void actualizarUsuario_UsernameProhibido() {

//         User userDB = new User();
//         userDB.setId(1L);
//         userDB.setUsername("original");
//         userDB.getRoles().add(Rol.ADMIN);

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("nuevo"); // intento prohibido

//         when(userRepository.findById(1L))
//                 .thenReturn(Optional.of(userDB));

//         Assertions.assertThrows(CustomException.ForbiddenException.class,
//                 () -> userService.actualizarUsuario(1L, dto));
//     }

//     // ============================================================
//     // ACTUALIZAR USUARIO - CLIENTE INTENTA ENVIAR USERNAME
//     // ============================================================
//     @Test
//     void actualizarUsuario_ClienteNoPuedeModificarUsername() {

//         User userDB = new User();
//         userDB.setId(1L);
//         userDB.setUsername("clienteUser");
//         userDB.getRoles().add(Rol.CLIENTE);

//         UserRequestDTO dto = new UserRequestDTO();
//         dto.setUsername("nuevoIntento");

//         when(userRepository.findById(1L))
//                 .thenReturn(Optional.of(userDB));

//         Assertions.assertThrows(CustomException.ForbiddenException.class,
//                 () -> userService.actualizarUsuario(1L, dto));
//     }

//     // ============================================================
//     // ELIMINAR USUARIO
//     // ============================================================
//     @Test
//     void eliminarUsuario_HappyPath() {

//         User user = new User();
//         when(userRepository.findById(1L))
//                 .thenReturn(Optional.of(user));

//         userService.eliminarUsuario(1L);

//         verify(userRepository).delete(user);
//     }

//     // ============================================================
//     // CREAR CLIENTE VACÍO
//     // ============================================================
// }
