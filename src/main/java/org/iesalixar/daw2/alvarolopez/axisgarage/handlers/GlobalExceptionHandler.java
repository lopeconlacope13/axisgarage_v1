package org.iesalixar.daw2.alvarolopez.axisgarage.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST de Axis Garage.
 * Centraliza el tratamiento de errores de validación Bean Validation,
 * errores de negocio y errores inesperados del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura los errores de validación Bean Validation generados por @Valid.
     * Devuelve un mapa con el nombre del campo y el mensaje de error correspondiente.
     *
     * @param ex excepción lanzada cuando uno o más campos @Valid fallan la validación.
     * @return ResponseEntity con código 400 y el mapa de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errores.put(error.getField(), error.getDefaultMessage())
        );
        logger.warn("Error de validación: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    /**
     * Captura excepciones de lógica de negocio lanzadas como IllegalArgumentException.
     * Devuelve un mensaje de error con código 400.
     *
     * @param ex excepción de argumento ilegal.
     * @return ResponseEntity con código 400 y el mensaje de error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Error de negocio: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Captura cualquier excepción no controlada por los handlers anteriores.
     * Devuelve un mensaje genérico con código 500 para no exponer detalles internos.
     *
     * @param ex excepción inesperada del servidor.
     * @return ResponseEntity con código 500 y mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Error interno del servidor: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor. Por favor, contacte con el administrador.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
