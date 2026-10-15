package com.hotelcaciquebolivar.reservas.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuración de ejecución asíncrona y programación de tareas.
 *
 * <p>Implementa {@link AsyncConfigurer} para definir un {@link ThreadPoolTaskExecutor}
 * personalizado como bean {@code taskExecutor}, y un {@link ThreadPoolTaskScheduler}
 * para tareas programadas con {@code @Scheduled}.</p>
 *
 * <p>El pool de hilos asíncrono utiliza entre 2 y 4 hilos con una cola de 100 tareas.
 * El pool de programación utiliza 2 hilos. Ambos esperan la finalización de tareas
 * al apagar el servidor con un timeout de 30 segundos.</p>
 *
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.scheduling.annotation.EnableAsync
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configura y devuelve el executor asíncrono principal de la aplicación.
     *
     * <p>Parámetros del pool:</p>
     * <ul>
     *   <li>Pool mínimo: 2 hilos</li>
     *   <li>Pool máximo: 4 hilos</li>
     *   <li>Capacidad de cola: 100 tareas en espera</li>
     *   <li>Nombre de prefijo de hilos: {@code async-}</li>
     *   <li>Espera finalización al apagar: 30 segundos</li>
     * </ul>
     *
     * @return executor asíncrono configurado como bean {@code taskExecutor}
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Proporciona el manejador de excepciones no capturadas para tareas asíncronas.
     *
     * <p>Utiliza {@link SimpleAsyncUncaughtExceptionHandler} que registra las
     * excepciones no controladas en el log con nivel WARN.</p>
     *
     * @return manejador de excepciones asíncronas
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    /**
     * Configura y devuelve el scheduler de tareas programadas.
     *
     * <p>Utiliza un pool de 2 hilos con nombre de prefijo {@code scheduled-}.
     * Espera la finalización de tareas al apagar el servidor con un timeout
     * de 30 segundos.</p>
     *
     * @return scheduler de tareas programadas
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // El backup programado puede ocupar un hilo durante varios minutos. Con solo 2, un
        // mysqldump largo retrasaria el expirador de PENDIENTE_PAGO (que corre cada minuto)
        // y el heartbeat de SSE. Un tercer hilo evita que las tareas cortas tengan que esperar.
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
}
