package com.hotelcaciquebolivar.reservas.service.resena;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.resena.PublicResenasResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaRequest;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaResponse;
import com.hotelcaciquebolivar.reservas.dto.resena.ResenaUpdateRequest;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.entity.auth.User;
import com.hotelcaciquebolivar.reservas.entity.resena.Resena;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.mapper.PageMapper;
import com.hotelcaciquebolivar.reservas.mapper.ResenaMapper;
import com.hotelcaciquebolivar.reservas.repository.resena.ResenaRepository;
import com.hotelcaciquebolivar.reservas.repository.user.UserRepository;

/**
 * Implementación del servicio de gestión de reseñas del hotel.
 * Administra la creación, consulta, actualización y eliminación de reseñas,
 * asociando automáticamente datos del usuario autenticado cuando estén disponibles.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
@Transactional
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final UserRepository userRepository;
    private final ResenaMapper resenaMapper;
    private final PageMapper pageMapper;

    public ResenaServiceImpl(
            ResenaRepository resenaRepository,
            UserRepository userRepository,
            ResenaMapper resenaMapper,
            PageMapper pageMapper
    ) {
        this.resenaRepository = resenaRepository;
        this.userRepository = userRepository;
        this.resenaMapper = resenaMapper;
        this.pageMapper = pageMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si el nombre no se proporciona, se completa desde el username del usuario.
     * Si la URL de imagen no se proporciona, se completa desde el profilePicture del usuario.
     * Si el usuario tiene un cliente asociado, se vincula a la reseña.
     */
    @Override
    public ResenaResponse crear(ResenaRequest dto, String email) {
        User usuario = null;
        if (email != null) {
            usuario = userRepository.findByEmail(email).orElse(null);
        }

        Resena resena = resenaMapper.toEntity(dto);

        if (dto.name() == null || dto.name().isBlank()) {
            if (usuario != null) {
                resena.setName(usuario.getUsername());
            }
        }

        if (dto.pictureUrl() == null || dto.pictureUrl().isBlank()) {
            if (usuario != null && usuario.getProfilePicture() != null) {
                resena.setPictureUrl(usuario.getProfilePicture());
            }
        }

        if (usuario != null && usuario.getCliente() != null) {
            resena.setCliente(usuario.getCliente());
        }

        Resena guardada = resenaRepository.save(resena);
        return resenaMapper.toResponse(guardada);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ResenaResponse buscarPorId(Long id) {
        Resena resena = obtenerResena(id);
        return resenaMapper.toResponse(resena);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PublicResenasResponse listarPublicas() {
        List<Resena> resenas = resenaRepository.findTop12ByOrderByCreatedAtDesc();
        return new PublicResenasResponse(
                resenas.stream().map(resenaMapper::toResponse).toList()
        );
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResenaResponse> listarTodos(Pageable pageable) {
        Page<Resena> page = resenaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return pageMapper.toPageResponse(page, resenaMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResenaResponse> listarPorCalificacion(Integer calificacion, Pageable pageable) {
        Page<Resena> page = resenaRepository.findByCalificacion(calificacion, pageable);
        return pageMapper.toPageResponse(page, resenaMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResenaResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        Page<Resena> page = resenaRepository.findByClienteId(clienteId, pageable);
        return pageMapper.toPageResponse(page, resenaMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    public ResenaResponse actualizar(Long id, ResenaUpdateRequest dto) {
        Resena resena = obtenerResena(id);
        resenaMapper.updateFromDto(dto, resena);
        Resena actualizada = resenaRepository.save(resena);
        return resenaMapper.toResponse(actualizada);
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(Long id) {
        Resena resena = obtenerResena(id);
        resenaRepository.delete(resena);
    }

    /**
     * Obtiene una reseña por su identificador o lanza excepción si no existe.
     *
     * @param id identificador de la reseña
     * @return la entidad Resena
     * @throws ResourceNotFoundException si no se encuentra la reseña
     */
    private Resena obtenerResena(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña", id));
    }
}
