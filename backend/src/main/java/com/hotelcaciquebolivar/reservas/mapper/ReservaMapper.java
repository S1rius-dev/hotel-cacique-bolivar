package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.*;

import com.hotelcaciquebolivar.reservas.dto.reserva.PricingBreakdown;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaRequest;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaUpdateRequest;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper MapStruct abstracto para la conversion entre la entidad {@link Reserva} y sus DTOs correspondientes.
 *
 * <p>Este mapper es una clase abstracta que inyecta {@link ReservaPricingService} para calcular precios
 * y desgloses de tarifas durante el mapeo. Utiliza los mappers {@link DetalleReservaMapper},
 * {@link AjusteReservaMapper} y {@link PagoMapper} para mapear las colecciones anidadas.</p>
 *
 * <p>Proporciona los siguientes mapeos:</p>
 * <ul>
 *   <li>{@link #toEntity(ReservaRequest)} - DTO de solicitud a nueva entidad.</li>
 *   <li>{@link #updateFromDto(ReservaUpdateRequest, Reserva)} - Actualizacion parcial de una entidad existente.</li>
 *   <li>{@link #toResponse(Reserva)} - Entidad a DTO de respuesta con titular, noches, totales y desglose.</li>
 * </ul>
 */
@Mapper(
        componentModel = "spring",
        uses = {DetalleReservaMapper.class, AjusteReservaMapper.class, PagoMapper.class}
)
public abstract class ReservaMapper {

    @Autowired
    protected ReservaPricingService reservaPricingService;

    /**
     * Convierte un DTO de solicitud {@link ReservaRequest} en una nueva entidad {@link Reserva}.
     *
     * <p>Se ignoran campos como {@code id}, {@code codigoReserva}, {@code titularReserva}, {@code detalles},
     * {@code fechaCreacion}, {@code total}, {@code estado}, {@code updatedAt}, {@code version} y {@code ajustes}
     * porque se asignan durante la persistencia y la logica de negocio.</p>
     *
     * @param dto el DTO de solicitud con los datos de la reserva.
     * @return una nueva entidad {@link Reserva} con los campos mapeados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigoReserva", ignore = true)
    @Mapping(target = "titularReserva", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ajustes", ignore = true)
    public abstract Reserva toEntity(ReservaRequest dto);

    /**
     * Actualiza una entidad {@link Reserva} existente con los valores no nulos de un DTO {@link ReservaUpdateRequest}.
     *
     * <p>Se ignoran campos como {@code id}, {@code codigoReserva}, {@code titularReserva}, {@code detalles},
     * {@code fechaCreacion}, {@code fechaCheckIn}, {@code total}, {@code updatedAt}, {@code version} y {@code ajustes}
     * para preservar su estado actual.</p>
     *
     * @param dto     el DTO con los campos a actualizar.
     * @param reserva la entidad objetivo que sera actualizada.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigoReserva", ignore = true)
    @Mapping(target = "titularReserva", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaCheckIn", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ajustes", ignore = true)
    public abstract void updateFromDto(ReservaUpdateRequest dto, @MappingTarget Reserva reserva);

    /**
     * Convierte una entidad {@link Reserva} en un DTO de respuesta {@link ReservaResponse}.
     *
     * <p>Construye un objeto anidado {@link com.hotelcaciquebolivar.reservas.dto.reserva.TitularInfo} con la
     * informacion del titular de la reserva. Calcula el numero de noches, total pagado, saldo pendiente
     * y el desglose completo de precios mediante {@link ReservaPricingService}.</p>
     *
     * @param reserva la entidad de reserva a convertir.
     * @return el DTO de respuesta con todos los campos calculados y mapeados.
     */
    @Mapping(target = "titular", expression = """
            java(new com.hotelcaciquebolivar.reservas.dto.reserva.TitularInfo(
                reserva.getTitularReserva().getId(),
                reserva.getTitularReserva().getCedula(),
                reserva.getTitularReserva().getTipoIdentificacion().getNombre(),
                reserva.getTitularReserva().getNombreCompleto(),
                reserva.getTitularReserva().getApellidoCompleto(),
                reserva.getTitularReserva().getTelefono()
            ))
            """)
    @Mapping(target = "noches", expression = "java(reserva.calcularNoches())")
    @Mapping(target = "totalPagado", expression = "java(reserva.getTotalPagado())")
    @Mapping(target = "saldoPendiente", expression = "java(reserva.getSaldoPendiente())")
    @Mapping(target = "desglose", expression = "java(reservaPricingService.calcularDesglose(reserva))")
    public abstract ReservaResponse toResponse(Reserva reserva);

    /**
     * Metodo protegido que calcula el desglose de precios de una reserva.
     *
     * @param reserva la entidad de reserva.
     * @return el desglose de precios {@link PricingBreakdown}.
     */
    protected PricingBreakdown mapDesglose(Reserva reserva) {
        return reservaPricingService.calcularDesglose(reserva);
    }
}
