package com.hotelcaciquebolivar.reservas.service.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;

import com.hotelcaciquebolivar.reservas.config.backup.BackupProperties;
import com.hotelcaciquebolivar.reservas.dto.backup.BackupVerificacionResponse;
import com.hotelcaciquebolivar.reservas.entity.audit.AuditAction;
import com.hotelcaciquebolivar.reservas.entity.backup.BackupHistory;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupDestinoInvalidoException;
import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;
import com.hotelcaciquebolivar.reservas.exception.resource.ResourceNotFoundException;
import com.hotelcaciquebolivar.reservas.repository.backup.BackupHistoryRepository;
import com.hotelcaciquebolivar.reservas.service.audit.AuditService;
import com.hotelcaciquebolivar.reservas.service.backup.mysql.MySqlTarget;
import com.hotelcaciquebolivar.reservas.service.backup.storage.BackupStorage;

/**
 * Tests del clon de copias de seguridad.
 *
 * <p>El foco son las defensas que impiden el error irreversible: rechazar un destino
 * invalido, rechazar la base en uso, y no importar un archivo cuyo checksum no coincide.
 * No se invoca mysql real (el perfil de test usa H2), asi que los procesos externos se
 * dejan fuera; lo que se comprueba es que ni siquiera llegan a lanzarse.</p>
 */
@ExtendWith(MockitoExtension.class)
class BackupCloneServiceTest {

    private static final String RUTA = "manual/backup-manual-2026-09-26T18-12-46.sql.gz";

    @Mock
    private BackupHistoryRepository repository;

    @Mock
    private BackupStorage storage;

    @Mock
    private DataSourceProperties dataSourceProperties;

    @Mock
    private AuditService auditService;

    private BackupProperties properties;
    private BackupCloneService cloneService;

    @BeforeEach
    void setUp() {
        properties = new BackupProperties();
        cloneService = new BackupCloneService(repository, storage, properties, dataSourceProperties, auditService);
    }

    private static final String URL_VIVA = "jdbc:mysql://localhost:3306/hotelcaciquebolivar?useSSL=false";

    private BackupHistory copia(String checksum) {
        BackupHistory registro = new BackupHistory();
        registro.setId(7L);
        registro.setStoragePath(RUTA);
        registro.setChecksumSha256(checksum);
        return registro;
    }

    private void Given_copia(String checksum) {
        when(repository.findById(7L)).thenReturn(Optional.of(copia(checksum)));
    }

    /** SHA-256 de la cadena vacia, usado como checksum de ejemplo valido. */
    private static final String CHECKSUM_REAL = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private void Given_archivoCon(byte[] contenido) throws IOException {
        when(storage.existe(RUTA)).thenReturn(true);
        when(storage.abrirLectura(RUTA)).thenAnswer(inv -> new ByteArrayInputStream(contenido));
    }

    @Nested
    @DisplayName("Validacion del nombre de destino")
    class ValidacionDestino {

        private final MySqlTarget viva = MySqlTarget.parse(URL_VIVA);

        @ParameterizedTest
        @ValueSource(strings = {
                "hotel_clon", "hotel_clon_2026", "HOTEL_CLON", "a", "clon123",
                "9" })
        void debeAceptarNombresValidos(String nombre) {
            assertEquals(nombre, cloneService.validarNombreDestino(nombre, viva));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "hotel-clon",           // guion medio
                "hotel clon",           // espacio
                "hotel.clon",           // punto
                "hotel;drop",           // inyeccion
                "hotel`",               // acento grave
                "",
                "   " })
        void debeRechazarNombresInvalidos(String nombre) {
            BackupDestinoInvalidoException error = assertThrows(BackupDestinoInvalidoException.class,
                    () -> cloneService.validarNombreDestino(nombre, viva));
            assertTrue(error.getMessage().contains("invalido"),
                    "el error debe explicar que el nombre no es valido");
        }

        @Test
        void debeRechazarNombresDemasiadoLargos() {
            String nombre = "a".repeat(65);

            assertThrows(BackupDestinoInvalidoException.class,
                    () -> cloneService.validarNombreDestino(nombre, viva));
        }

        @Test
        @DisplayName("Debe rechazar la base en uso, sin importar mayusculas")
        void debeRechazarLaBaseEnUso() {
            BackupDestinoInvalidoException error = assertThrows(BackupDestinoInvalidoException.class,
                    () -> cloneService.validarNombreDestino("HOTELCACIQUEBOLIVAR", viva));

            assertTrue(error.getMessage().contains("base en uso"),
                    "el error debe decir que es la base en uso, no solo que el nombre es invalido");
        }

        @Test
        void debeRechazarNull() {
            assertThrows(BackupDestinoInvalidoException.class,
                    () -> cloneService.validarNombreDestino(null, viva));
        }
    }

    @Nested
    @DisplayName("Verificacion de integridad")
    class Verificacion {

        @Test
        void debeReportarIntegroCuandoElChecksumCoincide() throws IOException {
            Given_copia(CHECKSUM_REAL);
            Given_archivoCon(new byte[0]);

            BackupVerificacionResponse resultado = cloneService.verificar(7L, "admin@hotel.com");

            assertTrue(resultado.integro());
            assertEquals(CHECKSUM_REAL, resultado.checksumEsperado());
            assertEquals(CHECKSUM_REAL, resultado.checksumCalculado());
            verify(auditService).log(any(AuditAction.class), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Un checksum discrepante es un resultado, no una excepcion")
        void debeReportarDanadoSinLanzar() throws IOException {
            Given_copia("0000000000000000000000000000000000000000000000000000000000000000");
            Given_archivoCon("contenido alterado".getBytes(StandardCharsets.UTF_8));

            BackupVerificacionResponse resultado = cloneService.verificar(7L, "admin@hotel.com");

            assertFalse(resultado.integro());
            assertEquals("0000000000000000000000000000000000000000000000000000000000000000",
                    resultado.checksumEsperado());
            assertFalse(resultado.checksumCalculado().isBlank());
        }

        @Test
        void debeFallarSiElArchivoNoExiste() {
            Given_copia(CHECKSUM_REAL);
            when(storage.existe(RUTA)).thenReturn(false);

            BackupExecutionException error = assertThrows(BackupExecutionException.class,
                    () -> cloneService.verificar(7L, null));

            assertTrue(error.getMessage().contains("no existe"));
        }
    }

    @Nested
    @DisplayName("Clonado")
    class Clonado {

        @Test
        @DisplayName("Con checksum incorrecto no debe registrar el clon como realizado")
        void debeRechazarArchivoDanadoAntesDeTocarElServidor() throws IOException {
            when(dataSourceProperties.determineUrl()).thenReturn(URL_VIVA);
            Given_copia("1111111111111111111111111111111111111111111111111111111111111111");
            Given_archivoCon("contenido que no corresponde".getBytes(StandardCharsets.UTF_8));

            BackupExecutionException error = assertThrows(BackupExecutionException.class,
                    () -> cloneService.clonar(7L, "hotel_clon", "admin@hotel.com"));

            assertTrue(error.getMessage().contains("danada"));
            // Si creara la base y luego fallara al importar, dejaria un clon a medias
            // y el operador tendria un "restaurado" que en realidad esta vacio.
            verify(auditService, never()).log(eq(AuditAction.BACKUP_CLONED), anyString(), any(), anyString(), anyString());
        }

        @Test
        void debeRechazarLaBaseEnUsoAntesDeLeerElArchivo() throws IOException {
            when(dataSourceProperties.determineUrl()).thenReturn(URL_VIVA);
            Given_copia(CHECKSUM_REAL);

            BackupDestinoInvalidoException error = assertThrows(BackupDestinoInvalidoException.class,
                    () -> cloneService.clonar(7L, "hotelcaciquebolivar", "admin@hotel.com"));

            assertTrue(error.getMessage().contains("base en uso"));
            // El nombre se valida antes que el archivo: un destino invalido no debe
            // provocar ni una lectura del .sql.gz.
            verify(storage, never()).abrirLectura(anyString());
        }

        @Test
        void debeRechazarUnaCopiaSinChecksumRegistrado() {
            when(dataSourceProperties.determineUrl()).thenReturn(URL_VIVA);
            Given_copia(null);
            when(storage.existe(RUTA)).thenReturn(true);

            BackupExecutionException error = assertThrows(BackupExecutionException.class,
                    () -> cloneService.clonar(7L, "hotel_clon", "admin@hotel.com"));

            assertTrue(error.getMessage().contains("checksum"));
        }

        @Test
        void debeFallarSiLaCopiaNoExisteEnElHistorial() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cloneService.clonar(99L, "hotel_clon", "admin@hotel.com"));
        }
    }
}

