package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;

import com.hotelcaciquebolivar.reservas.dto.backup.BackupResponse;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;

/**
 * Mapper MapStruct para la conversion de {@link BackupHistory} a su DTO de respuesta.
 *
 * <p>La conversion es directa: los nombres de los campos de {@link BackupResponse} coinciden
 * con los de la entidad salvo {@code tamanoBytes}, que en la entidad puede ser nulo (una
 * ejecucion fallida no tiene archivo) y en el DTO se expone como {@code long} con valor cero.</p>
 */
@Mapper(componentModel = "spring")
public interface BackupMapper {

    /**
     * Convierte una entidad {@link BackupHistory} en su DTO {@link BackupResponse}.
     *
     * <p>No expone {@code storagePath} ni {@code mensajeError}: el primero revela la
     * estructura del almacenamiento y el segundo solo interesa en los logs del servidor.</p>
     *
     * @param backup la entidad de la copia de seguridad
     * @return el DTO de respuesta correspondiente
     */
    BackupResponse toResponse(BackupHistory backup);
}
