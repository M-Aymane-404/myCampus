package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/administrateurs")
@RequiredArgsConstructor
public class AdministrateurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<AdministrateurSortieDTO> creerAdministrateur(
            @RequestBody AdministrateurEntrerDTO administrateurEntrerDTO) {
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
            @RequestBody AdministrateurEntrerDTO administrateurEntrerDTO) {
        AdministrateurSortieDTO updated = gestionUtilisateursMetier.modifierAdministrateur(ppr, administrateurEntrerDTO);
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
