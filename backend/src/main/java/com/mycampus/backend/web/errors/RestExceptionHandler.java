package com.mycampus.backend.web.errors;

import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.exceptions.StructureAcademiqueException;
import com.mycampus.backend.exceptions.UtilisateursException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(StructureAcademiqueException.class)
    public ResponseEntity<Object> handleStructureAcademiqueException(StructureAcademiqueException ex, WebRequest request) {
        log.error("Structure académique exception: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Requête invalide");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EtudiantException.class)
    public ResponseEntity<Object> handleEtudiantException(EtudiantException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Requête invalide");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(UtilisateursException.class)
    public ResponseEntity<Object> handleUtilisateursException(UtilisateursException ex, WebRequest request) {
        log.error("Utilisateurs exception: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Requête invalide");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Erreur de validation @RequestBody: {}", ex.getMessage());

        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                details.put(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Validation des données");
        body.put("message", "Les données fournies ne sont pas valides");
        body.put("details", details);
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.warn("Erreur de validation @RequestParam/@PathVariable: {}", ex.getMessage());

        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String chemin = v.getPropertyPath().toString();
            String cle = chemin.contains(".") ? chemin.substring(chemin.lastIndexOf('.') + 1) : chemin;
            details.put(cle, v.getMessage());
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Validation des données");
        body.put("message", "Les paramètres fournis ne sont pas valides");
        body.put("details", details);
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        log.error("Generic exception: {}", ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("erreur", "Erreur interne");
        body.put("message", "Erreur interne du serveur");
        body.put("path", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
