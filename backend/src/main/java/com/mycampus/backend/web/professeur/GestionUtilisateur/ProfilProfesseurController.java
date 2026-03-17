package com.mycampus.backend.web.professeur.GestionUtilisateur;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantModificationEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.EtudiantSortieEtudiantDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.ProfModificationProfDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import com.mycampus.backend.metier.professeur.gestionUtilisateur.gestionProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
public class  ProfilProfesseurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;
    private final GestionUtilisateurConnecteMetier utilisateurConnecteMetier;
    private final gestionProfesseurMetier gestionProfesseurMetier;

    @GetMapping("/moi")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER') or hasAuthority('PROFESSEUR_EMPLOI_LIRE') or hasAuthority('PROFESSEUR_NOTES_GERER')")
    public ResponseEntity<ProfesseurSortieDTO> trouverProfesseurConnecte(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String ppr = utilisateurConnecteMetier.trouverProfesseurPprParId(user.getId());
        return ResponseEntity.ok(gestionUtilisateursMetier.trouverProfesseurParPpr(ppr));
    }

    @PreAuthorize("hasAuthority('PROFESSEUR_INFORMATION_PERSONNEL_MODIFIER')")

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FichierStockerDTO modifierPhotoProfil(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart("photo") MultipartFile photo
    ) {
        Utilisateur u = gestionUtilisateursMetier.changerPhotoProfil(user.getEmail(), photo);
        return new FichierStockerDTO(u.getPhotoNom(),u.getPhotoUrl());

    }


    @PreAuthorize("hasAuthority('PROFESSEUR_INFORMATION_PERSONNEL_MODIFIER')")
    @PutMapping("/moi/modifier")
    public ResponseEntity<ProfesseurSortieDTO> modifierEtudiant(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody ProfModificationProfDTO profModificationProfDTO) {
        ProfesseurSortieDTO etudiant = gestionProfesseurMetier.modifierProfesseur(user.getId(), profModificationProfDTO);
        return ResponseEntity.ok(etudiant);
    }
}
