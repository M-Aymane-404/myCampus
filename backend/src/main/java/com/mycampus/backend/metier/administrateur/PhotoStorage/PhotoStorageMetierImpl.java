package com.mycampus.backend.metier.administrateur.PhotoStorage;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
@Service
@Transactional
@RequiredArgsConstructor
public class PhotoStorageMetierImpl implements PhotoStorageMetier {
    private static final Set<String> ALLOWED = Set.of("image/png", "image/jpeg", "image/webp");

    @Value("${app.files.base-dir:uploads}")
    private String baseDir;

    @Override
    public FichierStockerDTO enregistrerPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo obligatoire");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new IllegalArgumentException("Format image non supporté (png, jpeg, webp)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La photo ne doit pas dépasser 5MB");
        }

        String ext = extensionFromContentType(file.getContentType());
        String filename = UUID.randomUUID() + ext;

        Path dir = Paths.get(baseDir, "usersPhotos");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }

        String url = "/files/usersPhotos/" + filename; // URL publique
        return new FichierStockerDTO(filename, url);
    }

    @Override
    public void supprimerPhotoUtilisateurSiExsist(String photoNom) {
        if (!StringUtils.hasText(photoNom)) return;

        Path path = Paths.get(baseDir, "usersPhotos", photoNom);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // on log seulement, pas bloquant
        }
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }


}

