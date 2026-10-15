package com.hotelcaciquebolivar.reservas.config.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hotelcaciquebolivar.reservas.repository.cliente.ClienteRepository;
import com.hotelcaciquebolivar.reservas.service.migration.ClienteImportService;
import com.hotelcaciquebolivar.reservas.service.migration.ClienteImportService.ImportResult;

/**
 * Inicializador de migración automática de clientes desde archivo CSV.
 *
 * <p>Implementa {@link CommandLineRunner} con {@link Order#5} para ejecutarse
 * después de los inicializadores de datos maestros. Importa clientes desde
 * un archivo CSV ({@code data/migration-client.csv}) a la base de datos si
 * la tabla de clientes está vacía.</p>
 *
 * <p>La migración puede deshabilitarse configurando {@code MIGRACION_CLIENTES_AUTO=false}.
 * Por defecto está habilitada. El proceso de importación es idempotente: si ya
 * existen clientes, se omite la migración automática.</p>
 *
 * @see ClienteImportService
 * @see ClienteRepository
 */
@Component
@Order(5)
public class MigracionClientesInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionClientesInitializer.class);

    private final ClienteImportService clienteImportService;
    private final ClienteRepository clienteRepository;

    @Value("${MIGRACION_CLIENTES_AUTO:true}")
    private boolean migracionAutomatica;

    public MigracionClientesInitializer(
            ClienteImportService clienteImportService,
            ClienteRepository clienteRepository) {
        this.clienteImportService = clienteImportService;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Ejecuta la migración automática de clientes al arrancar la aplicación.
     *
     * <p>Verifica las condiciones de activación (propiedad {@code MIGRACION_CLIENTES_AUTO}
     * y ausencia de clientes existentes) antes de iniciar la importación. Registra
     * un resumen detallado del resultado de la migración incluyendo total de filas,
     * importados, saltados y errores.</p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    @Override
    public void run(String... args) {
        if (!migracionAutomatica) {
            log.info("[MIGRACION] Migración automática deshabilitada (MIGRACION_CLIENTES_AUTO=false)");
            return;
        }

        long clientesExistentes = clienteRepository.count();
        if (clientesExistentes > 0) {
            log.info("[MIGRACION] Ya existen {} clientes en BD, saltando migración automática", clientesExistentes);
            return;
        }

        log.info("[MIGRACION] Iniciando migración automática de clientes desde CSV...");
        ImportResult resultado = clienteImportService.importar();

        log.info("═══════════════════════════════════════════════");
        log.info("  RESULTADO DE MIGRACIÓN AUTOMÁTICA");
        log.info("  Total filas en CSV: {}", resultado.getTotal());
        log.info("  Importados:          {}", resultado.getImportados());
        log.info("  Saltados:            {}", resultado.getSaltados());
        log.info("  Errores:             {}", resultado.getErrores());
        log.info("═══════════════════════════════════════════════");

        if (!resultado.getDetalles().isEmpty()) {
            log.info("Detalles:");
            resultado.getDetalles().forEach(d -> log.info("  - {}", d));
        }
    }
}
