package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.presences.GestionPresencesProfesseurMetier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professeur/seances")
@RequiredArgsConstructor
public class PresencesProfesseurController {

    private final GestionPresencesProfesseurMetier metier;
    private final GestionPresencesProfesseurMetier utilisateurMetier;

    /*
        📄 1) Préparer / Lister la feuille de présence d’une séance
        Retourne tous les étudiants de la classe avec leur statut actuel.
    */
/*
    @GetMapping("/{seanceId}/presences")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_LIRE')")
    public ResponseEntity<List<PresenceSortieDTO>> listerPresencesSeance(
            Authentication authentication,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {

         String professeurPpr = utilisateurMetier.TrouverProfesseurPprParemail(authentication.getName());

        List<PresenceSortieDTO> result =
                metier.listerPresencesParSeance(professeurPpr, seanceId);

        return ResponseEntity.ok(result);
    }

    /*
        ✍️ 2) Saisir / Mettre à jour les présences d’une séance
        Le frontend envoie uniquement la liste des étudiants + statut.
    */

    @PutMapping("/{seanceId}/presences")
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_MODIFIER')")
    public ResponseEntity<LignePresenceSortieDTO> saisirPresences(
            Authentication authentication,
            @PathVariable Long seanceId,
            @Valid @RequestBody PresenceEntrerDTO dto
            ) throws SeancesProfesseurException {

        String email = authentication.getName();
        String professeurPpr = utilisateurMetier.TrouverProfesseurPprParemail(email);

        LignePresenceSortieDTO result =
                metier.saisirPresences(professeurPpr, seanceId,dto);

        return ResponseEntity.ok(result);
    }
}
