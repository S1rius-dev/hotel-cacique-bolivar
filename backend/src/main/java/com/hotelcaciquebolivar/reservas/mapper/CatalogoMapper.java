package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;

import com.hotelcaciquebolivar.reservas.dto.catalogo.MunicipioDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.RolDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.TipoIdentificacionDTO;
import com.hotelcaciquebolivar.reservas.entity.auth.Rol;
import com.hotelcaciquebolivar.reservas.entity.cliente.Municipio;
import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;

/**
 * Mapper MapStruct para la conversion de entidades de catalogo a sus DTOs correspondientes.
 *
 * <p>Proporciona mapeos para los siguientes catalogos:</p>
 * <ul>
 *   <li>{@link Rol} a {@link RolDTO} - Mediante metodo default que extrae el nombre del enum.</li>
 *   <li>{@link TipoIdentificacion} a {@link TipoIdentificacionDTO} - Mapeo directo de campos.</li>
 *   <li>{@link Municipio} a {@link MunicipioDTO} - Mapeo directo de campos.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    /**
     * Convierte un enum {@link Rol} en un DTO {@link RolDTO} con el nombre del rol.
     *
     * @param rol el enum de rol a convertir.
     * @return el DTO con el nombre del rol como cadena de texto.
     */
    default RolDTO toRolDTO(Rol rol) {
        return new RolDTO(rol.name());
    }

    /**
     * Convierte una entidad {@link TipoIdentificacion} en un DTO {@link TipoIdentificacionDTO}.
     *
     * @param tipo la entidad de tipo de identificacion a convertir.
     * @return el DTO con los campos mapeados directamente.
     */
    TipoIdentificacionDTO toTipoIdentificacionDTO(TipoIdentificacion tipo);

    /**
     * Convierte una entidad {@link Municipio} en un DTO {@link MunicipioDTO}.
     *
     * @param municipio la entidad de municipio a convertir.
     * @return el DTO con los campos mapeados directamente.
     */
    MunicipioDTO toMunicipioDTO(Municipio municipio);
}
