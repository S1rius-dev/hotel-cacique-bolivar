package com.hotelcaciquebolivar.reservas.service.backup.mysql;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;

/**
 * Ejecucion de procesos externos de MySQL con timeout y diagnostico.
 *
 * <p>Se centraliza el manejo de procesos porque tanto {@code mysqldump} como {@code mysql}
 * necesitan el mismo cuidado y equivocarse en esto cuelga el backend: hay que separar
 * stdout de stderr (el SQL va en stdout y los avisos en stderr; mezclarlos produce un
 * .sql.gz irrestaurable) y hay que drenar los flujos en hilos aparte.</p>
 *
 * <p>Cuando el proceso se alimenta por stdin, la escritura ocurre en un hilo propio mientras
 * el principal espera. Si se escribiera en el hilo principal, no se podria aplicar el timeout
 * (el hilo estaria ocupado escribiendo) y ademas un cliente que deja de leer su stdin
 * dejaria al escritor bloqueado para siempre.</p>
 */
public final class MysqlProcessRunner {

    private static final Logger log = LoggerFactory.getLogger(MysqlProcessRunner.class);

    /** Limita cuanto texto de stderr se conserva antes de truncar el diagnostico. */
    private static final int MAX_DIAGNOSTICO_CHARS = 4000;

    private static final int BUFFER = 16384;

    private MysqlProcessRunner() {
    }

    /**
     * Escribe la salida del proceso en un destino, por ejemplo un archivo comprimido.
     *
     * <p>La copia de stdout ocurre en un hilo propio y el hilo principal espera al proceso.
     * Copiar en el hilo principal haria que el timeout no empezara a contar hasta que
     * stdout cerrara, es decir nunca cuando el proceso se cuelga con el pipe abierto.</p>
     *
     * @param comando         argumentos del proceso, siendo el primero el ejecutable
     * @param salida          donde escribir stdout; el llamador lo cierra
     * @param timeoutSegundos tiempo maximo de espera
     * @param descripcion     que se estaba haciendo, para el mensaje de error
     * @return codigo de salida y diagnostico de stderr
     * @throws BackupExecutionException si el proceso falla, expira o es interrumpido
     */
    public static Resultado conSalida(List<String> comando, OutputStream salida,
            int timeoutSegundos, String descripcion) {

        StringBuilder diagnostico = new StringBuilder();
        ExecutorService hilos = newHilos(2, "mysql-io");
        try {
            Process proceso = iniciar(comando, descripcion);

            CompletableFuture<Void> stderr = CompletableFuture.runAsync(
                    () -> drenar(proceso.getErrorStream(), diagnostico), hilos);
            CompletableFuture<Void> copia = CompletableFuture.runAsync(() -> {
                try {
                    copiar(proceso.getInputStream(), salida);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            }, hilos);

            int codigo = esperar(proceso, timeoutSegundos, descripcion);
            unirDrenaje(stderr);
            unirDrenaje(copia);
            return new Resultado(codigo, recortar(diagnostico));
        } finally {
            hilos.shutdownNow();
        }
    }

    /**
     * Alimenta la entrada del proceso y drena su salida.
     *
     * @param comando         argumentos del proceso, siendo el primero el ejecutable
     * @param alimentador     escribe el contenido que el proceso consumira por stdin
     * @param timeoutSegundos tiempo maximo de espera
     * @param descripcion     que se estaba haciendo, para el mensaje de error
     * @return codigo de salida y diagnostico de stderr
     * @throws BackupExecutionException si el proceso falla, expira o es interrumpido
     */
    public static Resultado conEntrada(List<String> comando, Alimentador alimentador,
            int timeoutSegundos, String descripcion) {

        StringBuilder diagnostico = new StringBuilder();
        StringBuilder salida = new StringBuilder();
        ExecutorService hilos = newHilos(3, "mysql-io");
        try {
            Process proceso = iniciar(comando, descripcion);

            // stderr y stdout se drenan aparte: si el cliente MySQL escribiera en stdout y
            // nadie lo leyera, ambos lados terminarian bloqueados.
            CompletableFuture<Void> stderr = CompletableFuture.runAsync(
                    () -> drenar(proceso.getErrorStream(), diagnostico), hilos);
            CompletableFuture<Void> stdout = CompletableFuture.runAsync(
                    () -> drenar(proceso.getInputStream(), salida), hilos);
            CompletableFuture<Void> escritura = CompletableFuture.runAsync(() -> {
                try (OutputStream stdin = proceso.getOutputStream()) {
                    alimentador.escribir(stdin);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            }, hilos);

            int codigo = esperar(proceso, timeoutSegundos, descripcion);
            unirDrenaje(stderr);
            unirDrenaje(stdout);

            // El codigo de salida no basta: si el proceso murio a mitad, la escritura por
            // stdin falla con broken pipe y hay que reportarlo aunque el codigo sea 0.
            try {
                escritura.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new BackupExecutionException(
                        "No se pudo escribir la entrada de " + descripcion + ": "
                                + causa(e).getMessage() + detalle(recortar(diagnostico)), causa(e));
            }

            return new Resultado(codigo, unir(recortar(salida), recortar(diagnostico)));
        } finally {
            hilos.shutdownNow();
        }
    }

    /**
     * Ejecuta un comando sin canalizar datos, del tipo {@code mysql -e "CREATE DATABASE ..."}.
     *
     * <p>Se sigue drenando stdout y stderr en hilos aparte: el cliente de MySQL escribe
     * avisos por stderr y, si nadie los leyera, el pipe se llenaria y el proceso se
     * quedaria bloqueado escribiendo.</p>
     *
     * @param comando         argumentos del proceso, siendo el primero el ejecutable
     * @param timeoutSegundos tiempo maximo de espera
     * @param descripcion     que se estaba haciendo, para el mensaje de error
     * @return codigo de salida y diagnostico acumulado
     * @throws BackupExecutionException si el proceso no arranca, expira o es interrumpido
     */
    public static Resultado ejecutar(List<String> comando, int timeoutSegundos, String descripcion) {
        StringBuilder diagnostico = new StringBuilder();
        StringBuilder salida = new StringBuilder();
        ExecutorService hilos = newHilos(2, "mysql-io");
        try {
            Process proceso = iniciar(comando, descripcion);
            CompletableFuture<Void> stderr = CompletableFuture.runAsync(
                    () -> drenar(proceso.getErrorStream(), diagnostico), hilos);
            CompletableFuture<Void> stdout = CompletableFuture.runAsync(
                    () -> drenar(proceso.getInputStream(), salida), hilos);

            int codigo = esperar(proceso, timeoutSegundos, descripcion);
            unirDrenaje(stderr);
            unirDrenaje(stdout);
            return new Resultado(codigo, unir(recortar(salida), recortar(diagnostico)));
        } finally {
            hilos.shutdownNow();
        }
    }

    private static Process iniciar(List<String> comando, String descripcion) {
        try {
            ProcessBuilder builder = new ProcessBuilder(comando);
            // MySQL escribe el SQL en stdout y los avisos en stderr: mezclarlos contaminaria
            // el dump con mensajes de advertencia.
            builder.redirectErrorStream(false);
            return builder.start();
        } catch (IOException e) {
            throw new BackupExecutionException(
                    "No se pudo ejecutar " + descripcion + " (" + comando.get(0) + "). "
                    + "Verifica que la ruta configurada apunte a un ejecutable valido. Causa: "
                    + e.getMessage(), e);
        }
    }

    private static int esperar(Process proceso, int timeoutSegundos, String descripcion) {
        boolean termino;
        try {
            termino = proceso.waitFor(timeoutSegundos, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            destruir(proceso);
            Thread.currentThread().interrupt();
            throw new BackupExecutionException(descripcion + " fue interrumpido", e);
        }
        if (!termino) {
            destruir(proceso);
            throw new BackupExecutionException(
                    descripcion + " excedio el timeout de " + timeoutSegundos + " segundos");
        }
        return proceso.exitValue();
    }

    private static void unirDrenaje(CompletableFuture<Void> drenaje) {
        try {
            drenaje.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.debug("El drenaje de un flujo no termino limpiamente: {}", e.getMessage());
        }
    }

    private static void copiar(InputStream origen, OutputStream destino) throws IOException {
        byte[] buffer = new byte[BUFFER];
        int leidos;
        while ((leidos = origen.read(buffer)) != -1) {
            destino.write(buffer, 0, leidos);
        }
    }

    private static void drenar(InputStream origen, StringBuilder destino) {
        try (InputStream in = origen) {
            byte[] buffer = new byte[512];
            int leidos;
            while ((leidos = in.read(buffer)) != -1 && destino.length() < MAX_DIAGNOSTICO_CHARS) {
                destino.append(new String(buffer, 0, leidos, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            destino.append("no se pudo leer el flujo: ").append(e.getMessage());
        }
    }

    private static void destruir(Process proceso) {
        proceso.destroyForcibly();
        try {
            proceso.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ExecutorService newHilos(int cantidad, String nombre) {
        return Executors.newFixedThreadPool(cantidad, r -> {
            Thread t = new Thread(r, nombre);
            t.setDaemon(true);
            return t;
        });
    }

    private static String recortar(StringBuilder texto) {
        return texto.length() > 500 ? texto.substring(0, 500) + "..." : texto.toString().trim();
    }

    /** Une stdout y stderr en un unico diagnostico, omitting los vacios. */
    private static String unir(String... partes) {
        StringBuilder unido = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (!unido.isEmpty()) {
                    unido.append(" | ");
                }
                unido.append(parte);
            }
        }
        return unido.toString();
    }

    private static String detalle(String diagnostico) {
        return diagnostico == null || diagnostico.isBlank() ? "" : ": " + diagnostico;
    }

    private static Throwable causa(Exception e) {
        return e.getCause() != null ? e.getCause() : e;
    }

    /**
     * Escribe el contenido que el proceso consumira por su entrada estandar.
     */
    @FunctionalInterface
    public interface Alimentador {
        void escribir(OutputStream stdin) throws IOException;
    }

    /**
     * Resultado de un proceso externo.
     *
     * @param codigoSalida codigo devuelto por el proceso
     * @param diagnostico  texto acumulado de stderr, ya truncado
     */
    public record Resultado(int codigoSalida, String diagnostico) {
    }
}
