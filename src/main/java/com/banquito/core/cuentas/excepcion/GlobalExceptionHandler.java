package com.banquito.core.cuentas.excepcion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntidadNoEncontradaExcepcion.class)
    public ResponseEntity<Map<String, Object>> handleEntidadNoEncontrada(EntidadNoEncontradaExcepcion ex) {
        log.error("Entidad no encontrada: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "ENTIDAD_NO_ENCONTRADA");
        response.put("mensaje", ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("status", 404);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CrearEntidadExcepcion.class)
    public ResponseEntity<Map<String, Object>> handleCrearEntidad(CrearEntidadExcepcion ex) {
        log.error("Error de validación: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "VALIDACION_FALLIDA");
        response.put("mensaje", ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("status", 400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Errores de validación en campos: {}", ex.getMessage());
        
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "CAMPOS_INVALIDOS");
        response.put("mensaje", "Errores en los datos enviados");
        response.put("errores", errores);
        response.put("timestamp", Instant.now().toString());
        response.put("status", 400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Error en cola de mensajes: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "ERROR_COLA");
        response.put("mensaje", "Error enviando transacción a cola: " + ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("status", 500);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "NO_ENCONTRADO");
        response.put("mensaje", ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("status", 404);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "ERROR_INTERNO");
        response.put("mensaje", "Error interno del servidor");
        response.put("timestamp", Instant.now().toString());
        response.put("status", 500);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
