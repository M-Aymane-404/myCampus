package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.MatiereSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/matieres")
@RequiredArgsConstructor
public class MatiereController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<MatiereSortieDTO> creerMatiere(@RequestBody MatiereEntrerDTO matiereDTO) {
        MatiereSortieDTO result = metier.creerMatiere(matiereDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatiereSortieDTO> modifierMatiere(
            @PathVariable Long id, 
            @RequestBody MatiereEntrerDTO matiereDTO) {
        MatiereSortieDTO result = metier.modifierMatiere(id, matiereDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatiereSortieDTO> trouverMatiereParId(@PathVariable Long id) {
        MatiereSortieDTO result = metier.trouverMatiereParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<MatiereSortieDTO>> listerToutesLesMatieres() {
        List<MatiereSortieDTO> result = metier.listerToutesLesMatieres();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<MatiereSortieDTO>> trouverMatieresParModule(@PathVariable Long moduleId) {
        List<MatiereSortieDTO> result = metier.trouverMatieresParModule(moduleId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerMatiere(@PathVariable Long id) {
        metier.supprimerMatiere(id);
        return ResponseEntity.noContent().build();
    }
}
