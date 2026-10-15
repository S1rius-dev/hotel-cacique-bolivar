package com.hotelcaciquebolivar.reservas.service.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelcaciquebolivar.reservas.entity.cliente.Cliente;
import com.hotelcaciquebolivar.reservas.entity.cliente.Municipio;
import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;
import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.MunicipioRepository;
import com.hotelcaciquebolivar.reservas.repository.cliente.TipoIdentificacionRepository;

/**
 * Servicio de importación masiva de clientes desde un archivo CSV.
 * Lee el archivo de migración de clientes, valida y transforma los datos,
 * y los inserta en la base de datos evitando duplicados.
 * 
 * <p>El archivo CSV debe contener los siguientes campos separados por punto y coma:
 * nombre, tipo de identificación, identificación, código de municipio, ciudad, teléfono, estado.</p>
 */
@Service
@Transactional
public class ClienteImportService {

    private static final Logger log = LoggerFactory.getLogger(ClienteImportService.class);

    private static final Map<String, String> TIPO_ID_MAP = Map.ofEntries(
            Map.entry("Cédula de ciudadanía", "CC"),
            Map.entry("Cédula de extranjería", "CE"),
            Map.entry("Tarjeta de identidad", "TI"),
            Map.entry("Pasaporte", "PAS"),
            Map.entry("Tarjeta de extranjería", "CE"),
            Map.entry("Permiso especial de permanencia PEP", "PPT"),
            Map.entry("Documento de identificación extranjero", "CE"),
            Map.entry("PPT", "PPT"),
            Map.entry("PEP", "PPT"));

    private static final String MUNICIPIO_DEFECTO_CODIGO = "05031"; // Amalfi

    private final ClienteRepository clienteRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final MunicipioRepository municipioRepository;
    private Municipio municipioPorDefecto;

    /**
     * Constructor que inyecta los repositorios necesarios para la importación de clientes.
     *
     * @param clienteRepository repositorio de acceso a datos de clientes
     * @param tipoIdentificacionRepository repositorio de acceso a tipos de identificación
     * @param municipioRepository repositorio de acceso a datos de municipios
     */
    public ClienteImportService(ClienteRepository clienteRepository,
            TipoIdentificacionRepository tipoIdentificacionRepository,
            MunicipioRepository municipioRepository) {
        this.clienteRepository = clienteRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.municipioRepository = municipioRepository;
    }

    /**
     * Importa clientes desde el archivo CSV de migración.
     * Lee el archivo, valida cada fila, evita duplicados y guarda los clientes
     * en la base de datos en lotes.
     *
     * @return resultado de la importación con estadísticas y detalles
     */
    public ImportResult importar() {
        ImportResult result = new ImportResult();

        try {
            ClassPathResource resource = new ClassPathResource("data/migration-client.csv");
            List<String[]> filas = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                br.readLine();

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    filas.add(line.split(";", -1));
                }
            }

            result.total = filas.size();
            log.info("[MIGRACION] Iniciando importación de {} clientes", result.total);

            municipioPorDefecto = municipioRepository.findAll().stream()
                    .filter(m -> MUNICIPIO_DEFECTO_CODIGO.equals(m.getCodigo()))
                    .findFirst()
                    .orElse(null);

            Map<String, TipoIdentificacion> tipoIdCache = new HashMap<>();
            Map<String, Municipio> municipioCache = new HashMap<>();
            cargarCacheMunicipios(municipioCache);

            Set<String> cedulasExistentes = clienteRepository.findAllCedulas();
            log.info("[MIGRACION] Cédulas existentes en BD: {}", cedulasExistentes.size());

            Set<String> cedulasVistas = new HashSet<>();
            List<Cliente> batch = new ArrayList<>();

            for (int i = 0; i < filas.size(); i++) {
                String[] col = filas.get(i);
                int rowNum = i + 2;

                try {
                    Cliente cliente = mapearFila(col, rowNum, tipoIdCache, municipioCache);
                    if (cliente == null) {
                        result.saltados++;
                        continue;
                    }

                    if (cedulasExistentes.contains(cliente.getCedula())) {
                        log.warn("[MIGRACION] Fila {}: cédula {} ya existe en BD, saltando", rowNum, cliente.getCedula());
                        result.saltados++;
                        result.detalles.add("Fila " + rowNum + ": cédula " + cliente.getCedula() + " duplicada en BD");
                        continue;
                    }

                    if (!cedulasVistas.add(cliente.getCedula())) {
                        log.warn("[MIGRACION] Fila {}: cédula {} duplicada en CSV, saltando", rowNum, cliente.getCedula());
                        result.saltados++;
                        result.detalles.add("Fila " + rowNum + ": cédula " + cliente.getCedula() + " duplicada en CSV");
                        continue;
                    }

                    batch.add(cliente);
                    result.importados++;

                } catch (Exception e) {
                    log.error("[MIGRACION] Error en fila {}: {}", rowNum, e.getMessage());
                    result.errores++;
                    result.detalles.add("Fila " + rowNum + ": " + e.getMessage());
                }
            }

            clienteRepository.saveAll(batch);

            log.info("[MIGRACION] Importación finalizada. Total: {} | Importados: {} | Saltados: {} | Errores: {}",
                    result.total, result.importados, result.saltados, result.errores);

        } catch (IOException e) {
            log.error("[MIGRACION] Error leyendo archivo CSV", e);
            result.errores++;
            result.detalles.add("FATAL: " + e.getMessage());
        }

        return result;
    }

    /**
     * Mapea una fila del CSV a una entidad Cliente.
     * Valida campos obligatorios, normaliza nombres y busca referencias.
     *
     * @param col arreglo de columnas de la fila CSV
     * @param rowNum número de fila para logging
     * @param tipoIdCache caché de tipos de identificación
     * @param municipioCache caché de municipios
     * @return cliente mapeado, o null si la fila debe ser saltada
     */
    private Cliente mapearFila(String[] col, int rowNum,
            Map<String, TipoIdentificacion> tipoIdCache,
            Map<String, Municipio> municipioCache) {

        if (col.length < 11) {
            log.warn("[MIGRACION] Fila {}: menos de 11 columnas ({}), saltando", rowNum, col.length);
            return null;
        }

        String nombreTercero = col[0].trim();
        String tipoIdCsv = col[1].trim();
        String identificacion = col[2].trim();
        String ciudad = col[7].trim();
        String telefonoRaw = col[8].trim();
        String estado = col[10].trim();

        if (identificacion.isEmpty()) {
            log.warn("[MIGRACION] Fila {}: identificación vacía, saltando", rowNum);
            return null;
        }

        if (!"Activo".equalsIgnoreCase(estado)) {
            log.info("[MIGRACION] Fila {}: estado '{}' no es Activo, saltando", rowNum, estado);
            return null;
        }

        if (tipoIdCsv.equalsIgnoreCase("NIT")
                || tipoIdCsv.contains("Nit de otro país")
                || tipoIdCsv.contains("NIT")) {
            log.info("[MIGRACION] Fila {}: tipo NIT, saltando", rowNum);
            return null;
        }

        if (nombreTercero.isEmpty()) {
            log.warn("[MIGRACION] Fila {}: nombre vacío, saltando", rowNum);
            return null;
        }

        Cliente cliente = new Cliente();
        cliente.setCedula(truncar(identificacion, 15));

        TipoIdentificacion tipoId = buscarTipoIdentificacion(tipoIdCsv, tipoIdCache);
        if (tipoId == null) {
            tipoId = tipoIdentificacionRepository.findByCodigo("CC")
                    .orElseThrow(() -> new RuntimeException("TipoIdentificacion CC no encontrado"));
            log.warn("[MIGRACION] Fila {}: tipo '{}' no reconocido, default CC", rowNum, tipoIdCsv);
        }
        cliente.setTipoIdentificacion(tipoId);

        String nombreNormalizado = normalizarNombre(nombreTercero);
        String[] nombres = dividirNombreApellido(nombreNormalizado);
        cliente.setNombreCompleto(truncar(nombres[0], 30));
        cliente.setApellidoCompleto(truncar(nombres[1], 30));

        cliente.setTelefono(limpiarTelefono(telefonoRaw));

        Municipio municipio = buscarMunicipio(ciudad, municipioCache);
        if (municipio == null) {
            municipio = municipioPorDefecto;
            if (!ciudad.isEmpty() && !ciudad.equalsIgnoreCase("No aplica")) {
                log.warn("[MIGRACION] Fila {}: ciudad '{}' no encontrada, default Amalfi", rowNum, ciudad);
            }
        }
        cliente.setMunicipioExpedicion(municipio);

        cliente.setObservaciones(null);

        return cliente;
    }

    /**
     * Busca un tipo de identificación por su código CSV usando caché.
     *
     * @param tipoCsv nombre del tipo de identificación en el CSV
     * @param cache caché de tipos de identificación
     * @return tipo de identificación encontrado, o null si no existe
     */
    private TipoIdentificacion buscarTipoIdentificacion(String tipoCsv, Map<String, TipoIdentificacion> cache) {
        if (tipoCsv.isEmpty()) return null;

        String codigo = TIPO_ID_MAP.get(tipoCsv);
        if (codigo == null) return null;

        if (cache.containsKey(codigo)) return cache.get(codigo);

        Optional<TipoIdentificacion> opt = tipoIdentificacionRepository.findByCodigo(codigo);
        opt.ifPresent(t -> cache.put(codigo, t));
        return opt.orElse(null);
    }

    /**
     * Carga todos los municipios en la caché para búsquedas rápidas.
     *
     * @param cache mapa donde se almacenarán los municipios normalizados
     */
    private void cargarCacheMunicipios(Map<String, Municipio> cache) {
        List<Municipio> todos = municipioRepository.findAll();
        for (Municipio m : todos) {
            String key = normalizar(m.getNombre());
            if (!cache.containsKey(key)) {
                cache.put(key, m);
            }
        }
        log.info("[MIGRACION] Cache de municipios cargada: {} entradas", cache.size());
    }

    /**
     * Busca un municipio por su nombre en la caché.
     * Realiza búsqueda exacta y parcial (contiene, inicia con).
     *
     * @param ciudadCsv nombre del municipio en el CSV
     * @param cache caché de municipios
     * @return municipio encontrado, o null si no existe
     */
    private Municipio buscarMunicipio(String ciudadCsv, Map<String, Municipio> cache) {
        if (ciudadCsv == null || ciudadCsv.isBlank()
                || ciudadCsv.equalsIgnoreCase("No aplica")) return null;

        String normalizado = normalizar(ciudadCsv);

        Municipio m = cache.get(normalizado);
        if (m != null) return m;

        for (Map.Entry<String, Municipio> entry : cache.entrySet()) {
            String key = entry.getKey();
            if (key.contains(normalizado) || normalizado.contains(key)) {
                return entry.getValue();
            }
            if (key.startsWith(normalizado) || normalizado.startsWith(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Normaliza un texto eliminando tildes, caracteres especiales y convirtiendo a mayúsculas.
     *
     * @param texto texto a normalizar
     * @return texto normalizado
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        String result = Normalizer.normalize(texto, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replaceAll("[^a-zA-Z0-9\\s,]", "");
        return result.toUpperCase().trim();
    }

    /**
     * Normaliza un nombre资本izando la primera letra de cada palabra.
     * Preserva las palabras que contienen puntos (abreviaturas).
     *
     * @param texto nombre a normalizar
     * @return nombre normalizado con capitalización adecuada
     */
    private String normalizarNombre(String texto) {
        if (texto == null || texto.isBlank()) return "";
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String palabra : palabras) {
            if (sb.length() > 0) sb.append(" ");
            if (palabra.contains(".")) {
                sb.append(palabra);
            } else {
                sb.append(Character.toUpperCase(palabra.charAt(0)))
                  .append(palabra.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Divide un nombre completo en nombre y apellido.
     * Asume que las últimas dos palabras son el apellido y el resto es el nombre.
     *
     * @param nombreCompleto nombre completo a dividir
     * @return arreglo con [nombre, apellido]
     */
    static String[] dividirNombreApellido(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return new String[]{"", ""};
        }

        String[] palabras = nombreCompleto.trim().split("\\s+");

        if (palabras.length == 1) {
            return new String[]{palabras[0], ""};
        }

        if (palabras.length == 2) {
            return new String[]{palabras[0], palabras[1]};
        }

        StringBuilder nombre = new StringBuilder();
        for (int i = 0; i < palabras.length - 2; i++) {
            if (nombre.length() > 0) nombre.append(" ");
            nombre.append(palabras[i]);
        }

        String apellido = palabras[palabras.length - 2] + " " + palabras[palabras.length - 1];

        return new String[]{nombre.toString(), apellido};
    }

    /**
     * Limpia y formatea un número de teléfono.
     * Elimina caracteres no numéricos y trunca a 10 dígitos.
     *
     * @param raw teléfono en formato crudo del CSV
     * @return teléfono limpio de 10 dígitos, o null si es inválido
     */
    static String limpiarTelefono(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String[] partes = raw.split("-");
        if (partes.length < 2) return truncar(raw.replaceAll("[^0-9]", ""), 10);

        String numero = partes[1].trim();
        numero = numero.replaceAll("[^0-9]", "");

        if (numero.isEmpty() || numero.replaceAll("0", "").isEmpty()) return null;

        return truncar(numero, 10);
    }

    /**
     * Trunca un valor de texto a la longitud máxima especificada.
     *
     * @param valor valor a truncar
     * @param max longitud máxima permitida
     * @return valor truncado, o null si el valor es null
     */
    private static String truncar(String valor, int max) {
        if (valor == null) return null;
        return valor.length() <= max ? valor : valor.substring(0, max);
    }

    /**
     * Clase que almacena el resultado de una operación de importación de clientes.
     * Contiene estadísticas de la importación y detalles de los registros procesados.
     */
    public static class ImportResult {
        private int total;
        private int importados;
        private int saltados;
        private int errores;
        private List<String> detalles = new ArrayList<>();

        public int getTotal() { return total; }
        public int getImportados() { return importados; }
        public int getSaltados() { return saltados; }
        public int getErrores() { return errores; }
        public List<String> getDetalles() { return detalles; }

        public void setTotal(int total) { this.total = total; }
        public void setImportados(int importados) { this.importados = importados; }
        public void setSaltados(int saltados) { this.saltados = saltados; }
        public void setErrores(int errores) { this.errores = errores; }
        public void setDetalles(List<String> detalles) { this.detalles = detalles; }
    }
}
