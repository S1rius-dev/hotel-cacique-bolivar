package com.hotelcaciquebolivar.reservas.service.dashboard;

import java.util.List;

import com.hotelcaciquebolivar.reservas.dto.dashboard.DashboardResumen;
import com.hotelcaciquebolivar.reservas.dto.dashboard.IngresosResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.OcupacionResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.UltimaReserva;

/**
 * Interfaz que define las operaciones de consulta del panel de control (dashboard) del sistema hotelero.
 * Proporciona resúmenes ejecutivos, métricas de ingresos, datos de ocupación y
 * las últimas reservas registradas para monitoreo en tiempo real.
 */
public interface DashboardService {

    /**
     * Obtiene un resumen ejecutivo del estado actual del hotel.
     * Incluye estadísticas generales como habitaciones disponibles, reservas activas, etc.
     *
     * @return objeto de respuesta con el resumen del dashboard
     */
    DashboardResumen obtenerResumen();

    /**
     * Obtiene los datos de ingresos del hotel para un rango de tiempo específico.
     *
     * @param rango rango de tiempo para el que se desean los datos de ingresos
     *               (por ejemplo: "hoy", "semana", "mes")
     * @return objeto de respuesta con los datos de ingresos
     */
    IngresosResponse obtenerIngresos(String rango);

    /**
     * Obtiene los datos de ocupación del hotel para un rango de tiempo específico.
     *
     * @param rango rango de tiempo para el que se desean los datos de ocupación
     *               (por ejemplo: "hoy", "semana", "mes")
     * @return objeto de respuesta con los datos de ocupación
     */
    OcupacionResponse obtenerOcupacion(String rango);

    /**
     * Obtiene las últimas reservas registradas en el sistema.
     *
     * @param limite cantidad máxima de reservas a retornar
     * @return lista de las últimas reservas con información resumida
     */
    List<UltimaReserva> obtenerUltimasReservas(int limite);
}
