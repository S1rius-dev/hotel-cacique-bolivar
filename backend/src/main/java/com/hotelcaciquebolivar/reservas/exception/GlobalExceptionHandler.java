package com.hotelcaciquebolivar.reservas.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.hotelcaciquebolivar.reservas.exception.base.BusinessException;
import com.hotelcaciquebolivar.reservas.security.ratelimit.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Manejador global de excepciones para la aplicación.
 * Intercepta todas las excepciones lanzadas en los controladores y las convierte
 * en respuestas HTTP estructuradas con formato {@link ErrorResponse}.
 * Proporciona manejo centralizado para excepciones de negocio, validación,
 * autenticación, concurrencia, y errores internos del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Estructura de respuesta estándar para errores HTTP.
     * Contiene el código de estado, mensaje de error, detalle, URI de la solicitud,
     * marca de tiempo y errores de campo opcionales para validaciones.
     */
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String detail;
        private final String path;
        private final long timestamp;
        private final List<FieldErrorDetail> errors;

        /**
         * Constructor completo de ErrorResponse.
         *
         * @param status  código de estado HTTP
         * @param error   código de error lógico (ej. VALIDATION_ERROR, NOT_FOUND)
         * @param detail  mensaje descriptivo del error en español
         * @param path    URI de la solicitud que generó el error
         * @param timestamp marca de tiempo en milisegundos desde la época
         * @param errors  lista de errores de campo para validaciones (puede ser null)
         */
        public ErrorResponse(int status, String error, String detail, String path, long timestamp,
                List<FieldErrorDetail> errors) {
            this.status = status;
            this.error = error;
            this.detail = detail;
            this.path = path;
            this.timestamp = timestamp;
            this.errors = errors;
        }

        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getDetail() { return detail; }
        public String getPath() { return path; }
        public long getTimestamp() { return timestamp; }
        public List<FieldErrorDetail> getErrors() { return errors; }
    }

    /**
     * Detalle de error de campo para respuestas de validación.
     * Representa un error específico en un campo de la solicitud.
     */
    public static class FieldErrorDetail {
        private final String field;
        private final String message;

        /**
         * Constructor de FieldErrorDetail.
         *
         * @param field   nombre del campo que falló la validación
         * @param message mensaje de error descriptivo en español
         */
        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }

    /**
     * Método auxiliar para construir una respuesta HTTP de error.
     *
     * @param status  estado HTTP a retornar
     * @param error   código de error lógico
     * @param detail  mensaje descriptivo del error
     * @param request solicitud HTTP actual
     * @param errors  lista de errores de campo (puede ser null)
     * @return ResponseEntity con el ErrorResponse construido
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String detail,
            HttpServletRequest request, List<FieldErrorDetail> errors) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(status.value(), error, detail,
                        request.getRequestURI(), System.currentTimeMillis(), errors));
    }

    /**
     * Maneja excepciones de negocio ({@link BusinessException}).
     * Retorna el estado HTTP y código de error definidos en la excepción.
     *
     * @param ex      excepción de negocio lanzada
     * @param request solicitud HTTP actual
     * @return ResponseEntity con el ErrorResponse correspondiente
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(
                new ErrorResponse(ex.getStatus().value(), ex.getErrorCode(), ex.getMessage(),
                        request.getRequestURI(), System.currentTimeMillis(), null));
    }

    /**
     * Maneja excepciones de validación de anotaciones Jakarta ({@link MethodArgumentNotValidException}).
     * Extrae los errores de campo del BindingResult y los retorna en la respuesta.
     *
     * @param ex      excepción de validación de argumentos
     * @param request solicitud HTTP actual
     * @return ResponseEntity con errors 400 y lista de errores de campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationAnnotations(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "VALIDATION_ERROR",
                        "Uno o m\u00e1s campos de la solicitud contienen valores inv\u00e1lidos.",
                        request.getRequestURI(), System.currentTimeMillis(), errors));
    }

    /**
     * Maneja excepciones de estado ilegal ({@link IllegalStateException}).
     * Retorna error 400 con código VALIDATION_ERROR.
     *
     * @param ex      excepción de estado ilegal
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 400
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request, null);
    }

    /**
     * Maneja excepciones de parámetro de solicitud faltante ({@link MissingServletRequestParameterException}).
     * Retorna error 400 indicando el nombre del parámetro requerido.
     *
     * @param ex      excepción de parámetro faltante
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 400
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "El par\u00e1metro '" + ex.getParameterName() + "' es obligatorio", request, null);
    }

    /**
     * Maneja excepciones de mensaje HTTP no legible ({@link HttpMessageNotReadableException}).
     * Retorna error 400 indicando formato de solicitud inválido.
     *
     * @param ex      excepción de mensaje no legible
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Formato de solicitud inv\u00e1lido. Verifique los tipos de datos enviados.", request, null);
    }

    /**
     * Maneja excepciones de tipo de parámetro no coincidente ({@link MethodArgumentTypeMismatchException}).
     * Retorna error 400 indicando el nombre del parámetro con valor inválido.
     *
     * @param ex      excepción de tipo de argumento
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "El valor del par\u00e1metro '" + ex.getName() + "' no es v\u00e1lido", request, null);
    }

    /**
     * Maneja excepciones de violación de restricciones ({@link ConstraintViolationException}).
     * Extrae las violaciones de restricciones y las retorna como errores de campo.
     *
     * @param ex      excepción de violación de restricción
     * @param request solicitud HTTP actual
     * @return ResponseEntity con errors 400 y lista de errores de campo
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "VALIDATION_ERROR",
                        "Errores de validaci\u00f3n en la petici\u00f3n",
                        request.getRequestURI(), System.currentTimeMillis(), errors));
    }

    /**
     * Maneja excepciones de denegación de acceso ({@link AccessDeniedException}).
     * Retorna error 403 indicando que el usuario no tiene permisos.
     *
     * @param ex      excepción de denegación de acceso
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "No tienes permisos para acceder a este recurso.", request, null);
    }

    /**
     * Maneja excepciones de bloqueo optimista y de integridad de datos.
     * Para {@link ObjectOptimisticLockingFailureException} retorna error 409 (conflicto de concurrencia).
     * Para {@link IncorrectResultSizeDataAccessException} retorna error 500 (error de integridad de datos).
     *
     * @param ex      excepción de bloqueo optimista o integridad de datos
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 409 o 500 según el tipo de excepción
     */
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, IncorrectResultSizeDataAccessException.class })
    public ResponseEntity<ErrorResponse> handleOptimisticLockAndDataIntegrity(
            RuntimeException ex, HttpServletRequest request) {
        if (ex instanceof ObjectOptimisticLockingFailureException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse(409, "CONCURRENCY_CONFLICT",
                            "El recurso fue modificado por otro usuario. Recargue e intente nuevamente.",
                            request.getRequestURI(), System.currentTimeMillis(), null));
        }
        log.error("M\u00faltiples resultados inesperados en consulta \u2014 posibles datos duplicados", ex);
        return ResponseEntity.internalServerError().body(
                new ErrorResponse(500, "DATA_INTEGRITY_ERROR",
                        "Se encontraron m\u00faltiples registros inesperados. Contacte al administrador.",
                        request.getRequestURI(), System.currentTimeMillis(), null));
    }

    /**
     * Maneja excepciones de límite de tasa excedido ({@link RateLimitExceededException}).
     * Retorna error 429 con headers Retry-After y X-RateLimit-Reset.
     *
     * @param ex      excepción de límite de tasa
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 429 y headers de reintento
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        return ResponseEntity.status(429)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .header("X-RateLimit-Reset", String.valueOf(ex.getResetTime()))
                .body(new ErrorResponse(429, "TOO_MANY_REQUESTS", ex.getMessage(),
                        request.getRequestURI(), System.currentTimeMillis(), null));
    }

    /**
     * Maneja excepciones de solicitud asíncrona no utilizable ({@link AsyncRequestNotUsableException}).
     * Registra mensaje de debug y retorna error 499 (cliente desconectado).
     *
     * @param ex excepción de solicitud asíncrona no utilizable
     * @return ResponseEntity con error 499 y cuerpo vacío
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        log.debug("Cliente SSE desconectado: {}", ex.getMessage());
        return ResponseEntity.status(499).build();
    }

    /**
     * Maneja excepciones genéricas no capturadas ({@link Exception}).
     * Registra el error en log y retorna error 500.
     * Para solicitudes SSE retorna error 500 con cuerpo vacío.
     *
     * @param ex      excepción genérica inesperada
     * @param request solicitud HTTP actual
     * @return ResponseEntity con error 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado", ex);
        if (request.getRequestURI().contains("/api/sse")) {
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.internalServerError().body(
                new ErrorResponse(500, "INTERNAL_SERVER_ERROR", "Ocurri\u00f3 un error inesperado",
                        request.getRequestURI(), System.currentTimeMillis(), null));
    }
}
