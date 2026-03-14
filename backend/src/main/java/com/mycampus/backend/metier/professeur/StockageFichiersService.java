package com.mycampus.backend.metier.professeur;

import com.mycampus.backend.exceptions.NotesProfesseurException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class StockageFichiersService {
    private final Path baseDir = Paths.get("uploads");

    public String sauvegarderPdf(MultipartFile fichier, String sousDossier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new NotesProfesseurException("Fichier obligatoire.");
        }
        String ct = fichier.getContentType();
        if (ct == null || !ct.toLowerCase().contains("pdf")) {
            throw new NotesProfesseurException("Seul le format PDF est autorisé.");
        }

        try {
            Path dir = baseDir.resolve(sousDossier).normalize();
            Files.createDirectories(dir);

            String original = fichier.getOriginalFilename() == null ? "fichier.pdf" : fichier.getOriginalFilename();
            original = original.replaceAll("[^a-zA-Z0-9._-]", "_");

            String nom = UUID.randomUUID() + "_" + original;
            Path cible = dir.resolve(nom).normalize();

            if (!cible.startsWith(dir)) {
                throw new NotesProfesseurException("Chemin de fichier invalide.");
            }

            Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);
            return cible.toString();
        } catch (IOException e) {
            throw new NotesProfesseurException("Erreur lors de la sauvegarde du fichier.");
        }
    }

    public Resource chargerCommeResource(String chemin) {
        if (chemin == null || chemin.isBlank()) {
            throw new NotesProfesseurException("Aucun fichier disponible.");
        }
        try {
            Path p = Paths.get(chemin).normalize();
            if (!Files.exists(p)) {
                throw new NotesProfesseurException("Fichier introuvable.");
            }
            return new UrlResource(p.toUri());
        } catch (Exception e) {
            throw new NotesProfesseurException("Erreur lors de la lecture du fichier.");
        }
    }

    public void supprimerSilencieusement(String chemin) {
        if (chemin == null || chemin.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(chemin).normalize());
        } catch (Exception ignored) {
        }
    }
}
