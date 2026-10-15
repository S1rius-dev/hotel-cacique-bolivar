package com.hotelcaciquebolivar.reservas.service.registro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelcaciquebolivar.reservas.dto.registro.HuespedRegistroDTO;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;

/**
 * Interfaz del servicio de consulta de registros de huéspedes.
 * Permite consultar el historial de alojamientos con filtros por fechas,
 * habitación, tipo de habitación, búsqueda de huésped y estado del detalle.
 *
 * @author Hotel Cacique Bolívar
 */
public interface HuespedRegistroService {

    /**
     * Consulta registros de huéspedes con filtros opcionales de forma paginada.
     * Utiliza Specification de Spring Data para construir dinámicamente los filtros.
     *
     * @param fechaDesde      fecha de inicio del filtro de check-in; puede ser null
     * @param fechaHasta      fecha de fin del filtro de check-in; puede ser null
     * @param habitacion      número de habitación para filtrar; puede ser null o vacío
     * @param tipoHabitacionId identificador del tipo de habitación; puede ser null
     * @param busqueda        texto de búsqueda sobre datos del huésped; puede ser null o vacío
     * @param estado          estado del detalle de reserva; puede ser null o vacío
     * @param pageable        configuración de paginación
     * @return página con los registros de huéspedes encontrados
     */
    PageResponse<HuespedRegistroDTO> consultarRegistros(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String habitacion,
            Long tipoHabitacionId,
            String busqueda,
            String estado,
            Pageable pageable);

    /**
     * Consulta todos los registros de huéspedes que coincidan con los filtros especificados,
     * sin paginación. Utilizado para exportación a PDF y Word.
     *
     * @param fechaDesde      fecha de inicio del filtro de check-in; puede ser null
     * @param fechaHasta      fecha de fin del filtro de check-in; puede ser null
     * @param habitacion      número de habitación para filtrar; puede ser null o vacío
     * @param tipoHabitacionId identificador del tipo de habitación; puede ser null
     * @param busqueda        texto de búsqueda sobre datos del huésped; puede ser null o vacío
     * @param estado          estado del detalle de reserva; puede ser null o vacío
     * @return lista con todos los registros que coincidan con los filtros
     */
    List<HuespedRegistroDTO> consultarTodosParaExport(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String habitacion,
            Long tipoHabitacionId,
            String busqueda,
            String estado);
}
