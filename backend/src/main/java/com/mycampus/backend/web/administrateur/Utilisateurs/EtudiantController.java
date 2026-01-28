package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.EtudiantModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<EtudiantSortieDTO> creerEtudiant(@RequestBody @Valid EtudiantEntrerDTO etudiantEntrerDTO) {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.creerEtudiant(etudiantEntrerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(etudiant);
    }

    @GetMapping("/{apogee}")
    public ResponseEntity<EtudiantSortieDTO> trouverEtudiantParApogee(@PathVariable String apogee) {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.trouverEtudiantParApogee(apogee);
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("")
    public ResponseEntity<List<EtudiantSortieDTO>> listerTousLesEtudiants() {
        List<EtudiantSortieDTO> etudiants = gestionUtilisateursMetier.listerTousLesEtudiants();
        return ResponseEntity.ok(etudiants);
    }

    @PutMapping("/{apogee}")
    public ResponseEntity<EtudiantSortieDTO> modifierEtudiant(@PathVariable String apogee, @RequestBody @Valid EtudiantModificationDTO etudiantModificationDTO) {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.modifierEtudiant(apogee, etudiantModificationDTO);
        return ResponseEntity.ok(etudiant);
    }

    @PatchMapping("/{apogee}/activer")
    public ResponseEntity<Void> activerEtudiant(@PathVariable String apogee) {
        gestionUtilisateursMetier.activerEtudiant(apogee);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{apogee}/desactiver")
    public ResponseEntity<Void> desactiverEtudiant(@PathVariable String apogee) {
        gestionUtilisateursMetier.desactiverEtudiant(apogee);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{apogee}")
    public ResponseEntity<Void> supprimerEtudiant(@PathVariable String apogee) {
        gestionUtilisateursMetier.supprimerEtudiant(apogee);
        return ResponseEntity.noContent().build();
    }
}
