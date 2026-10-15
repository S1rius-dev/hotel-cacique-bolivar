package com.hotelcaciquebolivar.reservas.config.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.entity.caja.Caja;
import com.hotelcaciquebolivar.reservas.repository.caja.CajaRepository;

import jakarta.annotation.PostConstruct;

/**
 * Inicializador de las cajas del sistema (puntos de venta).
 *
 * <p>Crea las cajas predeterminadas "RECEPCION" y "TIENDA" al arrancar la aplicación
 * si no existen previamente en la base de datos. Cada caja se marca como activa
 * por defecto.</p>
 *
 * <p>Las cajas representan los puntos desde donde se realizan transacciones
 * financieras en el hotel (recepción principal y tienda).</p>
 *
 * @see CajaRepository
 */
@Component
public class CajaDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(CajaDataInitializer.class);

    private final CajaRepository cajaRepository;

    public CajaDataInitializer(CajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    /**
     * Inicializa las cajas del sistema al construir el bean.
     *
     * <p>Crea las cajas "RECEPCION" y "TIENDA" si no existen.</p>
     */
    @PostConstruct
    public void init() {
        crearSiNoExiste("RECEPCION");
        crearSiNoExiste("TIENDA");
        log.info("[INIT][CAJA] Cajas inicializadas");
    }

    /**
     * Crea una caja con el nombre indicado si no existe una caja con ese nombre.
     *
     * <p>Busca la caja por nombre en la base de datos. Si no la encuentra,
     * crea una nueva {@link Caja} con el nombre proporcionado y estado activo.</p>
     *
     * @param nombre nombre de la caja a crear (ej. "RECEPCION", "TIENDA")
     */
    private void crearSiNoExiste(String nombre) {
        if (cajaRepository.findByNombre(nombre).isEmpty()) {
            Caja caja = new Caja();
            caja.setNombre(nombre);
            caja.setActivo(true);
            cajaRepository.save(caja);
            log.info("[INIT][CAJA] Caja '{}' creada", nombre);
        }
    }
}
