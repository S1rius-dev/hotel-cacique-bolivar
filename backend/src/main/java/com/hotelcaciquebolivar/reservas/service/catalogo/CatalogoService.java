package com.hotelcaciquebolivar.reservas.service.catalogo;

import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.catalogo.MunicipioDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.RolDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.TipoIdentificacionDTO;

/**
 * Interfaz que define las operaciones de consulta del catálogo del sistema hotelero.
 * Proporciona listados estáticos de datos de referencia como roles, tipos de identificación
 * y municipios, utilizados para poblar formularios y selectores en la interfaz de usuario.
 */
public interface CatalogoService {

    /**
     * Obtiene la lista de todos los roles disponibles en el sistema.
     *
     * @return lista de DTOs de roles
     */
    List<RolDTO> obtenerRoles();

    /**
     * Obtiene la lista de todos los tipos de identificación disponibles en el sistema.
     *
     * @return lista de DTOs de tipos de identificación
     */
    List<TipoIdentificacionDTO> obtenerTiposIdentificacion();

    /**
     * Obtiene la lista de todos los municipios registrados en el sistema.
     *
     * @return lista de DTOs de municipios
     */
    List<MunicipioDTO> obtenerMunicipios();

    /**
     * Busca municipios cuyo nombre coincida parcial o totalmente con el término de búsqueda.
     *
     * @param search término de búsqueda para filtrar municipios por nombre
     * @return lista de DTOs de municipios que coinciden con la búsqueda
     */
    List<MunicipioDTO> buscarMunicipios(String search);
}
