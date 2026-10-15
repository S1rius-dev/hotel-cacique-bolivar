package com.hotelcaciquebolivar.reservas.config.backup;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Propiedades de configuracion del sistema de copias de seguridad.
 *
 * <p>Se vinculan al prefijo {@code app.backup} en {@code application.yml} y admiten
 * sobreescritura por variable de entorno. Toda la configuracion es de solo lectura:
 * cambiarla requiere reiniciar la aplicacion. No existe endpoint para modificarla
 * en caliente porque no hay backing store para persistir los cambios.</p>
 *
 * <p>Ejemplo de sobreescritura por entorno:
 * {@code BACKUP_ENABLED=true}, {@code MYSQLDUMP_PATH=/usr/bin/mysqldump},
 * {@code BACKUP_SCHEDULE_CRON=0 0 2 * * *}.</p>
 */
@ConfigurationProperties(prefix = "app.backup")
@Data
public class BackupProperties {

    /** Indica si el sistema de backups esta habilitado. Por defecto {@code false}
     *  para que un entorno nuevo no dispare mysqldump sin querer. */
    private boolean enabled = false;

    /** Ruta al ejecutable mysqldump. En Windows suele ser una ruta absoluta con
     *  espacios, p.ej. {@code C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe}. */
    private String mysqldumpPath = "mysqldump";

    /** Ruta al ejecutable mysql, usado para clonar una copia en otra base. Necesario
     *  porque MySQL no esta en el PATH de Windows en la mayoria de instalaciones. */
    private String mysqlPath = "mysql";

    /** Argumentos adicionales que se anexan a la invocacion de mysqldump.
     *  Por defecto usa una transaccion coherente y charset utf8mb4. */
    private String dumpExtraArgs = "--single-transaction --routines --triggers "
            + "--default-character-set=utf8mb4 --no-tablespaces";

    /** Tiempo maximo permitido para una ejecucion de mysqldump. Al superarlo el
     *  proceso se destruye y la ejecucion se marca como fallida. */
    private int timeoutSeconds = 1800;

    /** Configuracion del almacenamiento fisico de los archivos .sql.gz. */
    private Storage storage = new Storage();

    /** Configuracion de las tareas programadas. */
    private Schedule schedule = new Schedule();

    /** Configuracion de la clonacion de copias en otras bases. */
    private Clon clon = new Clon();

    /** Politica de retencion por origen de copia. */
    private Retention retention = new Retention();

    /**
     * Almacenamiento fisico de los archivos comprimidos.
     */
    @Data
    public static class Storage {

        /** Proveedor activo. Por defecto {@code local}. Un proveedor de nube se
         *  registrara mas adelante como implementacion adicional de {@code BackupStorage}. */
        private String provider = "local";

        /** Directorio raiz de los backups. Cada origen usa un subdirectorio
         *  (automatic/ o manual/). El valor por defecto es relativo al directorio
         *  de trabajo de la aplicacion. */
        private String dir = "./backups";

        /** Subdirectorio de las copias generadas por la tarea programada. */
        private String folderAutomatic = "automatic";

        /** Subdirectorio de las copias solicitadas a mano. */
        private String folderManual = "manual";
    }

    /**
     * Tareas programadas de backup y purga de retenidos.
     */
    @Data
    public static class Schedule {

        /** Habilita la tarea programada de backup automatico. */
        private boolean enabled = false;

        /** Expresion cron con 6 campos (segundos inclusos) que dispara el backup automatico. */
        private String cron = "0 0 2 * * *";

        /** Zona horaria del cron. Debe fijarse explicitamente porque el servidor
         *  puede operar en UTC mientras el hotel opera en America/Bogota. */
        private String zone = "America/Bogota";

        /** Habilita la purga diaria de backups que exceden la retencion. */
        private boolean cleanupEnabled = true;

        /** Hora del dia (cron de 6 campos) para la purga de retenidos. */
        private String cleanupCron = "0 30 3 * * *";
    }

    /**
     * Dias de retencion por origen. Un valor negativo significa que nunca se borra.
     */
    @Data
    public static class Retention {

        /** Dias que se conservan los backups automaticos antes de purgarse. */
        private int automaticDays = 30;

        /** Dias que se conservan los backups manuales. Por defecto {@code -1} (indefinido). */
        private int manualDays = -1;
    }

    /**
     * Parametros de la clonacion de una copia en otra base de datos.
     */
    @Data
    public static class Clon {

        /** Tiempo maximo para importar el .sql.gz. Es mayor que el del dump porque
         *  escribir miles de sentencias en MySQL suele tardar mas que leerlas. */
        private int timeoutSeconds = 3600;

        /** Prefijo por defecto que la UI sugiere al operador para el nombre destino. */
        private String defaultPrefix = "hotel_restaurada_";
    }
}
