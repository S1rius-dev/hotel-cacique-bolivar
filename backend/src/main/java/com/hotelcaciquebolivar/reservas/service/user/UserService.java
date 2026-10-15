package com.hotelcaciquebolivar.reservas.service.user;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.user.UserRequest;
import com.hotelcaciquebolivar.reservas.dto.user.UserResponse;
import com.hotelcaciquebolivar.reservas.dto.user.UserUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz del servicio de gestión de usuarios del sistema.
 * Administra la creación, consulta, actualización y eliminación de usuarios,
 * incluyendo encriptación de contraseñas, asignación de roles y registro de auditoría.
 *
 * @author Hotel Cacique Bolívar
 */
public interface UserService {

    /**
     * Crea un nuevo usuario en el sistema.
     * Encripta la contraseña, establece el proveedor como LOCAL, asigna roles
     * y registra la acción de auditoría.
     *
     * @param dto datos del usuario a crear
     * @return el usuario creado con su respuesta de administrador
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceAlreadyExistsException si el email ya existe
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException         si faltan campos obligatorios
     */
    UserResponse crearUsuario(UserRequest dto);

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado en formato de respuesta de administrador
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    UserResponse buscarPorId(Long id);

    /**
     * Lista todos los usuarios de forma paginada.
     *
     * @param pageable configuración de paginación
     * @return página con los usuarios encontrados
     */
    PageResponse<UserResponse> listarUsuarios(Pageable pageable);

    /**
     * Actualiza un usuario existente.
     * Valida que el usuario esté activo y que no se intente cambiar el email a uno ya existente.
     * Encripta la contraseña solo si se proporciona una nueva. Registra la acción de auditoría.
     *
     * @param id  identificador del usuario a actualizar
     * @param dto datos actualizados
     * @return el usuario actualizado
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     * @throws com.hotelcaciquebolivar.reservas.exception.validation.ValidationException     si el usuario no está activo o el email ya existe
     */
    UserResponse actualizarUsuario(Long id, UserUpdateRequest dto);

    /**
     * Elimina un usuario del sistema por su identificador.
     * Registra la acción de auditoría antes de eliminar.
     *
     * @param id identificador del usuario a eliminar
     * @throws com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException si no se encuentra
     */
    void eliminarUsuario(Long id);

}
