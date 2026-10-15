// =============================================
// HabitacionDataInitializer.java
// =============================================
package com.hotelcaciquebolivar.reservas.config.initializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.habitacion.EstadoHabitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.Habitacion;
import com.hotelcaciquebolivar.reservas.entity.habitacion.TipoHabitacion;
import com.hotelcaciquebolivar.reservas.repository.habitacion.HabitacionRepository;
import com.hotelcaciquebolivar.reservas.repository.habitacion.TipoHabitacionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Inicializador de tipos de habitación y habitaciones del hotel.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Order#4} para ejecutarse
 * después de los inicializadores de datos maestros (municipios, tipos de
 * identificación). Crea los tipos de habitación y las habitaciones físicas
 * del hotel si la base de datos está vacía.</p>
 *
 * <p>Tipos de habitación creados:</p>
 * <ul>
 *   <li><strong>Cama doble</strong> — capacidad 2, precio base $70.000 COP</li>
 *   <li><strong>Cama sencilla</strong> — capacidad 1-2, precio base $50.000 COP</li>
 *   <li><strong>Dos camas</strong> — capacidad 2-4, precio base $90.000 COP</li>
 * </ul>
 *
 * <p>Todos los tipos incluyen precio por persona extra de $15.000 COP.</p>
 *
 * @see TipoHabitacionRepository
 * @see HabitacionRepository
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class HabitacionDataInitializer implements CommandLineRunner {

    private static final Logger log
            = LoggerFactory.getLogger(HabitacionDataInitializer.class);

    private final TipoHabitacionRepository tipoRepo;
    private final HabitacionRepository habitacionRepo;

    /**
     * Ejecuta la inicialización de tipos y habitaciones del hotel.
     *
     * <p>Crea los tipos de habitación si no existen, luego crea las habitaciones
     * físicas asociadas. Maneja excepciones para evitar que la aplicación falle
     * durante la inicialización.</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    @Override
    public void run(String... args) {

        log.info("[INIT][HABITACION] Iniciando carga");

        try {
            crearTiposHabitacion();
            crearHabitaciones();

            log.info("[INIT][HABITACION] Finalizado");

        } catch (Exception e) {
            log.error("[INIT][HABITACION] Error inicializando", e);
        }
    }

    /**
     * Crea los tipos de habitación del hotel si no existen.
     *
     * <p>Verifica si ya hay tipos registrados en la base de datos. Si no hay,
     * crea tres tipos: "Cama doble", "Cama sencilla" y "Dos camas" con sus
     * respectivas capacidades, descripciones y precios base.</p>
     */
    private void crearTiposHabitacion() {

        if (tipoRepo.count() > 0) {
            log.info("[INIT][HABITACION] Tipos ya existen");
            return;
        }

        TipoHabitacion doble = new TipoHabitacion();
        doble.setNombre("Cama doble");
        doble.setDescripcion("Habitación cama doble");
        doble.setCapacidadBase(2);
        doble.setCapacidadMaxima(2);
        doble.setPrecioBase(new BigDecimal("70000"));
        doble.setPrecioPersonaExtra(new BigDecimal("15000"));

        TipoHabitacion sencilla = new TipoHabitacion();
        sencilla.setNombre("Cama sencilla");
        sencilla.setDescripcion("Habitación cama sencilla");
        sencilla.setCapacidadBase(1);
        sencilla.setCapacidadMaxima(2);
        sencilla.setPrecioBase(new BigDecimal("50000"));
        sencilla.setPrecioPersonaExtra(new BigDecimal("15000"));

        TipoHabitacion dosCamas = new TipoHabitacion();
        dosCamas.setNombre("Dos camas");
        dosCamas.setDescripcion("Habitación dos camas");
        dosCamas.setCapacidadBase(2);
        dosCamas.setCapacidadMaxima(4);
        dosCamas.setPrecioBase(new BigDecimal("90000"));
        dosCamas.setPrecioPersonaExtra(new BigDecimal("15000"));

        tipoRepo.saveAll(List.of(doble, sencilla, dosCamas));

        log.info("[INIT][HABITACION] Tipos creados: 3");
    }

    /**
     * Crea las habitaciones físicas del hotel si no existen.
     *
     * <p>Distribuye las habitaciones por número de habitación (201-322) con
     * los tipos correspondientes. Todas las habitaciones se crean con estado
     * {@link EstadoHabitacion#DISPONIBLE}. La distribución es:</p>
     * <ul>
     *   <li>201-209: Cama doble</li>
     *   <li>210-222: Cama sencilla</li>
     *   <li>301: Dos camas</li>
     *   <li>302-303: Cama doble</li>
     *   <li>304-307: Dos camas</li>
     *   <li>308: Cama doble</li>
     *   <li>310-322: Cama sencilla</li>
     * </ul>
     */
    private void crearHabitaciones() {

        if (habitacionRepo.count() > 0) {
            log.info("[INIT][HABITACION] Habitaciones ya existen");
            return;
        }

        TipoHabitacion doble
                = tipoRepo.findByNombre("Cama doble").orElseThrow();

        TipoHabitacion sencilla
                = tipoRepo.findByNombre("Cama sencilla").orElseThrow();

        TipoHabitacion dosCamas
                = tipoRepo.findByNombre("Dos camas").orElseThrow();

        List<Habitacion> habitaciones = new ArrayList<>();

        for (int i = 201; i <= 209; i++) {
            habitaciones.add(crear(String.valueOf(i), doble));
        }

        for (int i = 210; i <= 222; i++) {
            habitaciones.add(crear(String.valueOf(i), sencilla));
        }

        habitaciones.add(crear("301", dosCamas));
        habitaciones.add(crear("302", doble));
        habitaciones.add(crear("303", doble));

        for (int i = 304; i <= 307; i++) {
            habitaciones.add(crear(String.valueOf(i), dosCamas));
        }

        habitaciones.add(crear("308", doble));

        for (int i = 310; i <= 322; i++) {
            habitaciones.add(crear(String.valueOf(i), sencilla));
        }

        habitacionRepo.saveAll(habitaciones);

        log.info("[INIT][HABITACION] Habitaciones creadas: {}",
                habitaciones.size());
    }

    /**
     * Crea una habitación con el número y tipo especificados.
     *
     * <p>La habitación se crea con estado {@link EstadoHabitacion#DISPONIBLE}
     * y sin descripción.</p>
     *
     * @param numero número de la habitación (ej. "201", "301")
     * @param tipo tipo de habitación asociado
     * @return instancia de {@link Habitacion} lista para persistir
     */
    private Habitacion crear(
            String numero,
            TipoHabitacion tipo
    ) {
        Habitacion habitacion = new Habitacion();
        habitacion.setNumero(numero);
        habitacion.setTipo(tipo);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacion.setDescripcion(null);

        return habitacion;
    }
}
