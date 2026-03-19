package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.presences.GestionPresencesProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/professeur/seances")
@RequiredArgsConstructor
public class PresencesProfesseurController {

    private final GestionPresencesProfesseurMetier presenceMetier;
    private  final GestionUtilisateurConnecteMetier UtilisateurMetier;


    @GetMapping("/{seanceId}/presences")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_LIRE')")
    public ResponseEntity<FeuilleAppelPresenceSortieDTO> chargerFeuille(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {

        String professeurPpr =  UtilisateurMetier.trouverProfesseurPprParId(user.getId());
        return ResponseEntity.ok(presenceMetier.chargerFeuilleAppelPresence(professeurPpr, seanceId));
    }
    @PutMapping("/{seanceId}/presences")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_MODIFIER')")
    public ResponseEntity<LignePresenceSortieDTO> saisirPresences(
           @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @Valid @RequestBody PresenceEntrerDTO dto
            ) throws SeancesProfesseurException {

        String professeurPpr =  UtilisateurMetier.trouverProfesseurPprParId(user.getId());
        LignePresenceSortieDTO result =
                presenceMetier.saisirPresences(professeurPpr, seanceId,dto);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{seanceId}/presences/{presenceId}/valider-justificatif")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_MODIFIER')")
    public ResponseEntity<LignePresenceSortieDTO> validerJustificatif(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @PathVariable Long presenceId
    ) throws SeancesProfesseurException {
        String professeurPpr = UtilisateurMetier.trouverProfesseurPprParId(user.getId());
        return ResponseEntity.ok(presenceMetier.validerJustificatif(professeurPpr, seanceId, presenceId));
    }

    @PatchMapping("/{seanceId}/presences/{presenceId}/refuser-justificatif")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_MODIFIER')")
    public ResponseEntity<LignePresenceSortieDTO> refuserJustificatif(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @PathVariable Long presenceId
    ) throws SeancesProfesseurException {
        String professeurPpr = UtilisateurMetier.trouverProfesseurPprParId(user.getId());
        return ResponseEntity.ok(presenceMetier.refuserJustificatif(professeurPpr, seanceId, presenceId));
    }

    @GetMapping("/{seanceId}/presences/{presenceId}/justificatif")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_LIRE')")
    public ResponseEntity<Resource> telechargerJustificatif(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @PathVariable Long presenceId
    ) throws SeancesProfesseurException {
        String professeurPpr = UtilisateurMetier.trouverProfesseurPprParId(user.getId());
        Resource resource = presenceMetier.telechargerJustificatif(professeurPpr, seanceId, presenceId);

        String filename = StringUtils.hasText(resource.getFilename()) ? resource.getFilename() : "justificatif";
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try {
            Path filePath = resource.getFile().toPath();
            String probe = Files.probeContentType(filePath);
            if (StringUtils.hasText(probe)) {
                contentType = probe;
            }
        } catch (IOException ignored) {
            // fallback to default
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
