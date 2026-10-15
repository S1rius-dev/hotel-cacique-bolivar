package com.hotelcaciquebolivar.reservas.sse.payload;

import java.math.BigDecimal;

/**
 * Payload de datos que se transmite vía SSE cuando se abre o cierra un turno de caja.
 * Contiene la información del turno para que los clientes conectados actualicen
 * los paneles de gestión de caja en tiempo real.
 *
 * @param turnoId     identificador único del turno
 * @param caja        identificador o nombre de la caja asociada al turno
 * @param usuario     identificador del usuario que abre o cierra el turno
 * @param fondoInicial monto de fondo inicial con el que se inicia el turno
 * @param contadoFinal monto contado final al momento del cierre del turno
 * @param estado      estado del turno (abierto, cerrado)
 */
public record TurnoPayload(
    Long turnoId,
    String caja,
    String usuario,
    BigDecimal fondoInicial,
    BigDecimal contadoFinal,
    String estado
) {}
