package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.ProfesseurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/professeurs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class ProfesseurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<ProfesseurSortieDTO> creerProfesseur(@Valid @RequestBody ProfesseurEntrerDTO professeurEntrerDTO) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.creerProfesseur(professeurEntrerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(professeur);
    }

    @GetMapping("/{ppr}")
    public ResponseEntity<ProfesseurSortieDTO> trouverProfesseurParPpr(@PathVariable String ppr) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.trouverProfesseurParPpr(ppr);
        return ResponseEntity.ok(professeur);
    }

    @GetMapping("")
    public ResponseEntity<List<ProfesseurSortieDTO>> listerTousLesProfesseurs() {
        List<ProfesseurSortieDTO> professeurs = gestionUtilisateursMetier.listerTousLesProfesseurs();
        return ResponseEntity.ok(professeurs);
    }

    @PutMapping("/{ppr}")
    public ResponseEntity<ProfesseurSortieDTO> modifierProfesseur(@PathVariable String ppr, @Valid @RequestBody ProfesseurModificationDTO professeurModificationDTO) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.modifierProfesseur(ppr, professeurModificationDTO);
        return ResponseEntity.ok(professeur);
    }

    @PatchMapping("/{ppr}/activer")
    public ResponseEntity<Void> activerProfesseur(@PathVariable String ppr) {
        gestionUtilisateursMetier.activerProfesseur(ppr);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ppr}/desactiver")
    public ResponseEntity<Void> desactiverProfesseur(@PathVariable String ppr) {
        gestionUtilisateursMetier.desactiverProfesseur(ppr);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{ppr}")
    public ResponseEntity<Void> supprimerProfesseur(@PathVariable String ppr) {
        gestionUtilisateursMetier.supprimerProfesseur(ppr);
        return ResponseEntity.noContent().build();
    }
}
