package com.hotelcaciquebolivar.reservas.mail;

/**
 * Excepción personalizada que se lanza cuando ocurre un error durante el envío de correos electrónicos.
 * Extiende {@link RuntimeException} para ser una excepción no verificada, lo que permite
 * que se propague sin necesidad de ser declarada en la firma de los métodos.
 *
 * <p>Esta excepción se genera típicamente en {@link SmtpEmailService} cuando el envío
 * SMTP falla por motivos de conexión, autenticación, timeout u otros errores
 * relacionados con el servicio de correo.</p>
 */
public class EmailException extends RuntimeException {

    /**
     * Construye una nueva {@code EmailException} con el mensaje de error especificado
     * y la causa original que provocó la excepción.
     *
     * @param message mensaje descriptivo del error ocurrido
     * @param cause   excepción original que causó este error de envío de correo
     */
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construye una nueva {@code EmailException} con el mensaje de error especificado.
     *
     * @param message mensaje descriptivo del error ocurrido
     */
    public EmailException(String message) {
        super(message);
    }
}
