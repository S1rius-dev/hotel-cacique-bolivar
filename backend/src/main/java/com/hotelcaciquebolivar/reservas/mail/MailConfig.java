package com.hotelcaciquebolivar.reservas.mail;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configuración de Spring Boot para el subsistema de envío de correos electrónicos.
 *
 * <p>Define los beans necesarios para el procesamiento de plantillas HTML de correos
 * utilizando Thymeleaf. Configura el motor de plantillas con un resolvedor que busca
 * las plantillas en el directorio {@code templates/emails/} del classpath, con extensión
 * {@code .html}, codificación UTF-8 y habilitación de caché.</p>
 *
 * <p>Esta configuración también habilita las propiedades de {@link EmailProperties}
 * mediante la anotación {@code @EnableConfigurationProperties}, permitiendo
 * la inyección de las propiedades del prefijo {@code app.mail}.</p>
 */
@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class MailConfig {

    /**
     * Crea y configura el bean del motor de plantillas Thymeleaf dedicado al envío de correos.
     *
     * @return instancia de {@link TemplateEngine} configurada con el resolvedor de plantillas de correo
     */
    @Bean
    public TemplateEngine emailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(emailTemplateResolver());
        return engine;
    }

    /**
     * Crea el resolvedor de plantillas que busca archivos HTML en el classpath.
     *
     * <p>Configura el resolvedor para buscar plantillas en {@code templates/emails/}
     * con extensión {@code .html}, utilizando modo HTML, codificación UTF-8
     * y caché habilitada para mejorar el rendimiento.</p>
     *
     * @return resolvedor de plantillas configurado para correos electrónicos
     */
    private ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/emails/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);
        return resolver;
    }
}
