package com.mycampus.backend.web.administrateur.imports;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/import/template")
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class ImportTemplateController {

    @GetMapping("/{type}")
    public ResponseEntity<Resource> telechargerTemplate(@PathVariable String type) {
        String typeNormalise = type == null ? "" : type.trim().toLowerCase();

        String nomFichier;
        MediaType mediaType;

        switch (typeNormalise) {
            case "xlsx" -> {
                nomFichier = "etudiants_import.xlsx";
                mediaType = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
            }
            case "csv" -> {
                nomFichier = "import_etudiants.csv";
                mediaType = MediaType.parseMediaType("text/csv");
            }
            default -> throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Type de template invalide. Utiliser 'xlsx' ou 'csv'."
            );
        }

        ClassPathResource resource = new ClassPathResource("insertEtudiant/" + nomFichier);
        if (!resource.exists()) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Template introuvable: " + nomFichier
            );
        }

        try {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(resource.contentLength())
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(nomFichier).build().toString()
                    )
                    .body(resource);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Erreur lors de la lecture du template.",
                    e
            );
        }
    }
}
