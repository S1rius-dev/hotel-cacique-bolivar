package com.hotelcaciquebolivar.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación Hotel Cacique Bolívar.
 *
 * <p>Esta clase contiene el método {@code main} que inicia el contexto de la aplicación Spring Boot
 * para el sistema de reservas del Hotel Cacique Bolívar. La aplicación gestiona el ciclo completo
 * de reservas hoteleras, incluyendo habitaciones, huéspedes, pagos, facturación, inventario de
 * productos, caja, PQRS, notificaciones y paneles de control.</p>
 *
 * <p>La anotación {@code @EnableScheduling} habilita la ejecución de tareas programadas
 * (schedulers) utilizadas para procesos automáticos como la expiración de reservas pendientes,
 * la finalización automática de reservas y la detección de no-show.</p>
 *
 * @author Hotel Cacique Bolívar Development Team
 * @version 1.0
 * @since 1.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.scheduling.annotation.EnableScheduling
 */
@SpringBootApplication
@EnableScheduling
public class HotelCaciqueBolivarApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * <p>Este método delega al {@link SpringApplication} la inicialización del contexto
     * de la aplicación, el cual carga la configuración, crea los bean de Spring,
     * arranca el servidor web embebido y ejecuta los inicializadores de datos
     * definidos en la capa de configuración.</p>
     *
     * @param args argumentos de línea de comandos pasados a la aplicación,
     *             que pueden incluir perfiles de Spring ({@code --spring.profiles.active=dev})
     *             y otras propiedades de configuración
     */
    public static void main(String[] args) {
        SpringApplication.run(HotelCaciqueBolivarApplication.class, args);
    }

}
