package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/affectations")
@RequiredArgsConstructor
public class AffectationEnseignementController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<AffectationEnseignementSortieDTO> creerAffectationEnseignement(@RequestBody AffectationEnseignementEntrerDTO affectationDTO) {
        AffectationEnseignementSortieDTO result = metier.creerAffectationEnseignement(affectationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AffectationEnseignementSortieDTO> modifierAffectationEnseignement(
            @PathVariable Long id, 
            @RequestBody AffectationEnseignementEntrerDTO affectationDTO) {
        AffectationEnseignementSortieDTO result = metier.modifierAffectationEnseignement(id, affectationDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AffectationEnseignementSortieDTO> trouverAffectationEnseignementParId(@PathVariable Long id) {
        AffectationEnseignementSortieDTO result = metier.trouverAffectationParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerToutesLesAffectations() {
        List<AffectationEnseignementSortieDTO> result = metier.listerToutesLesAffectations();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professeur/{ppr}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParProfesseur(@PathVariable String ppr) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParProfesseur(ppr);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParClasse(@PathVariable Long classeId) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParClasse(classeId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/matiere/{matiereId}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParMatiere(@PathVariable Long matiereId) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParMatiere(matiereId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerAffectationEnseignement(@PathVariable Long id) {
        metier.activerAffectationEnseignement(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverAffectationEnseignement(@PathVariable Long id) {
        metier.desactiverAffectationEnseignement(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAffectationEnseignement(@PathVariable Long id) {
        metier.supprimerAffectationEnseignement(id);
        return ResponseEntity.noContent().build();
    }
}
