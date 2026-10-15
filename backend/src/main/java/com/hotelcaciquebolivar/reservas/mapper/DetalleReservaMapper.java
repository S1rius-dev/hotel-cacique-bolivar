package com.hotelcaciquebolivar.reservas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.hotelcaciquebolivar.reservas.dto.reserva.DetalleReservaResponse;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.service.reserva.pricing.ReservaPricingService;

/**
 * Mapper MapStruct abstracto para la conversion entre la entidad {@link DetalleReserva} y su DTO de respuesta {@link DetalleReservaResponse}.
 *
 * <p>Este mapper es una clase abstracta que inyecta {@link ReservaPricingService} para calcular precios
 * y desgloses de tarifas durante el mapeo. Realiza transformaciones complejas que incluyen:</p>
 * <ul>
 *   <li>Extraccion de datos de la habitacion asociada (id, numero, tipo, capacidades).</li>
 *   <li>Calculo de personas extras y costos asociados.</li>
 *   <li>Mapeo de huespedes con su informacion personal y alertas activas.</li>
 *   <li>Calculo del subtotal mediante el servicio de precios.</li>
 *   <li>Preservacion de desgloses congelados y snapshots de precios.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public abstract class DetalleReservaMapper {

    @Autowired
    protected ReservaPricingService reservaPricingService;

    /**
     * Convierte una entidad {@link DetalleReserva} en un DTO de respuesta {@link DetalleReservaResponse}.
     *
     * <p>Este metodo realiza mapeos complejos que incluyen expresiones Java para:</p>
     * <ul>
     *   <li>Extraer datos de la habitacion y su tipo (id, numero, nombre, capacidades).</li>
     *   <li>Calcular personas extras y el costo total de personas extras.</li>
     *   <li>Mapear huespedes con su informacion personal, tipo de identificacion y alertas activas.</li>
     *   <li>Calcular el subtotal utilizando {@link ReservaPricingService#calcularTotalDetalle(DetalleReserva)}.</li>
     *   <li>Preservar snapshots de precios y desgloses congelados si existen.</li>
     * </ul>
     *
     * @param detalle la entidad de detalle de reserva a convertir.
     * @return el DTO de respuesta con todos los campos calculados y mapeados.
     */
    @Mapping(target = "habitacionId", expression = "java(detalle.getHabitacion().getId())")
    @Mapping(target = "habitacionNumero", expression = "java(detalle.getHabitacion().getNumero())")
    @Mapping(target = "habitacionTipo", expression = "java(detalle.getHabitacion().getTipo().getNombre())")
    @Mapping(target = "capacidadBase", expression = "java(detalle.getHabitacion().getTipo().getCapacidadBase())")
    @Mapping(target = "capacidadMaxima", expression = "java(detalle.getHabitacion().getTipo().getCapacidadMaxima())")
    @Mapping(target = "precioPersonaExtra", expression = "java(detalle.getPrecioPersonaExtraSnapshot() != null ? detalle.getPrecioPersonaExtraSnapshot() : detalle.getHabitacion().getTipo().getPrecioPersonaExtra())")
    @Mapping(target = "precioPersonaExtraSnapshot", expression = "java(detalle.getPrecioPersonaExtraSnapshot())")
    @Mapping(target = "desgloseCongelado", expression = "java(detalle.getDesgloseCongelado())")
    @Mapping(target = "personasExtras", expression = """
            java(detalle.getCantidadOcupantes() - detalle.getHabitacion().getTipo().getCapacidadBase())
            """)
    @Mapping(target = "precioPersonaExtraTotal", expression = """
            java((detalle.getPrecioPersonaExtraSnapshot() != null ? detalle.getPrecioPersonaExtraSnapshot() : detalle.getHabitacion().getTipo().getPrecioPersonaExtra())
                    .multiply(java.math.BigDecimal.valueOf(
                            Math.max(0, detalle.getCantidadOcupantes() - detalle.getHabitacion().getTipo().getCapacidadBase())))
                    .multiply(java.math.BigDecimal.valueOf(detalle.getReserva().calcularNoches())))
            """)
    @Mapping(target = "subtotal", expression = "java(reservaPricingService.calcularTotalDetalle(detalle))")
    @Mapping(target = "huespedes", expression = """
            java(detalle.getHuespedes().stream()
                    .map(h -> new com.hotelcaciquebolivar.reservas.dto.reserva.HuespedResponse(
                            h.getId(),
                            h.getCliente().getId(),
                            h.getCliente().getNombreCompleto(),
                            h.getCliente().getApellidoCompleto(),
                            h.getCliente().getCedula(),
                            h.getCliente().getTipoIdentificacion().getNombre(),
                            h.getCliente().getAlertas() != null
                                ? h.getCliente().getAlertas().stream()
                                    .filter(a -> a.getEstado())
                                    .map(a -> new com.hotelcaciquebolivar.reservas.dto.reserva.AlertaInfo(
                                        a.getId(), a.getTipo().name(), a.getMensaje(), a.getFechaCreacion(), a.getEstado()))
                                    .toList()
                                : java.util.List.of()))
                    .toList())
            """)
    public abstract DetalleReservaResponse toResponse(DetalleReserva detalle);
}
