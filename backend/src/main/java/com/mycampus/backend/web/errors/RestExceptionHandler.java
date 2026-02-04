package com.mycampus.backend.web.errors;

import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.exceptions.StructureAcademiqueException;
import com.mycampus.backend.exceptions.UtilisateursException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
 import jakarta.servlet.http.HttpServletRequest;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    private Map<String, Object> construireBody(HttpStatus status,
                                               String erreur,
                                               String message,
                                               String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("erreur", erreur);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    @ExceptionHandler(StructureAcademiqueException.class)
    public ResponseEntity<Object> handleStructureAcademiqueException(StructureAcademiqueException ex, HttpServletRequest  request) {
        log.error("Structure académique exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requête invalide",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EtudiantException.class)
    public ResponseEntity<Object> handleEtudiantException(EtudiantException ex, HttpServletRequest  request) {

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requête invalide",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(UtilisateursException.class)
    public ResponseEntity<Object> handleUtilisateursException(UtilisateursException ex, HttpServletRequest  request) {
        log.error("Utilisateurs exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requête invalide",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest  request) {
        log.warn("Erreur de validation @RequestBody: {}", ex.getMessage());

        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                details.put(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Validation des données",
                "Les données fournies ne sont pas valides",
                request.getRequestURI()
        );
        body.put("details", details);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest  request) {
        log.warn("Erreur de validation @RequestParam/@PathVariable: {}", ex.getMessage());

        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String chemin = v.getPropertyPath().toString();
            String cle = chemin.contains(".") ? chemin.substring(chemin.lastIndexOf('.') + 1) : chemin;
            details.put(cle, v.getMessage());
        });

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Validation des paramètres",
                "Les paramètres fournis ne sont pas valides",
                request.getRequestURI()
        );
        body.put("details", details);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, HttpServletRequest  request) {
        log.error("Generic exception: {}", ex.getMessage(), ex);

        Map<String, Object> body = construireBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne",
                "Erreur interne du serveur",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    // erreurs DB -> 409 Conflict

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                               HttpServletRequest request) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.CONFLICT,
                "Conflit de données",
                "Une contrainte de la base de données a été violée (doublon ou référence invalide)",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
