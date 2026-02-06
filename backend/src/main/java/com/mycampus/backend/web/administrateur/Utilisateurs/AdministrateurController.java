package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AdministrateurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/administrateurs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class AdministrateurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<AdministrateurSortieDTO> creerAdministrateur(
            @Valid @RequestBody AdministrateurEntrerDTO administrateurEntrerDTO) {
        AdministrateurSortieDTO created = gestionUtilisateursMetier.creerAdministrateur(administrateurEntrerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{ppr}")
    public ResponseEntity<AdministrateurSortieDTO> trouverAdministrateurParPpr(@PathVariable String ppr) {
        AdministrateurSortieDTO administrateur = gestionUtilisateursMetier.trouverAdministrateurParPpr(ppr);
        return ResponseEntity.ok(administrateur);
    }

    @GetMapping("")
    public ResponseEntity<List<AdministrateurSortieDTO>> listerTousLesAdministrateurs() {
        List<AdministrateurSortieDTO> administrateurs = gestionUtilisateursMetier.listerTousLesAdministrateurs();
        return ResponseEntity.ok(administrateurs);
    }

    @PutMapping("/{ppr}")
    public ResponseEntity<AdministrateurSortieDTO> modifierAdministrateur(
            @PathVariable String ppr,
            @Valid @RequestBody AdministrateurModificationDTO administrateurModificationDTO) {
        AdministrateurSortieDTO updated = gestionUtilisateursMetier.modifierAdministrateur(ppr, administrateurModificationDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{ppr}/activer")
    public ResponseEntity<Void> activerAdministrateur(@PathVariable String ppr) {
        gestionUtilisateursMetier.activerAdministrateur(ppr);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ppr}/desactiver")
    public ResponseEntity<Void> desactiverAdministrateur(@PathVariable String ppr) {
        gestionUtilisateursMetier.desactiverAdministrateur(ppr);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{ppr}")
    public ResponseEntity<Void> supprimerAdministrateur(@PathVariable String ppr) {
        gestionUtilisateursMetier.supprimerAdministrateur(ppr);
        return ResponseEntity.noContent().build();
    }

}
