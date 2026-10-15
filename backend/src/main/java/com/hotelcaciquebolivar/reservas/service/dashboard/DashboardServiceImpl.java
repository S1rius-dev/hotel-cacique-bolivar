package com.hotelcaciquebolivar.reservas.service.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.dto.dashboard.DashboardResumen;
import com.hotelcaciquebolivar.reservas.dto.dashboard.IngresoDiario;
import com.hotelcaciquebolivar.reservas.dto.dashboard.IngresosResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.OcupacionDiaria;
import com.hotelcaciquebolivar.reservas.dto.dashboard.OcupacionResponse;
import com.hotelcaciquebolivar.reservas.dto.dashboard.UltimaReserva;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.reserva.Reserva;
import com.hotelcaciquebolivar.reservas.exception.validation.ValidationException;
import com.hotelcaciquebolivar.reservas.repository.audit.AuditLogRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.pago.PagoRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaHuespedRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.DetalleReservaRepository;
import com.hotelcaciquebolivar.reservas.repository.reserva.ReservaRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de dashboard del sistema hotelero.
 * Proporciona resúmenes ejecutivos, métricas de ingresos por período,
 * datos de ocupación histórica y proyectada, y las últimas reservas
 * registradas para monitoreo en tiempo real.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HabitacionRepository habitacionRepository;
    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleReservaRepository;
    private final DetalleReservaHuespedRepository detalleReservaHuespedRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Obtiene un resumen ejecutivo del estado actual del hotel.
     * Calcula habitaciones ocupadas, total de habitaciones, reservas activas,
     * ingresos del día y huéspedes activos.
     *
     * @return objeto de respuesta con el resumen del dashboard
     */
    @Override
    public DashboardResumen obtenerResumen() {
        int habitacionesOcupadas = (int) detalleReservaRepository.countHabitacionesOcupadasEnFecha(LocalDate.now());
        int totalHabitaciones = (int) habitacionRepository.count();
        int reservasActivas = (int) reservaRepository.countByEstadoIn(List.of(
                com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva.PENDIENTE,
                com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva.PENDIENTE_PAGO,
                com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva.CONFIRMADA,
                com.hotelcaciquebolivar.reservas.entity.reserva.EstadoReserva.EN_CURSO));

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = hoyInicio.plusDays(1);
        BigDecimal ingresosHoy = pagoRepository.sumByFechaPagoBetween(hoyInicio, hoyFin);

        int huespedesHoy = (int) detalleReservaHuespedRepository.countHuespedesActivos();

        return new DashboardResumen(
                habitacionesOcupadas,
                totalHabitaciones,
                reservasActivas,
                ingresosHoy.longValue(),
                huespedesHoy);
    }

    /**
     * Obtiene los datos de ingresos del hotel para un rango de tiempo específico.
     * Soporta rangos: HOY (por hora), SEMANA (últimos 7 días), MES (últimos 30 días).
     *
     * @param rango rango de tiempo para el que se desean los datos de ingresos
     * @return objeto de respuesta con los datos de ingresos por período
     * @throws ValidationException si el rango no es válido
     */
    @Override
    public IngresosResponse obtenerIngresos(String rango) {
        LocalDateTime inicio;
        LocalDateTime fin = LocalDate.now().plusDays(1).atStartOfDay();
        List<IngresoDiario> data;

        switch (rango.toUpperCase()) {
            case "HOY" -> {
                inicio = LocalDate.now().atStartOfDay();
                data = generarIngresosPorHora(inicio, fin);
            }
            case "SEMANA" -> {
                inicio = LocalDate.now().minusDays(6).atStartOfDay();
                data = generarIngresosPorDia(inicio, fin);
            }
            case "MES" -> {
                inicio = LocalDate.now().minusDays(29).atStartOfDay();
                data = generarIngresosPorDia(inicio, fin);
            }
            default ->
                throw new ValidationException("rango", "rango invalido: debe ser HOY, SEMANA o MES");
        }

        return new IngresosResponse(rango.toUpperCase(), data);
    }

    /**
     * Genera los datos de ingresos agrupados por hora para un período específico.
     * Retorna 24 registros (una por hora del día) con los ingresos acumulados.
     *
     * @param inicio fecha y hora de inicio del período
     * @param fin fecha y hora de fin del período
     * @return lista de ingresos diarios por hora
     */
    private List<IngresoDiario> generarIngresosPorHora(LocalDateTime inicio, LocalDateTime fin) {
        Map<Integer, Long> hourlyMap = new HashMap<>();
        for (var row : pagoRepository.sumGroupedByHour(inicio, fin)) {
            int hour = ((Number) row[0]).intValue();
            long total = ((BigDecimal) row[1]).longValue();
            hourlyMap.put(hour, total);
        }

        List<IngresoDiario> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            String fecha = String.format("%sT%02d:00:00", LocalDate.now().toString(), h);
            result.add(new IngresoDiario(fecha, hourlyMap.getOrDefault(h, 0L)));
        }
        return result;
    }

    /**
     * Genera los datos de ingresos agrupados por día para un período específico.
     * Retorna un registro por día en el rango especificado.
     *
     * @param inicio fecha y hora de inicio del período
     * @param fin fecha y hora de fin del período
     * @return lista de ingresos diarios
     */
    private List<IngresoDiario> generarIngresosPorDia(LocalDateTime inicio, LocalDateTime fin) {
        Map<LocalDate, Long> dailyMap = new HashMap<>();
        for (var row : pagoRepository.sumGroupedByDay(inicio, fin)) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            long total = ((BigDecimal) row[1]).longValue();
            dailyMap.put(date, total);
        }

        List<IngresoDiario> result = new ArrayList<>();
        LocalDate current = inicio.toLocalDate();
        LocalDate end = fin.toLocalDate();
        while (current.isBefore(end)) {
            result.add(new IngresoDiario(current.format(DATE_FMT), dailyMap.getOrDefault(current, 0L)));
            current = current.plusDays(1);
        }
        return result;
    }

    /**
     * Obtiene los datos de ocupación del hotel para un rango de tiempo específico.
     * Soporta rangos: HOY (por hora), SEMANA (últimos 7 días), MES (últimos 30 días).
     *
     * @param rango rango de tiempo para el que se desean los datos de ocupación
     * @return objeto de respuesta con los datos de ocupación por período
     * @throws ValidationException si el rango no es válido
     */
    @Override
    public OcupacionResponse obtenerOcupacion(String rango) {
        int totalHabitaciones = (int) habitacionRepository.count();
        List<OcupacionDiaria> data;

        switch (rango.toUpperCase()) {
            case "HOY" -> {
                int ocupadas = (int) detalleReservaRepository.countOcupadasEnFecha(LocalDate.now());
                data = generarOcupacionPorHora(ocupadas, totalHabitaciones);
            }
            case "SEMANA" -> {
                data = generarOcupacionPorDia(7, totalHabitaciones);
            }
            case "MES" -> {
                data = generarOcupacionPorDia(30, totalHabitaciones);
            }
            default ->
                throw new ValidationException("rango", "rango invalido: debe ser HOY, SEMANA o MES");
        }

        return new OcupacionResponse(rango.toUpperCase(), data);
    }

    /**
     * Genera los datos de ocupación por hora para el día actual.
     * Utiliza el porcentaje de ocupación actual para todas las horas del día.
     *
     * @param ocupadas cantidad de habitaciones ocupadas
     * @param total cantidad total de habitaciones
     * @return lista de ocupación diaria por hora
     */
    private List<OcupacionDiaria> generarOcupacionPorHora(int ocupadas, int total) {
        List<OcupacionDiaria> data = new ArrayList<>();
        double porcentaje = total > 0 ? (ocupadas * 100.0 / total) : 0.0;
        for (int h = 0; h < 24; h++) {
            String fecha = String.format("%sT%02d:00:00", LocalDate.now().toString(), h);
            data.add(new OcupacionDiaria(fecha, porcentaje, ocupadas, total));
        }
        return data;
    }

    /**
     * Genera los datos de ocupación por día para un período específico.
     * Para días pasados utiliza datos históricos, para el día actual usa datos en tiempo real.
     *
     * @param dias cantidad de días hacia atrás a generar datos
     * @param total cantidad total de habitaciones
     * @return lista de ocupación diaria
     */
    private List<OcupacionDiaria> generarOcupacionPorDia(int dias, int total) {
        List<OcupacionDiaria> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate fecha = today.minusDays(i);
            int ocupadas;
            if (fecha.isBefore(today)) {
                ocupadas = (int) detalleReservaRepository.countOcupadasHistoricasEnFecha(fecha);
            } else {
                ocupadas = (int) detalleReservaRepository.countOcupadasEnFecha(fecha);
            }
            double porcentaje = total > 0 ? (ocupadas * 100.0 / total) : 0.0;
            data.add(new OcupacionDiaria(fecha.format(DATE_FMT), porcentaje, ocupadas, total));
        }
        return data;
    }

    /**
     * Obtiene las últimas reservas registradas en el sistema, combinando reservas
     * nuevas (por fecha de crección) y check-ins recientes (por eventos de auditoría).
     * Retorna los resultados ordenados por fecha descendente.
     *
     * @param limite cantidad máxima de reservas a retornar
     * @return lista de las últimas reservas con información resumida
     */
    @Override
    public List<UltimaReserva> obtenerUltimasReservas(int limite) {
        List<UltimaReserva> items = new ArrayList<>();

        List<Reserva> ultimasReservas = reservaRepository.findTopNByOrderByFechaCreacionDesc(
                PageRequest.of(0, limite));
        for (Reserva r : ultimasReservas) {
            items.add(mapearUltimaReserva(r, "CREACION", r.getFechaCreacion()));
        }

        var auditLogs = auditLogRepository.findByActionAndEntityOrderByFechaDesc(
                AuditAction.CHECK_IN, "Reserva", PageRequest.of(0, limite));
        if (!auditLogs.isEmpty()) {
            List<Long> ids = auditLogs.stream().map(log -> log.getEntityId()).toList();
            Map<Long, Reserva> reservasMap = reservaRepository.findAllById(ids)
                    .stream().collect(Collectors.toMap(Reserva::getId, r -> r));
            for (var log : auditLogs) {
                Reserva r = reservasMap.get(log.getEntityId());
                if (r != null) {
                    items.add(mapearUltimaReserva(r, "CHECK_IN", log.getFecha()));
                }
            }
        }

        items.sort(Comparator.comparing(UltimaReserva::fecha).reversed());
        return items.stream().limit(limite).collect(Collectors.toList());
    }

    /**
     * Mapea una entidad Reserva a un DTO UltimaReserva con la información resumida.
     * Extrae el nombre del titular, número de habitación y tipo de habitación.
     *
     * @param r reserva a mapear
     * @param tipo tipo de evento (CREACION o CHECK_IN)
     * @param fecha fecha del evento
     * @return DTO UltimaReserva con los datos resumidos
     */
    private UltimaReserva mapearUltimaReserva(Reserva r, String tipo, LocalDateTime fecha) {
        String titular = r.getTitularReserva() != null
                ? r.getTitularReserva().getNombreCompleto() + " " + r.getTitularReserva().getApellidoCompleto()
                : "N/A";
        String habNumero = "N/A";
        String tipoHab = "N/A";
        if (r.getDetalles() != null && !r.getDetalles().isEmpty()) {
            var detalle = r.getDetalles().get(0);
            if (detalle.getHabitacion() != null) {
                habNumero = detalle.getHabitacion().getNumero();
                if (detalle.getHabitacion().getTipo() != null) {
                    tipoHab = detalle.getHabitacion().getTipo().getNombre();
                }
            }
        }
        return new UltimaReserva(
                r.getId(),
                r.getCodigoReserva(),
                titular,
                habNumero,
                tipoHab,
                tipo,
                fecha.format(DATETIME_FMT));
    }
}
