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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
