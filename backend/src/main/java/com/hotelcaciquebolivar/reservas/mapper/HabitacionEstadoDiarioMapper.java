package com.hotelcaciquebolivar.reservas.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hotelcaciquebolivar.reservas.dto.reserva.AlertaInfo;
import com.hotelcaciquebolivar.reservas.dto.reserva.HabitacionEstadoDiario;
import com.hotelcaciquebolivar.reservas.dto.reserva.HuespedResponse;
import com.hotelcaciquebolivar.reservas.dto.reserva.ReservaActiva;
import com.hotelcaciquebolivar.reservas.entity.alerta.Alerta;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReserva;
import com.hotelcaciquebolivar.reservas.entity.reserva.DetalleReservaHuesped;

/**
 * Mapper MapStruct para la construccion de vistas diarias de estado de habitaciones.
 *
 * <p>Proporciona mapeos para generar el estado diario del hotel, incluyendo:</p>
 * <ul>
 *   <li>{@link #toEstadoDiarioDTO(Habitacion, EstadoHabitacion, ReservaActiva)} - Construye el DTO de estado diario de una habitacion.</li>
 *   <li>{@link #toReservaActiva(DetalleReserva, List)} - Convierte un detalle de reserva en una reserva activa con huespedes.</li>
 *   <li>{@link #toHuespedResponse(DetalleReservaHuesped, com.hotelcaciquebolivar.reservas.entity.cliente.Cliente)} - Convierte un huesped de reserva a su DTO de respuesta.</li>
 *   <li>{@link #toAlertaInfoList(List)} - Filtra y convierte alertas activas a {@link AlertaInfo}.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface HabitacionEstadoDiarioMapper {

    /**
     * Construye un DTO {@link HabitacionEstadoDiario} con el estado actual de una habitacion.
     *
     * <p>Mapea datos basicos de la habitacion, su tipo y el estado dinamico proporcionado.
     * Los campos {@code precioNocheEfectivo} y {@code precioPersonaExtraEfectivo} se ignoran porque
     * se calculan externamente.</p>
     *
     * @param h             la entidad de habitacion.
     * @param estadoDinamico el estado dinamico calculado de la habitacion.
     * @param reservaActiva la reserva activa asociada a la habitacion, o {@code null} si no tiene.
     * @return el DTO de estado diario de la habitacion.
     */
    @Mapping(target = "id", source = "h.id")
    @Mapping(target = "numero", source = "h.numero")
    @Mapping(target = "tipoNombre", source = "h.tipo.nombre")
    @Mapping(target = "capacidadBase", source = "h.tipo.capacidadBase")
    @Mapping(target = "capacidadMaxima", source = "h.tipo.capacidadMaxima")
    @Mapping(target = "estado", source = "estadoDinamico")
    @Mapping(target = "precioBase", source = "h.tipo.precioBase")
    @Mapping(target = "precioPersonaExtra", source = "h.tipo.precioPersonaExtra")
    @Mapping(target = "precioNocheEfectivo", ignore = true)
    @Mapping(target = "precioPersonaExtraEfectivo", ignore = true)
    @Mapping(target = "reservaActiva", source = "reservaActiva")
    HabitacionEstadoDiario toEstadoDiarioDTO(Habitacion h, EstadoHabitacion estadoDinamico, ReservaActiva reservaActiva);

    /**
     * Convierte un {@link DetalleReserva} en un DTO {@link ReservaActiva} con informacion de la reserva y sus huespedes.
     *
     * <p>Extrae datos del detalle de reserva, la reserva asociada y el titular de la reserva.
     * Calcula el subtotal, saldo pendiente y total pagado directamente desde la entidad.</p>
     *
     * @param detalle  el detalle de reserva activa.
     * @param huespedes la lista de huespedes formateados como {@link HuespedResponse}.
     * @return el DTO de reserva activa con los campos mapeados.
     */
    @Mapping(target = "reservaId", source = "detalle.reserva.id")
    @Mapping(target = "detalleId", source = "detalle.id")
    @Mapping(target = "codigoReserva", source = "detalle.reserva.codigoReserva")
    @Mapping(target = "titularNombre", expression = "java(detalle.getReserva().getTitularReserva().getNombreCompleto() + \" \" + detalle.getReserva().getTitularReserva().getApellidoCompleto())")
    @Mapping(target = "checkIn", source = "detalle.reserva.fechaCheckIn")
    @Mapping(target = "checkOut", source = "detalle.reserva.fechaCheckOut")
    @Mapping(target = "estadoReserva", source = "detalle.reserva.estado")
    @Mapping(target = "cantidadOcupantes", source = "detalle.cantidadOcupantes")
    @Mapping(target = "precioPorNoche", source = "detalle.precioPorNoche")
    @Mapping(target = "subtotal", expression = "java(detalle.calcularSubtotal())")
    @Mapping(target = "total", source = "detalle.reserva.total")
    @Mapping(target = "totalPagado", expression = "java(detalle.getReserva().getTotalPagado())")
    @Mapping(target = "saldoPendiente", expression = "java(detalle.getReserva().getSaldoPendiente())")
    @Mapping(target = "huespedes", source = "huespedes")
    ReservaActiva toReservaActiva(DetalleReserva detalle, List<HuespedResponse> huespedes);

    /**
     * Convierte un {@link DetalleReservaHuesped} y su {@link com.hotelcaciquebolivar.reservas.entity.cliente.Cliente} asociado
     * en un DTO {@link HuespedResponse}.
     *
     * <p>Mapea los datos personales del huesped y sus alertas activas mediante {@link #toAlertaInfoList(List)}.</p>
     *
     * @param huesped el registro de huesped del detalle de reserva.
     * @param cliente el cliente asociado al huesped.
     * @return el DTO de respuesta del huesped con sus datos y alertas.
     */
    @Mapping(target = "detalleHuespedId", source = "huesped.id")
    @Mapping(target = "huespedId", source = "cliente.id")
    @Mapping(target = "nombreCompleto", source = "cliente.nombreCompleto")
    @Mapping(target = "apellidoCompleto", source = "cliente.apellidoCompleto")
    @Mapping(target = "cedula", source = "cliente.cedula")
    @Mapping(target = "tipoIdentificacion", source = "cliente.tipoIdentificacion.nombre")
    @Mapping(target = "alertas", expression = "java(toAlertaInfoList(cliente.getAlertas()))")
    HuespedResponse toHuespedResponse(DetalleReservaHuesped huesped, com.hotelcaciquebolivar.reservas.entity.cliente.Cliente cliente);

    /**
     * Filtra y convierte una lista de {@link Alerta} a una lista de {@link AlertaInfo},
     * incluyendo solo las alertas con estado activo.
     *
     * @param alertas la lista de alertas del cliente.
     * @return una lista de {@link AlertaInfo} con las alertas activas, o una lista vacia si la entrada es nula.
     */
    default List<AlertaInfo> toAlertaInfoList(List<Alerta> alertas) {
        if (alertas == null) return List.of();
        return alertas.stream()
                .filter(Alerta::getEstado)
                .map(a -> new AlertaInfo(a.getId(), a.getTipo().name(), a.getMensaje(), a.getFechaCreacion(), a.getEstado()))
                .toList();
    }
}
