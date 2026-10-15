package com.hotelcaciquebolivar.reservas.sse;

/**
 * Clase utilitaria que define las constantes de tipo de evento SSE utilizadas
 * en el sistema de notificaciones en tiempo real del hotel.
 *
 * <p>Cada constante corresponde a un evento de dominio que puede ocurrir en el
 * sistema y que se notifica a los clientes conectados vía Server-Sent Events.
 * Los eventos están organizados por dominio:</p>
 * <ul>
 *   <li><b>Reservas:</b> creación, actualización, cancelación, extensión, detalle agregado/eliminado, vencimiento</li>
 *   <li><b>Check-in/Check-out:</b> check-in manual, check-out manual, check-out automático, no-show automático</li>
 *   <li><b>Pagos:</b> registro y reembolso</li>
 *   <li><b>Ajustes:</b> agregación de ajustes a reservas</li>
 *   <li><b>Alertas:</b> creación de alertas para clientes</li>
 *   <li><b>Habitaciones:</b> cambio de estado</li>
 *   <li><b>Turnos:</b> apertura y cierre de turno de caja</li>
 *   <li><b>Gastos:</b> registro de gastos operativos</li>
 *   <li><b>Clientes:</b> creación de nuevos clientes</li>
 *   <li><b>PQRS:</b> creación, cambio de estado y respuesta a peticiones</li>
 * </ul>
 */
public final class SseEventTypes {

    private SseEventTypes() {}

    public static final String RESERVA_CREADA = "RESERVA_CREADA";
    public static final String RESERVA_ACTUALIZADA = "RESERVA_ACTUALIZADA";
    public static final String RESERVA_CANCELADA = "RESERVA_CANCELADA";
    public static final String RESERVA_EXTENDIDA = "RESERVA_EXTENDIDA";
    public static final String RESERVA_DETALLE_AGREGADO = "RESERVA_DETALLE_AGREGADO";
    public static final String RESERVA_DETALLE_ELIMINADO = "RESERVA_DETALLE_ELIMINADO";
    public static final String CHECK_IN = "CHECK_IN";
    public static final String CHECK_OUT = "CHECK_OUT";
    public static final String PAGO_REGISTRADO = "PAGO_REGISTRADO";
    public static final String PAGO_REEMBOLSADO = "PAGO_REEMBOLSADO";
    public static final String AJUSTE_AGREGADO = "AJUSTE_AGREGADO";
    public static final String ALERTA_CREADA = "ALERTA_CREADA";
    public static final String HABITACION_ESTADO_CAMBIADO = "HABITACION_ESTADO_CAMBIADO";
    public static final String TURNO_ABIERTO = "TURNO_ABIERTO";
    public static final String TURNO_CERRADO = "TURNO_CERRADO";
    public static final String GASTO_REGISTRADO = "GASTO_REGISTRADO";
    public static final String CLIENTE_CREADO = "CLIENTE_CREADO";
    public static final String RESERVA_VENCIDA = "RESERVA_VENCIDA";
    public static final String NO_SHOW_AUTO = "NO_SHOW_AUTO";
    public static final String CHECK_OUT_AUTO = "CHECK_OUT_AUTO";

    public static final String PQRS_CREADA = "PQRS_CREADA";
    public static final String PQRS_ESTADO_CAMBIADO = "PQRS_ESTADO_CAMBIADO";
    public static final String PQRS_RESPONDIDA = "PQRS_RESPONDIDA";
}
