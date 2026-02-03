package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AffectationEnseignementModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/affectations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_STRUCTURE_GERER')")
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
            @RequestBody AffectationEnseignementModificationDTO affectationDTO) {
        AffectationEnseignementSortieDTO result = metier.modifierAffectationEnseignement(id, affectationDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AffectationEnseignementSortieDTO> trouverAffectationParId(@PathVariable Long id) {
        AffectationEnseignementSortieDTO result = metier.trouverAffectationParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerToutesLesAffectations() {
        List<AffectationEnseignementSortieDTO> result = metier.listerToutesLesAffectations();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professeur/{professeurPpr}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParProfesseur(@PathVariable String professeurPpr) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParProfesseur(professeurPpr);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classe/{codeClasse}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParClasse(@PathVariable String codeClasse) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParClasse(codeClasse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/matiere/{codeMatiere}")
    public ResponseEntity<List<AffectationEnseignementSortieDTO>> listerAffectationsParMatiere(@PathVariable String codeMatiere) {
        List<AffectationEnseignementSortieDTO> result = metier.listerAffectationsParMatiere(codeMatiere);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/activer/{id}")
    public ResponseEntity<Void> activerAffectationEnseignement(@PathVariable Long id) {
        metier.activerAffectationEnseignement(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/desactiver/{id}")
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
