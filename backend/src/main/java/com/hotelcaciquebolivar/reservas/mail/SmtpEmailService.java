package com.hotelcaciquebolivar.reservas.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hotelcaciquebolivar.reservas.mail.dto.PasswordRecoveryData;
import com.hotelcaciquebolivar.reservas.mail.dto.PqrsRespuestaData;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaCanceladaData;
import com.hotelcaciquebolivar.reservas.mail.dto.ReservaConfirmadaData;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementación del servicio de envío de correos electrónicos utilizando SMTP.
 *
 * <p>Esta clase implementa la interfaz {@link EmailService} y utiliza {@link JavaMailSender}
 * de Spring para el envío de correos a través del protocolo SMTP. Los correos se envían
 * de forma asíncrona (mediante la anotación {@code @Async}) para no bloquear el hilo
 * de ejecución del solicitante.</p>
 *
 * <p>Cada método público delega al método privado {@link #enviarCorreo(String, DatosCorreo)},
 * que se encarga de renderizar la plantilla HTML correspondiente mediante
 * {@link EmailTemplateService}, construir el mensaje MIME con soporte para
 * imágenes inline (logo del hotel) y enviar el correo a través de SMTP.</p>
 *
 * <p>En caso de error durante el envío, se registra el error en el log y se lanza
 * una {@link EmailException} con el detalle del error.</p>
 */
@Service
public class SmtpEmailService implements EmailService {

    /**
     * Logger para el registro de eventos del envío de correos.
     */
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    /**
     * Componente de Spring para el envío de correos a través de SMTP.
     */
    private final JavaMailSender mailSender;

    /**
     * Servicio de procesamiento de plantillas HTML para correos.
     */
    private final EmailTemplateService templateService;

    /**
     * Propiedades de configuración de correo del hotel.
     */
    private final EmailProperties emailProperties;

    /**
     * Construye una instancia de {@code SmtpEmailService} con las dependencias inyectadas por Spring.
     *
     * @param mailSender      sender SMTP configurado en la aplicación
     * @param templateService servicio de renderizado de plantillas HTML
     * @param emailProperties propiedades de configuración de correo del hotel
     */
    public SmtpEmailService(JavaMailSender mailSender, EmailTemplateService templateService,
            EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.emailProperties = emailProperties;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Envía el correo de forma asíncrona utilizando la plantilla "reserva-confirmada".</p>
     */
    @Async
    @Override
    public void sendReservationConfirmation(ReservaConfirmadaData data) {
        enviarCorreo("reserva-confirmada", data);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Envía el correo de forma asíncrona utilizando la plantilla "reserva-cancelada".</p>
     */
    @Async
    @Override
    public void sendReservationCancellation(ReservaCanceladaData data) {
        enviarCorreo("reserva-cancelada", data);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Envía el correo de forma asíncrona utilizando la plantilla "respuesta-pqrs".</p>
     */
    @Async
    @Override
    public void sendPqrsResponse(PqrsRespuestaData data) {
        enviarCorreo("respuesta-pqrs", data);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Envía el correo de forma asíncrona utilizando la plantilla "recuperar-password".</p>
     */
    @Async
    @Override
    public void sendPasswordRecovery(PasswordRecoveryData data) {
        enviarCorreo("recuperar-password", data);
    }

    /**
     * Método privado que encapsula la lógica de envío de correos electrónicos.
     *
     * <p>Renderiza la plantilla HTML especificada con los datos del correo,
     * construye un mensaje MIME con soporte para imágenes inline (logo del hotel
     * desde {@code static/images/logo.png}), configura el remitente según
     * las propiedades de correo y envía el mensaje a través de SMTP.</p>
     *
     * <p>Registra exitosamente el envío en el log. En caso de {@link MessagingException}
     * o cualquier otra excepción, registra el error en el log y lanza
     * una {@link EmailException} envolviendo la causa original.</p>
     *
     * @param templateName nombre de la plantilla Thymeleaf a renderizar
     * @param data         datos del correo que implementa la interfaz {@link DatosCorreo}
     * @throws EmailException si ocurre un error durante el envío del correo
     */
    private void enviarCorreo(String templateName, com.hotelcaciquebolivar.reservas.mail.dto.DatosCorreo data) {
        try {
            String html = templateService.procesar(templateName, data);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(data.destinatario());
            helper.setSubject(data.asunto());
            helper.setText(html, true);
            helper.addInline("logo", new ClassPathResource("static/images/logo.png"), "image/png");
            helper.setFrom(emailProperties.getFrom(), emailProperties.getSenderName());

            mailSender.send(message);
            log.info("Correo enviado a {} — asunto: {}", data.destinatario(), data.asunto());
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {} — asunto: {}", data.destinatario(), data.asunto(), e);
            throw new EmailException("Error al enviar correo a " + data.destinatario(), e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo a {} — asunto: {}", data.destinatario(), data.asunto(), e);
            throw new EmailException("Error inesperado al enviar correo a " + data.destinatario(), e);
        }
    }
}
