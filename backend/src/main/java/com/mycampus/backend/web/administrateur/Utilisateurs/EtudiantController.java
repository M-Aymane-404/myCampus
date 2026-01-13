package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/etudiants")
@RequiredArgsConstructor
@Slf4j
public class EtudiantController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;


    @PostMapping("")
    public ResponseEntity<EtudiantSortieDTO> creerEtudiant(@RequestBody EtudiantEntrerDTO etudiantEntrerDTO) {

        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.creerEtudiant(etudiantEntrerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(etudiant);

    }

    @PutMapping("/{id}")
    public ResponseEntity<EtudiantSortieDTO> modifierEtudiant(@PathVariable Long id, @RequestBody EtudiantEntrerDTO etudiantEntrerDTO) {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.modifierEtudiant(id, etudiantEntrerDTO);
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtudiantSortieDTO> trouverEtudiantParId(@PathVariable Long id)  {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.trouverEtudiantParId(id);
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("/apogee/{apogee}")
    public ResponseEntity<EtudiantSortieDTO> trouverEtudiantParPpr(@PathVariable String apogee)  {
        EtudiantSortieDTO etudiant = gestionUtilisateursMetier.trouverEtudiantParApogee(apogee);
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("")
    public ResponseEntity<List<EtudiantSortieDTO>> listerTousLesEtudiants(){
        List<EtudiantSortieDTO> etudiants = gestionUtilisateursMetier.listerTousLesEtudiants();
        return ResponseEntity.ok(etudiants);
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerEtudiant(@PathVariable Long id) {
        gestionUtilisateursMetier.activerEtudiant(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverEtudiant(@PathVariable Long id) {
        gestionUtilisateursMetier.desactiverEtudiant(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerEtudiant(@PathVariable Long id)  {
        gestionUtilisateursMetier.supprimerEtudiant(id);
        return ResponseEntity.noContent().build();
    }


}
