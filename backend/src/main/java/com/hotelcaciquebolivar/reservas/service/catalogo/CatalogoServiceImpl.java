package com.hotelcaciquebolivar.reservas.service.catalogo;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.catalogo.MunicipioDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.RolDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.TipoIdentificacionDTO;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.mapper.CatalogoMapper;
import com.hotelcaciquebolivar.reservas.repository.cliente.MunicipioRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.TipoIdentificacionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de catálogo del sistema hotelero.
 * Proporciona listados estáticos de datos de referencia como roles,
 * tipos de identificación y municipios, consultados desde la base de datos
 * o derivados de enumeraciones del sistema.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final MunicipioRepository municipioRepository;
    private final CatalogoMapper catalogoMapper;

    /**
     * Obtiene la lista de todos los roles disponibles en el sistema.
     * Los roles se derivan de la enumeración Rol.
     *
     * @return lista de DTOs de roles
     */
    @Override
    public List<RolDTO> obtenerRoles() {
        return Arrays.stream(Rol.values())
                .map(catalogoMapper::toRolDTO)
                .toList();
    }

    /**
     * Obtiene la lista de todos los tipos de identificación registrados en la base de datos.
     *
     * @return lista de DTOs de tipos de identificación
     */
    @Override
    public List<TipoIdentificacionDTO> obtenerTiposIdentificacion() {
        return tipoIdentificacionRepository.findAll()
                .stream()
                .map(catalogoMapper::toTipoIdentificacionDTO)
                .toList();
    }

    /**
     * Obtiene la lista de todos los municipios registrados en la base de datos.
     *
     * @return lista de DTOs de municipios
     */
    @Override
    public List<MunicipioDTO> obtenerMunicipios() {
        return municipioRepository.findAll()
                .stream()
                .map(catalogoMapper::toMunicipioDTO)
                .toList();
    }

    /**
     * Busca municipios cuyo nombre coincida parcial o totalmente con el término de búsqueda.
     * Retorna un máximo de 20 resultados.
     *
     * @param search término de búsqueda para filtrar municipios por nombre (case-insensitive)
     * @return lista de DTOs de municipios que coinciden con la búsqueda
     */
    @Override
    public List<MunicipioDTO> buscarMunicipios(String search) {
        return municipioRepository
                .findTop20ByNombreContainingIgnoreCase(search)
                .stream()
                .map(catalogoMapper::toMunicipioDTO)
                .toList();
    }
}
