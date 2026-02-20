package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantJustificationPresenceDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.TypeMotif;
import com.mycampus.backend.metier.etudiant.motifDePresence.GestionDeMotifDePresenceMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class JustificationPresenceController {
    private final GestionDeMotifDePresenceMetier gestionDeMotifDePresenceMetier;

    @PreAuthorize("hasAuthority('ETUDIANT_INFORMATION_PERSONNEL_MODIFIER')")
    @PutMapping(value = "/justification/{presenceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> ajouterUneJustificationDEPresence(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long presenceId,
            @RequestParam("typeMotif") TypeMotif typeMotif,
            @RequestParam(value = "descriptionDeJustification", required = false) String description,
            @RequestPart(value = "motif", required = false) MultipartFile motif
    ) {
        EtudiantJustificationPresenceDTO dto = new EtudiantJustificationPresenceDTO();
        dto.setTypeMotif(typeMotif);
        dto.setDescriptionDeJustification(description);

        gestionDeMotifDePresenceMetier.ajouterJustification_Absence(user.getId(), motif, presenceId, dto);
        return ResponseEntity.noContent().build();
    }


}
