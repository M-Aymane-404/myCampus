package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/professeurs")
@RequiredArgsConstructor
public class ProfesseurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<ProfesseurSortieDTO> creerProfesseur(@RequestBody ProfesseurEntrerDTO professeurEntrerDTO) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.creerProfesseur(professeurEntrerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(professeur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesseurSortieDTO> modifierProfesseur(@PathVariable Long id, @RequestBody ProfesseurEntrerDTO professeurEntrerDTO) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.modifierProfesseur(id, professeurEntrerDTO);
        return ResponseEntity.ok(professeur);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesseurSortieDTO> trouverProfesseurParId(@PathVariable Long id) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.trouverProfesseurParId(id);
        return ResponseEntity.ok(professeur);
    }

    @GetMapping("/ppr/{ppr}")
    public ResponseEntity<ProfesseurSortieDTO> trouverProfesseurParPpr(@PathVariable String ppr) {
        ProfesseurSortieDTO professeur = gestionUtilisateursMetier.trouverProfesseurParPpr(ppr);
        return ResponseEntity.ok(professeur);
    }

    @GetMapping("")
    public ResponseEntity<List<ProfesseurSortieDTO>> listerTousLesProfesseurs() {
        List<ProfesseurSortieDTO> professeurs = gestionUtilisateursMetier.listerTousLesProfesseurs();
        return ResponseEntity.ok(professeurs);
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerProfesseur(@PathVariable Long id) {
        gestionUtilisateursMetier.activerProfesseur(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverProfesseur(@PathVariable Long id) {
        gestionUtilisateursMetier.desactiverProfesseur(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerProfesseur(@PathVariable Long id) {
        gestionUtilisateursMetier.supprimerProfesseur(id);
        return ResponseEntity.noContent().build();
    }
}
