package com.mycampus.backend.metier.professeur;

import com.mycampus.backend.exceptions.NotesProfesseurException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StockageFichiersService {
    private final Path basePath;

    public StockageFichiersService(@Value("${app.files.base-dir:uploads}") String baseDir) {
        this.basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException ex) {
            throw new NotesProfesseurException("Impossible de preparer le stockage des fichiers.");
        }
    }

    public String sauvegarderPdf(MultipartFile fichier, String sousDossier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new NotesProfesseurException("Fichier obligatoire.");
        }
        String ct = fichier.getContentType();
        if (ct == null || !ct.toLowerCase().contains("pdf")) {
            throw new NotesProfesseurException("Seul le format PDF est autorise.");
        }

        try {
            Path dir = resolveSubdirectory(sousDossier);
            Files.createDirectories(dir);

            String original = fichier.getOriginalFilename() == null ? "fichier.pdf" : fichier.getOriginalFilename();
            original = original.replaceAll("[^a-zA-Z0-9._-]", "_");

            String nom = UUID.randomUUID() + "_" + original;
            Path cible = dir.resolve(nom).normalize();

            ensureInsideBase(cible);
            Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);

            return relativize(cible);
        } catch (IOException e) {
            throw new NotesProfesseurException("Erreur lors de la sauvegarde du fichier.");
        }
    }

    public Resource chargerCommeResource(String chemin) {
        Path fichier = resolveStoredPath(chemin);
        if (!Files.exists(fichier)) {
            throw new NotesProfesseurException("Fichier introuvable.");
        }
        try {
            return new UrlResource(fichier.toUri());
        } catch (Exception e) {
            throw new NotesProfesseurException("Erreur lors de la lecture du fichier.");
        }
    }

    public void supprimerSilencieusement(String chemin) {
        if (!StringUtils.hasText(chemin)) return;
        Path fichier;
        try {
            fichier = resolveStoredPath(chemin);
        } catch (NotesProfesseurException ex) {
            return;
        }
        try {
            Files.deleteIfExists(fichier);
        } catch (Exception ignored) {
        }
    }

    private Path resolveStoredPath(String chemin) {
        if (!StringUtils.hasText(chemin)) {
            throw new NotesProfesseurException("Aucun fichier disponible.");
        }
        try {
            String normalized = chemin.replace("\\", "/").replaceAll("^/+", "");
            Path candidate = normalized.isEmpty() ? Paths.get("") : Paths.get(normalized);
            Path resolved = basePath.resolve(candidate).normalize();

            ensureInsideBase(resolved);
            return resolved;
        } catch (InvalidPathException ex) {
            throw new NotesProfesseurException("Chemin de fichier invalide.");
        }
    }

    private Path resolveSubdirectory(String sousDossier) {
        if (!StringUtils.hasText(sousDossier)) {
            return basePath;
        }
        String normalized = sousDossier.trim().replace("\\", "/");
        normalized = normalized.replaceFirst("^/+", "").replaceAll("/+$", "");
        if (!StringUtils.hasText(normalized)) {
            return basePath;
        }

        try {
            Path sub = Paths.get(normalized);
            Path resolved = basePath.resolve(sub).normalize();
            ensureInsideBase(resolved);
            return resolved;
        } catch (InvalidPathException ex) {
            throw new NotesProfesseurException("Chemin de fichier invalide.");
        }
    }

    private void ensureInsideBase(Path path) {
        if (!path.startsWith(basePath)) {
            throw new NotesProfesseurException("Chemin de fichier invalide.");
        }
    }

    private String relativize(Path cible) {
        Path relative = basePath.relativize(cible);
        String normalized = relative.toString().replace("\\", "/").replaceAll("^/+", "");
        return normalized;
    }
}
