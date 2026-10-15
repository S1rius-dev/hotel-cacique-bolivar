package com.hotelcaciquebolivar.reservas.config.initializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.cliente.Municipio;
import com.hotelcaciquebolivar.reservas.repository.cliente.MunicipioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Inicializador de municipios del sistema.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Order#2} para ejecutarse
 * segundo en la cadena de inicializadores. Carga los municipios colombianos
 * desde el archivo CSV {@code data/municipios.csv} ubicado en el classpath.</p>
 *
 * <p>El archivo CSV contiene 1123 registros con código, departamento y nombre
 * del municipio. Se procesa omitiendo la cabecera y capitalizando los nombres
 * adecuadamente. La carga es idempotente: si ya existen municipios en la base
 * de datos, se omite.</p>
 *
 * @see MunicipioRepository
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class MunicipioDataInitializer implements CommandLineRunner {

    private static final Logger log
            = LoggerFactory.getLogger(MunicipioDataInitializer.class);

    private final MunicipioRepository municipioRepository;

    /**
     * Ejecuta la carga de municipios desde el archivo CSV al arrancar.
     *
     * <p>Verifica si ya existen municipios en la base de datos antes de intentar
     * la carga. Lee el archivo CSV línea por línea, parsea cada registro con
     * soporte para campos entre comillas, y guarda los municipios en lote.</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    @Override
    public void run(String... args) {

        log.info("[INIT][MUNICIPIO] Iniciando carga");

        try {

            if (municipioRepository.count() > 0) {
                log.info("[INIT][MUNICIPIO] Municipios ya existen");
                return;
            }

            Resource resource
                    = new ClassPathResource("data/municipios.csv");

            List<Municipio> municipios = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            resource.getInputStream(),
                            StandardCharsets.UTF_8))) {

                br.readLine();

                String line;

                while ((line = br.readLine()) != null) {

                    String[] data = parseCsvLine(line);
                    if (data.length < 3) continue;

                    Municipio municipio = new Municipio();

                    municipio.setCodigo(data[0].trim());

                    municipio.setDepartamento(
                            capitalizar(data[1].trim())
                    );

                    municipio.setNombre(
                            capitalizar(data[2].trim())
                    );

                    municipios.add(municipio);
                }
            }

            municipioRepository.saveAll(municipios);

            log.info("[INIT][MUNICIPIO] Municipios cargados: {}",
                    municipios.size());

        } catch (IOException e) {
            log.error("[INIT][MUNICIPIO] Error cargando municipios", e);
        }
    }

    /**
     * Analiza una línea CSV y extrae los campos, respetando campos entre comillas.
     *
     * <p>Implementa un parser simple de CSV que maneja campos envueltos en comillas
     * dobles. Las comillas dobles dentro de un campo entre comillas se ignoran
     * (no se implementa el escape con comillas duplicadas).</p>
     *
     * @param line línea CSV a parsear
     * @return array de campos extraídos de la línea
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Capitaliza el texto aplicando formato de título a cada palabra.
     *
     * <p>Convierte la primera letra de cada palabra a mayúscula y el resto a
     * minúscula. Las palabras que contienen puntos (ej. "S.A.") se mantienen
     * sin cambios. Las palabras en blanco se omiten.</p>
     *
     * @param texto texto a capitalizar
     * @return texto capitalizado con formato de título, o cadena vacía si es nulo o vacío
     */
    private String capitalizar(String texto) {

        if (texto == null || texto.isBlank()) {
            return "";
        }

        String[] partes = texto.split("\\s+");

        StringBuilder sb = new StringBuilder();

        for (String parte : partes) {

            if (parte.isBlank()) {
                continue;
            }

            if (parte.contains(".")) {
                sb.append(parte).append(" ");
            } else {
                sb.append(parte.substring(0, 1).toUpperCase())
                        .append(parte.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return sb.toString().trim();
    }
}
