package com.mycampus.backend.web.Securite;

import com.mycampus.backend.metier.security.AccesFichiersSecurisesMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/fichiers")
@RequiredArgsConstructor
public class FichierSecuriseController {

    private final AccesFichiersSecurisesMetier accesFichiersSecurisesMetier;

    @GetMapping("/photos/{photoNom}")
    public ResponseEntity<Resource> telechargerPhotoProfil(
            @AuthenticationPrincipal CustomUserDetails utilisateurConnecte,
            @PathVariable String photoNom
    ) {
        Resource resource = accesFichiersSecurisesMetier.chargerPhotoProfil(utilisateurConnecte, photoNom);
        return construireReponse(resource, true);
    }

    @GetMapping("/justificatifs/presences/{presenceId}")
    public ResponseEntity<Resource> telechargerJustificatifPresence(
            @AuthenticationPrincipal CustomUserDetails utilisateurConnecte,
            @PathVariable Long presenceId
    ) {
        Resource resource = accesFichiersSecurisesMetier.chargerJustificatifPresence(utilisateurConnecte, presenceId);
        return construireReponse(resource, false);
    }

    private ResponseEntity<Resource> construireReponse(Resource resource, boolean inline) {
        String nomFichier = StringUtils.hasText(resource.getFilename()) ? resource.getFilename() : "fichier";
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try {
            Path chemin = resource.getFile().toPath();
            String detecte = Files.probeContentType(chemin);
            if (StringUtils.hasText(detecte)) {
                contentType = detecte;
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + nomFichier + "\"")
                .body(resource);
    }
}
