package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hotelcaciquebolivar.reservas.dto.pago.PagoDesgloseResponse;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoRequest;
import com.hotelcaciquebolivar.reservas.dto.pago.PagoResponse;
import com.hotelcaciquebolivar.reservas.entity.pago.Pago;
import com.hotelcaciquebolivar.reservas.entity.pago.PagoDesglose;

/**
 * Mapper MapStruct para la conversion entre la entidad {@link Pago} y sus DTOs correspondientes.
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(PagoRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #toResponse(Pago)} - Entidad a DTO de respuesta con info de la reserva asociada.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface PagoMapper {

    /**
     * Convierte un DTO de solicitud {@link PagoRequest} en una nueva entidad {@link Pago}.
     *
     * <p>Los campos {@code id}, {@code codigoPago}, {@code reserva}, {@code estado}, {@code fechaPago}
     * y {@code createdAt} se ignoran porque se asignan durante la persistencia y la logica de negocio.</p>
     *
     * @param dto el DTO de solicitud con los datos del pago.
     * @return una nueva entidad {@link Pago} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigoPago", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "desglose", ignore = true)
    Pago toEntity(PagoRequest dto);

    /**
     * Convierte una entidad {@link Pago} en un DTO de respuesta {@link PagoResponse}.
     *
     * <p>Extrae el {@code reservaId} y {@code codigoReserva} de la reserva asociada.</p>
     *
     * @param pago la entidad de pago a convertir.
     * @return el DTO de respuesta con los campos mapeados.
     */
    @Mapping(target = "reservaId", source = "reserva.id")
    @Mapping(target = "codigoReserva", source = "reserva.codigoReserva")
    PagoResponse toResponse(Pago pago);

    /**
     * Convierte una línea de desglose en su DTO de respuesta.
     *
     * @param desglose la entidad de desglose a convertir.
     * @return el DTO con el origen, monto, concepto y consumo de la línea.
     */
    @Mapping(target = "consumoId", source = "consumo.id")
    PagoDesgloseResponse toDesgloseResponse(PagoDesglose desglose);
}
