// =============================================
// TipoIdentificacionDataInitializer.java
// =============================================
package com.hotelcaciquebolivar.reservas.config.initializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.cliente.TipoIdentificacion;
import com.hotelcaciquebolivar.reservas.repository.cliente.TipoIdentificacionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Inicializador de tipos de identificación del sistema.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Order#3} para ejecutarse
 * después del inicializador de municipios. Crea los tipos de documento de
 * identificación válidos en Colombia si la tabla está vacía.</p>
 *
 * <p>Tipos creados:</p>
 * <ul>
 *   <li>CC — Cédula de ciudadanía</li>
 *   <li>TI — Tarjeta de identidad</li>
 *   <li>CE — Cédula de extranjería</li>
 *   <li>PAS — Pasaporte</li>
 *   <li>PPT — Permiso por Protección Temporal</li>
 *   <li>NIT — Número de Identificación Tributaria</li>
 *   <li>RC — Registro Civil</li>
 * </ul>
 *
 * @see TipoIdentificacionRepository
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class TipoIdentificacionDataInitializer
        implements CommandLineRunner {

    private static final Logger log
            = LoggerFactory.getLogger(
                    TipoIdentificacionDataInitializer.class);

    private final TipoIdentificacionRepository repository;

    /**
     * Ejecuta la carga de tipos de identificación al arrancar la aplicación.
     *
     * <p>Verifica si ya existen tipos en la base de datos antes de intentar
     * la carga. Crea siete tipos de identificación estándar colombianos con
     * sus códigos y nombres completos. Todos se crean con estado activo.</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    @Override
    public void run(String... args) {

        log.info("[INIT][TIPO_ID] Iniciando carga");

        try {

            if (repository.count() > 0) {
                log.info("[INIT][TIPO_ID] Tipos ya existen");
                return;
            }

            List<TipoIdentificacion> tipos = List.of(
                    crear("CC", "Cédula de ciudadanía"),
                    crear("TI", "Tarjeta de identidad"),
                    crear("CE", "Cédula de extranjería"),
                    crear("PAS", "Pasaporte"),
                    crear("PPT", "Permiso por Protección Temporal"),
                    crear("NIT", "Número de Identificación Tributaria"),
                    crear("RC", "Registro Civil")
            );

            repository.saveAll(tipos);

            log.info("[INIT][TIPO_ID] Tipos cargados: {}",
                    tipos.size());

        } catch (Exception e) {
            log.error("[INIT][TIPO_ID] Error cargando tipos", e);
        }
    }

    /**
     * Crea un tipo de identificación con el código y nombre especificados.
     *
     * @param codigo código corto del tipo (ej. "CC", "TI", "CE")
     * @param nombre nombre completo del tipo de identificación
     * @return instancia de {@link TipoIdentificacion} con estado activo
     */
    private TipoIdentificacion crear(
            String codigo,
            String nombre
    ) {
        TipoIdentificacion tipo = new TipoIdentificacion();

        tipo.setCodigo(codigo);
        tipo.setNombre(nombre);
        tipo.setActivo(true);

        return tipo;
    }
}
