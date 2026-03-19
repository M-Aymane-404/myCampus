package com.mycampus.backend.web.errors;

import com.mycampus.backend.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

 import jakarta.servlet.http.HttpServletRequest;


import java.time.Instant;
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
        body.put("timestamp", LocalDateTime.now().toString());
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

    @ExceptionHandler(SecuriteException.class)
    public ResponseEntity<Object> handleSecuriteException (SecuriteException ex, HttpServletRequest  request) {
        log.error("Securite exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requête invalide",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(PresenceException.class)
    public ResponseEntity<Object> handlePresenceException(PresenceException ex, HttpServletRequest  request) {
        log.error("Presence exception: {}", ex.getMessage());

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

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<Object> handleNotificationException(NotificationException ex, HttpServletRequest request) {
        log.error("Notification exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requete invalide",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(FilesException.class)
    public ResponseEntity<Object> handleFilesException(FilesException ex, HttpServletRequest request) {
        log.error("Files exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requete invalide",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NotesProfesseurException.class)
    public ResponseEntity<Object> handleNotesProfesseurException(NotesProfesseurException ex, HttpServletRequest request) {
        log.error("Notes professeur exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.BAD_REQUEST,
                "Requete invalide",
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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication exception: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.UNAUTHORIZED,
                "Non authentifie",
                "Email ou mot de passe incorrect",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(TropDeTentativesException.class)
    public ResponseEntity<Object> handleTropDeTentatives(TropDeTentativesException ex, HttpServletRequest request) {
        log.warn("Rate limit connexion: {}", ex.getMessage());

        Map<String, Object> body = construireBody(
                HttpStatus.TOO_MANY_REQUESTS,
                "Trop de requetes",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
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


    //Prof

    @ExceptionHandler(SeancesProfesseurException.class)
    public ResponseEntity<Object> handleSeancesProfesseurException(SeancesProfesseurException ex, WebRequest request) {
        log.error("Séances professeur exception: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Requête invalide");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSize(MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("Upload trop volumineux: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Fichier trop volumineux");
        body.put("message", "La taille du fichier dépasse la limite autorisée");
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipart(MultipartException ex, WebRequest request) {
        log.warn("Erreur multipart: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erreur", "Erreur upload");
        body.put("message", "Le fichier envoyé n'est pas valide ou la requête multipart est incorrecte");
        body.put("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }
    @ExceptionHandler({ org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.authorization.AuthorizationDeniedException.class })
    public ResponseEntity<?> handleAccessDenied(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 403,
                "error", "Forbidden",
                "message", "Access Denied",
                "path", req.getRequestURI()
        ));
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
