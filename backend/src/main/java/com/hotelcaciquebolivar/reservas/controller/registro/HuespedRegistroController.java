package com.hotelcaciquebolivar.reservas.controller.registro;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.registro.HuespedRegistroDTO;
import com.hotelcaciquebolivar.reservas.dto.shared.PageResponse;
import com.hotelcaciquebolivar.reservas.service.registro.HuespedRegistroExportService;
import com.hotelcaciquebolivar.reservas.service.registro.HuespedRegistroService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la consulta y exportación de registros de huéspedes.
 * Permite consultar registros con múltiples filtros y exportarlos en formato PDF y Word.
 */
@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
public class HuespedRegistroController {

    private final HuespedRegistroService registroService;
    private final HuespedRegistroExportService exportService;

    /**
     * Consulta registros de huéspedes con filtros opcionales y paginación.
     *
     * @param fechaDesde      fecha de inicio del rango de búsqueda (opcional)
     * @param fechaHasta      fecha de fin del rango de búsqueda (opcional)
     * @param habitacion      filtro por número o nombre de habitación (opcional)
     * @param tipoHabitacionId filtro por tipo de habitación (opcional)
     * @param busqueda        término de búsqueda general (opcional)
     * @param estado          filtro por estado del registro (opcional)
     * @param pageable        parámetros de paginación (tamaño por defecto: 15, ordenado por id descendente)
     * @return página de registros de huéspedes
     */
    @GetMapping
    public ResponseEntity<PageResponse<HuespedRegistroDTO>> consultarRegistros(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String habitacion,
            @RequestParam(required = false) Long tipoHabitacionId,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<HuespedRegistroDTO> result = registroService.consultarRegistros(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Exporta los registros de huéspedes a formato PDF.
     * Los registros se filtran según los parámetros proporcionados.
     *
     * @param fechaDesde      fecha de inicio del rango de búsqueda (opcional)
     * @param fechaHasta      fecha de fin del rango de búsqueda (opcional)
     * @param habitacion      filtro por número o nombre de habitación (opcional)
     * @param tipoHabitacionId filtro por tipo de habitación (opcional)
     * @param busqueda        término de búsqueda general (opcional)
     * @param estado          filtro por estado del registro (opcional)
     * @return archivo PDF con los registros
     * @throws IOException si ocurre un error al generar el archivo
     */
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String habitacion,
            @RequestParam(required = false) Long tipoHabitacionId,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) throws IOException {

        byte[] pdfBytes = exportService.generarPdf(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro-huespedes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    /**
     * Exporta los registros de huéspedes a formato Word (DOCX).
     * Los registros se filtran según los parámetros proporcionados.
     *
     * @param fechaDesde      fecha de inicio del rango de búsqueda (opcional)
     * @param fechaHasta      fecha de fin del rango de búsqueda (opcional)
     * @param habitacion      filtro por número o nombre de habitación (opcional)
     * @param tipoHabitacionId filtro por tipo de habitación (opcional)
     * @param busqueda        término de búsqueda general (opcional)
     * @param estado          filtro por estado del registro (opcional)
     * @return archivo DOCX con los registros
     * @throws IOException si ocurre un error al generar el archivo
     */
    @GetMapping("/exportar/word")
    public ResponseEntity<byte[]> exportarWord(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String habitacion,
            @RequestParam(required = false) Long tipoHabitacionId,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) throws IOException {

        byte[] wordBytes = exportService.generarWord(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro-huespedes.docx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .contentLength(wordBytes.length)
                .body(wordBytes);
    }
}
