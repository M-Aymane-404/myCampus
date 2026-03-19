package com.mycampus.backend.metier.administrateur.PhotoStorage;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.metier.security.LienFichierSecuriseService;
import com.mycampus.backend.metier.security.ValidationFichierSecuriseService;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PhotoStorageMetierImpl implements PhotoStorageMetier {

    @Value("${app.files.base-dir:uploads}")
    private String baseDir;

    private final ValidationFichierSecuriseService validationFichierSecuriseService;
    private final LienFichierSecuriseService lienFichierSecuriseService;

    @Override
    public FichierStockerDTO enregistrerPhoto(MultipartFile file) {
        ValidationFichierSecuriseService.FichierValide fichierValide = validationFichierSecuriseService.valider(
                file,
                java.util.Set.of(
                        ValidationFichierSecuriseService.FormatFichierAutorise.JPEG,
                        ValidationFichierSecuriseService.FormatFichierAutorise.PNG
                ),
                5 * 1024 * 1024L
        );

        String filename = UUID.randomUUID() + fichierValide.extension();

        Path dir = Paths.get(baseDir).toAbsolutePath().normalize().resolve("usersPhotos").normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename).normalize();
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("Chemin de photo invalide");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }

        String url = lienFichierSecuriseService.construireUrlPhoto(filename);
        return new FichierStockerDTO(filename, url);
    }

    @Override
    public void supprimerPhotoUtilisateurSiExsist(String photoNom) {
        if (!StringUtils.hasText(photoNom)) return;

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path photoDir = basePath.resolve("usersPhotos").normalize();
        Path path = photoDir.resolve(photoNom).normalize();

        if (!path.startsWith(photoDir)) {
            throw new IllegalArgumentException("Chemin de photo invalide");
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // on log seulement, pas bloquant
        }
    }
}
