package com.hotelcaciquebolivar.reservas.mail;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hotelcaciquebolivar.reservas.mail.dto.DatosCorreo;

/**
 * Servicio encargado del procesamiento y renderizado de plantillas HTML para correos electrónicos.
 *
 * <p>Utiliza el motor de plantillas Thymeleaf para renderizar las plantillas HTML
 * almacenadas en el classpath del proyecto. Inyecta variables comunes en cada plantilla
 * como el nombre del hotel, correo del hotel, teléfono, dirección, año actual y URL base.</p>
 *
 * <p>Las plantillas se encuentran en el directorio {@code templates/emails/} y utilizan
 * la extensión {@code .html}. El objeto {@link DatosCorreo} se pasa como variable
 * {@code data} al contexto de Thymeleaf para su uso dentro de las plantillas.</p>
 */
@Service
public class EmailTemplateService {

    /**
     * Motor de plantillas Thymeleaf configurado específicamente para el procesamiento de correos.
     */
    private final TemplateEngine emailTemplateEngine;
    /**
     * Propiedades de configuración de correo utilizadas para obtener datos del hotel.
     */
    private final EmailProperties emailProperties;

    /**
     * Construye una instancia de {@code EmailTemplateService} con el motor de plantillas
     * y las propiedades de correo inyectadas por Spring.
     *
     * @param emailTemplateEngine motor de plantillas Thymeleaf para el renderizado HTML
     * @param emailProperties     propiedades de configuración de correo del hotel
     */
    public EmailTemplateService(TemplateEngine emailTemplateEngine, EmailProperties emailProperties) {
        this.emailTemplateEngine = emailTemplateEngine;
        this.emailProperties = emailProperties;
    }

    /**
     * Procesa una plantilla de correo electrónico con los datos proporcionados.
     *
     * <p>Renderiza la plantilla especificada mediante Thymeleaf, inyectando los datos
     * del correo y las variables comunes del hotel (nombre, correo, teléfono, dirección,
     * año actual y URL base).</p>
     *
     * @param templateName nombre de la plantilla a renderizar (sin ruta ni extensión,
     *                     ej: "reserva-confirmada", "reserva-cancelada", "respuesta-pqrs",
     *                     "recuperar-password")
     * @param data         objeto que implementa {@link DatosCorreo} con la información
     *                     específica del correo a enviar
     * @return contenido HTML resultante del procesamiento de la plantilla
     */
    public String procesar(String templateName, DatosCorreo data) {
        Context context = new Context();
        context.setVariable("data", data);
        context.setVariable("hotelNombre", "Hotel Cacique Bolívar");
        context.setVariable("hotelEmail", emailProperties.getFrom());
        context.setVariable("hotelTelefono", "+57 300 0000000");
        context.setVariable("hotelDireccion", "Calle 10 #5-20, Amalfi, Antioquia");

        context.setVariable("anioActual", java.time.Year.now().getValue());
        context.setVariable("baseUrl", emailProperties.getBaseUrl());
        return emailTemplateEngine.process(templateName, context);
    }
}
