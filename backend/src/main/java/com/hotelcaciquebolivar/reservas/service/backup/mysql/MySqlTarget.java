package com.hotelcaciquebolivar.reservas.service.backup.mysql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hotelcaciquebolivar.reservas.exception.backup.BackupExecutionException;

/**
 * Destino de una conexion MySQL deducido del JDBC url de la aplicacion.
 *
 * <p>Se reutiliza el url de la datasource en vez de duplicar la configuracion en
 * {@code app.backup}, de modo que las herramientas de linea de comandos siempre apunten a la
 * misma base que la aplicacion: si alguien cambia el destino en un solo sitio, no puede
 * desincronizar el backup.</p>
 *
 * @param host        servidor MySQL
 * @param port        puerto de conexion
 * @param baseDeDatos nombre del esquema
 */
public record MySqlTarget(String host, int port, String baseDeDatos) {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^jdbc:mysql://(?<host>[^/:?]+)(:(?<port>\\d+))?/(?<db>[^?]+)(\\?.*)?$");

    /**
     * Interpreta un JDBC url de MySQL.
     *
     * @param url jdbc url, por ejemplo {@code jdbc:mysql://localhost:3306/hotelcaciquebolivar}
     * @return el destino deducido
     * @throws BackupExecutionException si el url no es de MySQL
     */
    public static MySqlTarget parse(String url) {
        Matcher matcher = URL_PATTERN.matcher(url == null ? "" : url);
        if (!matcher.matches()) {
            throw new BackupExecutionException(
                    "No se pudo interpretar el JDBC url de la base de datos para MySQL: " + url);
        }
        String puerto = matcher.group("port");
        return new MySqlTarget(
                matcher.group("host"),
                puerto != null ? Integer.parseInt(puerto) : 3306,
                matcher.group("db"));
    }
}
