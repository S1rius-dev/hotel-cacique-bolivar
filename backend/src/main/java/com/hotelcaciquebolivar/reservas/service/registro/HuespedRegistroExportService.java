package com.hotelcaciquebolivar.reservas.service.registro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hotelcaciquebolivar.reservas.dto.registro.HuespedRegistroDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.ServletContext;

/**
 * Servicio de exportación de registros de huéspedes a formatos PDF y Word (DOCX).
 * Genera documentos con encabezados formateados, tablas de datos, filtros aplicados,
 * logos del hotel y pies de página. Utiliza Thymeleaf para plantillas HTML (PDF)
 * y Apache POI para generación de documentos Word.
 *
 * @author Hotel Cacique Bolívar
 */
@Service
public class HuespedRegistroExportService {

    private static final Locale ESPANOL = Locale.of("es", "ES");
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter
            .ofPattern("dd 'de' MMMM 'de' yyyy", ESPANOL);
    private static final DateTimeFormatter FECHA_HORA_LARGA = DateTimeFormatter
            .ofPattern("dd 'de' MMMM 'de' yyyy, h:mm a", ESPANOL);
    private static final String ROSE_950 = "4C0519";
    private static final String ROSE_900 = "881337";
    private static final String ROSE_800 = "9F1239";
    private static final String ROSE_700 = "BE123C";
    private static final String ROSE_600 = "E11D48";
    private static final String ROSE_200 = "FECDD3";
    private static final String ROSE_50 = "FFF1F2";
    private static final String COLOR_TEXTO = "1F2937";
    private static final String COLOR_MUTED = "6B7280";
    private static final String COLOR_PIE = "9CA3AF";
    private static final String COLOR_FILA_ALTERNA = "F9FAFB";

    private final HuespedRegistroService registroService;
    private final TemplateEngine templateEngine;
    private final ServletContext servletContext;

    public HuespedRegistroExportService(
            HuespedRegistroService registroService,
            TemplateEngine templateEngine,
            ServletContext servletContext) {
        this.registroService = registroService;
        this.templateEngine = templateEngine;
        this.servletContext = servletContext;
    }

    /**
     * Genera un reporte de registros de huéspedes en formato PDF.
     * Consulta los registros filtrados, renderiza una plantilla Thymeleaf con los datos
     * y convierte el HTML resultante a PDF usando openhtmltopdf.
     *
     * @param fechaDesde     fecha de inicio del filtro de check-in; puede ser null
     * @param fechaHasta     fecha de fin del filtro de check-in; puede ser null
     * @param habitacion     número de habitación para filtrar; puede ser null
     * @param tipoHabitacionId identificador del tipo de habitación para filtrar; puede ser null
     * @param busqueda       texto de búsqueda sobre huéspedes; puede ser null
     * @param estado         estado del detalle de reserva para filtrar; puede ser null
     * @return arreglo de bytes con el contenido del archivo PDF generado
     * @throws IOException si ocurre un error al generar el PDF o cargar el logo
     */
    public byte[] generarPdf(
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) throws IOException {

        List<HuespedRegistroDTO> registros = registroService.consultarTodosParaExport(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        String htmlContent = renderTemplate("huesped-registro-report", registros,
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new PdfRendererBuilder()
                .withHtmlContent(htmlContent, servletContext.getContextPath())
                .toStream(outputStream)
                .run();
        return outputStream.toByteArray();
    }

    /**
     * Genera un reporte de registros de huéspedes en formato Word (DOCX).
     * Consulta los registros filtrados y construye un documento Word con encabezado,
     * tabla de datos, filtros aplicados y pie de página formateados.
     *
     * @param fechaDesde     fecha de inicio del filtro de check-in; puede ser null
     * @param fechaHasta     fecha de fin del filtro de check-in; puede ser null
     * @param habitacion     número de habitación para filtrar; puede ser null
     * @param tipoHabitacionId identificador del tipo de habitación para filtrar; puede ser null
     * @param busqueda       texto de búsqueda sobre huéspedes; puede ser null
     * @param estado         estado del detalle de reserva para filtrar; puede ser null
     * @return arreglo de bytes con el contenido del archivo DOCX generado
     * @throws IOException si ocurre un error al generar el documento
     */
    public byte[] generarWord(
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) throws IOException {

        List<HuespedRegistroDTO> registros = registroService.consultarTodosParaExport(
                fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);

        return generarDocx(registros, fechaDesde, fechaHasta, habitacion, tipoHabitacionId, busqueda, estado);
    }

    /**
     * Renderiza la plantilla Thymeleaf para el reporte de registros.
     * Carga el logo en Base64, formatea los registros y establece el contexto
     * con todos los parámetros de filtrado y metadatos del reporte.
     *
     * @param templateName    nombre de la plantilla Thymeleaf (sin extensión)
     * @param registros       lista de registros de huéspedes a incluir
     * @param fechaDesde      fecha de inicio del filtro
     * @param fechaHasta      fecha de fin del filtro
     * @param habitacion      número de habitación filtrado
     * @param tipoHabitacionId identificador del tipo de habitación filtrado
     * @param busqueda        texto de búsqueda
     * @param estado          estado filtrado
     * @return contenido HTML renderizado
     * @throws IOException si ocurre un error al cargar el logo
     */
    private String renderTemplate(String templateName, List<HuespedRegistroDTO> registros,
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) throws IOException {

        String logoBase64 = cargarLogoBase64();

        List<Map<String, String>> registrosFormateados = new ArrayList<>();
        for (HuespedRegistroDTO reg : registros) {
            Map<String, String> row = new HashMap<>();
            row.put("numeroHabitacion", reg.numeroHabitacion());
            row.put("tipoHabitacion", reg.tipoHabitacion());
            row.put("cedula", reg.cedula());
            row.put("nombreCompleto", reg.nombreCompleto() + " " + reg.apellidoCompleto());
            row.put("codigoReserva", reg.codigoReserva());
            row.put("fechaCheckIn", reg.fechaCheckIn() != null
                    ? reg.fechaCheckIn().format(FECHA_LARGA) : "");
            row.put("fechaCheckOut", reg.fechaCheckOut() != null
                    ? reg.fechaCheckOut().format(FECHA_LARGA) : "");
            row.put("estadoDetalle", reg.estadoDetalle() != null
                    ? reg.estadoDetalle().name() : "");
            registrosFormateados.add(row);
        }

        Context context = new Context();
        context.setVariable("registros", registrosFormateados);
        context.setVariable("logoBase64", logoBase64);
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(FECHA_HORA_LARGA));
        context.setVariable("fechaDesde", fechaDesde);
        context.setVariable("fechaHasta", fechaHasta);
        context.setVariable("habitacion", habitacion);
        context.setVariable("tipoHabitacionId", tipoHabitacionId);
        context.setVariable("busqueda", busqueda);
        context.setVariable("estado", estado);
        context.setVariable("totalRegistros", registros.size());
        context.setVariable("fechaConsulta",
                formatFechaConsulta(fechaDesde, fechaHasta));

        return templateEngine.process("registros/" + templateName, context);
    }

    /**
     * Carga el logo del hotel desde el classpath y lo codifica en Base64.
     *
     * @return cadena Base64 del logo PNG
     * @throws IOException si no se encuentra o no se puede leer el archivo del logo
     */
    private String cargarLogoBase64() throws IOException {
        ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
        try (InputStream is = logoResource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * Genera el documento Word (DOCX) con la estructura completa del reporte.
     * Construye encabezado con logo, resumen, filtros, tabla de datos y pie de página.
     *
     * @param registros       lista de registros de huéspedes
     * @param fechaDesde      fecha de inicio del filtro
     * @param fechaHasta      fecha de fin del filtro
     * @param habitacion      número de habitación filtrado
     * @param tipoHabitacionId identificador del tipo de habitación filtrado
     * @param busqueda        texto de búsqueda
     * @param estado          estado filtrado
     * @return arreglo de bytes con el documento DOCX
     * @throws IOException si ocurre un error al generar el documento
     */
    private byte[] generarDocx(List<HuespedRegistroDTO> registros,
            LocalDate fechaDesde, LocalDate fechaHasta,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) throws IOException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var document = new org.apache.poi.xwpf.usermodel.XWPFDocument();

            construirHeaderWord(document);
            addEmptyParagraph(document);
            construirResumenWord(document, registros.size(),
                    formatFechaConsulta(fechaDesde, fechaHasta));
            addEmptyParagraph(document);

            if (hasFiltros(habitacion, tipoHabitacionId, busqueda, estado)) {
                construirFiltrosWord(document, habitacion, tipoHabitacionId, busqueda, estado);
                addEmptyParagraph(document);
            }

            if (registros.isEmpty()) {
                var emptyPara = document.createParagraph();
                emptyPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
                emptyPara.setSpacingBefore(400);
                emptyPara.setSpacingAfter(400);
                var emptyRun = emptyPara.createRun();
                emptyRun.setFontSize(11);
                emptyRun.setColor(COLOR_PIE);
                emptyRun.setItalic(true);
                emptyRun.setText("No se encontraron registros con los filtros aplicados.");
            } else {
                construirTablaWord(document, registros);
            }

            addEmptyParagraph(document);
            construirFooterWord(document, registros.size());

            document.write(outputStream);
            document.close();
            return outputStream.toByteArray();
        }
    }

    /**
     * Construye el encabezado del documento Word con logo del hotel, nombre y fecha de generación.
     *
     * @param document documento Word en el que se escribe el encabezado
     */
    private void construirHeaderWord(
            org.apache.poi.xwpf.usermodel.XWPFDocument document) {

        XWPFTable headerTable = document.createTable(1, 3);
        headerTable.setWidth("100%");
        XWPFTableRow headerRow = headerTable.getRow(0);

        XWPFTableCell logoCell = headerRow.getCell(0);
        logoCell.setColor("FFFFFF");
        var logoPara = logoCell.getParagraphs().get(0);
        logoPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT);
        try {
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
            try (InputStream logoStream = logoResource.getInputStream()) {
                var logoRun = logoPara.createRun();
                logoRun.addPicture(logoStream,
                        org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG,
                        "logo.png",
                        Units.toEMU(45), Units.toEMU(45));
            }
        } catch (Exception e) {
            var logoRun = logoPara.createRun();
            logoRun.setText("");
        }

        XWPFTableCell titleCell = headerRow.getCell(1);
        titleCell.setColor("FFFFFF");
        titleCell.removeParagraph(0);
        var hotelNamePara = titleCell.addParagraph();
        hotelNamePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        hotelNamePara.setSpacingAfter(0);
        var hotelNameRun = hotelNamePara.createRun();
        hotelNameRun.setBold(true);
        hotelNameRun.setFontSize(8);
        hotelNameRun.setColor(ROSE_800);
        hotelNameRun.setText("HOTEL CACIQUE BOL\u00cdVAR");

        var titlePara = titleCell.addParagraph();
        titlePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        titlePara.setSpacingBefore(0);
        titlePara.setSpacingAfter(0);
        var titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setColor(ROSE_950);
        titleRun.setText("Registro de Hu\u00e9spedes");

        var subtitlePara = titleCell.addParagraph();
        subtitlePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        subtitlePara.setSpacingBefore(0);
        var subtitleRun = subtitlePara.createRun();
        subtitleRun.setFontSize(8);
        subtitleRun.setColor(COLOR_MUTED);
        subtitleRun.setText("HISTORIAL DE ALOJAMIENTOS");

        XWPFTableCell dateCell = headerRow.getCell(2);
        dateCell.setColor("FFFFFF");
        dateCell.removeParagraph(0);
        var dateLabelPara = dateCell.addParagraph();
        dateLabelPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT);
        dateLabelPara.setSpacingAfter(0);
        var dateLabelRun = dateLabelPara.createRun();
        dateLabelRun.setFontSize(6);
        dateLabelRun.setColor(COLOR_MUTED);
        dateLabelRun.setText("DOCUMENTO GENERADO");

        var dateValuePara = dateCell.addParagraph();
        dateValuePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT);
        dateValuePara.setSpacingBefore(0);
        var dateValueRun = dateValuePara.createRun();
        dateValueRun.setBold(true);
        dateValueRun.setFontSize(8);
        dateValueRun.setColor(COLOR_TEXTO);
        dateValueRun.setText(LocalDateTime.now().format(FECHA_HORA_LARGA));
    }

    /**
     * Construye la sección de resumen del documento Word con el total de registros
     * y el período consultado.
     *
     * @param document      documento Word
     * @param totalRegistros total de registros encontrados
     * @param fechaConsulta  texto con el formato del período consultado
     */
    private void construirResumenWord(
            org.apache.poi.xwpf.usermodel.XWPFDocument document,
            int totalRegistros, String fechaConsulta) {

        XWPFTable summaryTable = document.createTable(1, 3);
        summaryTable.setWidth("100%");
        XWPFTableRow row = summaryTable.getRow(0);

        XWPFTableCell totalCell = row.getCell(0);
        totalCell.setColor("FFFFFF");
        totalCell.removeParagraph(0);
        var totalLabel = totalCell.addParagraph();
        totalLabel.setSpacingAfter(0);
        var totalLabelRun = totalLabel.createRun();
        totalLabelRun.setBold(true);
        totalLabelRun.setFontSize(6);
        totalLabelRun.setColor(COLOR_MUTED);
        totalLabelRun.setText("TOTAL DE REGISTROS");
        var totalValue = totalCell.addParagraph();
        totalValue.setSpacingBefore(0);
        var totalValueRun = totalValue.createRun();
        totalValueRun.setBold(true);
        totalValueRun.setFontSize(14);
        totalValueRun.setColor(ROSE_800);
        totalValueRun.setText(String.valueOf(totalRegistros));

        XWPFTableCell periodCell = row.getCell(1);
        periodCell.setColor("FFFFFF");
        periodCell.removeParagraph(0);
        var periodLabel = periodCell.addParagraph();
        periodLabel.setSpacingAfter(0);
        var periodLabelRun = periodLabel.createRun();
        periodLabelRun.setBold(true);
        periodLabelRun.setFontSize(6);
        periodLabelRun.setColor(COLOR_MUTED);
        periodLabelRun.setText("PER\u00cdODO CONSULTADO");
        var periodValue = periodCell.addParagraph();
        periodValue.setSpacingBefore(0);
        var periodValueRun = periodValue.createRun();
        periodValueRun.setBold(true);
        periodValueRun.setFontSize(9);
        periodValueRun.setColor(COLOR_TEXTO);
        periodValueRun.setText(fechaConsulta);

        XWPFTableCell infoCell = row.getCell(2);
        infoCell.setColor("FFFFFF");
        infoCell.removeParagraph(0);
        var infoLabel = infoCell.addParagraph();
        infoLabel.setSpacingAfter(0);
        var infoLabelRun = infoLabel.createRun();
        infoLabelRun.setBold(true);
        infoLabelRun.setFontSize(6);
        infoLabelRun.setColor(COLOR_MUTED);
        infoLabelRun.setText("INFORMACI\u00d3N DEL REPORTE");
        var infoValue = infoCell.addParagraph();
        infoValue.setSpacingBefore(0);
        var infoValueRun = infoValue.createRun();
        infoValueRun.setFontSize(9);
        infoValueRun.setColor(COLOR_TEXTO);
        infoValueRun.setText("Historial de alojamientos");
    }

    /**
     * Construye la sección de filtros aplicados en el documento Word.
     *
     * @param document         documento Word
     * @param habitacion       número de habitación filtrado
     * @param tipoHabitacionId identificador del tipo de habitación filtrado
     * @param busqueda         texto de búsqueda
     * @param estado           estado filtrado
     */
    private void construirFiltrosWord(
            org.apache.poi.xwpf.usermodel.XWPFDocument document,
            String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) {

        var titlePara = document.createParagraph();
        titlePara.setSpacingAfter(40);
        var titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(7);
        titleRun.setColor(ROSE_950);
        titleRun.setText("FILTROS APLICADOS");

        StringBuilder sb = new StringBuilder();
        if (habitacion != null && !habitacion.isBlank()) {
            sb.append("Habitaci\u00f3n: ").append(habitacion).append("  ");
        }
        if (tipoHabitacionId != null) {
            sb.append("Tipo ID: ").append(tipoHabitacionId).append("  ");
        }
        if (busqueda != null && !busqueda.isBlank()) {
            sb.append("B\u00fasqueda: \"").append(busqueda).append("\"  ");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append("Estado: ").append(estado);
        }

        var filterPara = document.createParagraph();
        filterPara.setSpacingAfter(40);
        var filterRun = filterPara.createRun();
        filterRun.setFontSize(8);
        filterRun.setColor(ROSE_950);
        filterRun.setText(sb.toString().trim());
    }

    /**
     * Construye la tabla de datos del documento Word con los registros de huéspedes.
     * Incluye columnas: #, Hab., Tipo, Cédula, Huésped, Reserva, Check-In, Check-Out, Estado.
     * Aplica formato alternado de filas y anchos de columna predefinidos.
     *
     * @param document  documento Word
     * @param registros lista de registros de huéspedes a mostrar en la tabla
     */
    private void construirTablaWord(
            org.apache.poi.xwpf.usermodel.XWPFDocument document,
            List<HuespedRegistroDTO> registros) {

        String[] headers = {
            "#", "Hab.", "Tipo", "C\u00e9dula", "Hu\u00e9sped",
            "Reserva", "Check-In", "Check-Out", "Estado"
        };

        XWPFTable table = document.createTable(1, headers.length);
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            cell.setText(headers[i]);
            cell.setColor(ROSE_900);
            cell.setVerticalAlignment(org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign.CENTER);
            var para = cell.getParagraphs().get(0);
            para.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            para.setSpacingBefore(0);
            para.setSpacingAfter(0);
            var run = para.createRun();
            run.setBold(true);
            run.setFontSize(8);
            run.setColor("FFFFFF");
            run.setFontFamily("Arial");
        }

        for (int idx = 0; idx < registros.size(); idx++) {
            HuespedRegistroDTO reg = registros.get(idx);
            XWPFTableRow dataRow = table.createRow();

            String[] values = {
                String.valueOf(idx + 1),
                reg.numeroHabitacion(),
                reg.tipoHabitacion(),
                reg.cedula(),
                reg.nombreCompleto() + " " + reg.apellidoCompleto(),
                reg.codigoReserva(),
                reg.fechaCheckIn() != null ? reg.fechaCheckIn().format(FECHA_LARGA) : "",
                reg.fechaCheckOut() != null ? reg.fechaCheckOut().format(FECHA_LARGA) : "",
                reg.estadoDetalle() != null ? reg.estadoDetalle().name() : ""
            };

            for (int i = 0; i < values.length; i++) {
                XWPFTableCell cell = dataRow.getCell(i);
                cell.setText(values[i]);
                cell.setVerticalAlignment(org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign.CENTER);
                var para = cell.getParagraphs().get(0);
                para.setSpacingBefore(0);
                para.setSpacingAfter(0);
                var run = para.createRun();
                run.setFontSize(8);
                run.setColor(COLOR_TEXTO);
                run.setFontFamily("Arial");

                if (idx % 2 == 1) {
                    cell.setColor(COLOR_FILA_ALTERNA);
                }
            }

            int[] colWidths = {400, 700, 1200, 1100, 2200, 1300, 1300, 1300, 1000};
            for (int i = 0; i < colWidths.length; i++) {
                XWPFTableCell cell = dataRow.getCell(i);
                if (cell != null) {
                    var tcPr = cell.getCTTc().addNewTcPr();
                    var width = tcPr.addNewTcW();
                    width.setW(colWidths[i]);
                    width.setType(STTblWidth.DXA);
                }
            }
        }
    }

    /**
     * Construye el pie de página del documento Word con el total de registros
     * y la marca del sistema.
     *
     * @param document       documento Word
     * @param totalRegistros total de registros en el reporte
     */
    private void construirFooterWord(
            org.apache.poi.xwpf.usermodel.XWPFDocument document, int totalRegistros) {

        var footerSep = document.createParagraph();
        footerSep.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        var footerSepRun = footerSep.createRun();
        footerSepRun.setText("___________________________________________________________________________");
        footerSepRun.setColor(ROSE_200);
        footerSepRun.setFontSize(8);

        addEmptyParagraph(document);

        var totalPara = document.createParagraph();
        totalPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT);
        var totalLabelRun = totalPara.createRun();
        totalLabelRun.setFontSize(8);
        totalLabelRun.setColor(COLOR_TEXTO);
        totalLabelRun.setText("Total de registros: ");
        var totalValueRun = totalPara.createRun();
        totalValueRun.setBold(true);
        totalValueRun.setFontSize(9);
        totalValueRun.setColor(ROSE_800);
        totalValueRun.setText(String.valueOf(totalRegistros));

        var brandPara = document.createParagraph();
        brandPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT);
        brandPara.setSpacingBefore(0);
        var brandRun = brandPara.createRun();
        brandRun.setFontSize(7);
        brandRun.setColor(COLOR_PIE);
        brandRun.setText("Hotel Cacique Bol\u00edvar");

        var sysPara = document.createParagraph();
        sysPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT);
        sysPara.setSpacingBefore(0);
        var sysRun = sysPara.createRun();
        sysRun.setFontSize(7);
        sysRun.setColor(COLOR_PIE);
        sysRun.setText("Sistema de Gesti\u00f3n de Reservas \u2014 Documento generado autom\u00e1ticamente");
    }

    /**
     * Agrega un párrafo vacío al documento Word como separador visual.
     *
     * @param document documento Word
     */
    private void addEmptyParagraph(org.apache.poi.xwpf.usermodel.XWPFDocument document) {
        document.createParagraph();
    }

    /**
     * Verifica si al menos uno de los filtros proporcionado tiene valor.
     *
     * @param habitacion       número de habitación
     * @param tipoHabitacionId identificador del tipo de habitación
     * @param busqueda         texto de búsqueda
     * @param estado           estado del detalle
     * @return true si al menos un filtro tiene valor no nulo ni vacío
     */
    private boolean hasFiltros(String habitacion, Long tipoHabitacionId,
            String busqueda, String estado) {
        return (habitacion != null && !habitacion.isBlank())
                || tipoHabitacionId != null
                || (busqueda != null && !busqueda.isBlank())
                || (estado != null && !estado.isBlank());
    }

    /**
     * Formatea el período de consulta para mostrar en el reporte.
     * Genera textos como "Del X al Y", "Desde X", "Hasta Y" o "Todas las fechas".
     *
     * @param fechaDesde fecha de inicio
     * @param fechaHasta fecha de fin
     * @return texto con el período formateado en español
     */
    private String formatFechaConsulta(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde != null && fechaHasta != null) {
            return "Del " + fechaDesde.format(FECHA_LARGA) + " al " + fechaHasta.format(FECHA_LARGA);
        }
        if (fechaDesde != null) {
            return "Desde " + fechaDesde.format(FECHA_LARGA);
        }
        if (fechaHasta != null) {
            return "Hasta " + fechaHasta.format(FECHA_LARGA);
        }
        return "Todas las fechas";
    }
}
