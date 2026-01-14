package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.FiliereSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<FiliereSortieDTO> creerFiliere(@RequestBody FiliereEntrerDTO filiereDTO) {
        FiliereSortieDTO result = metier.creerFiliere(filiereDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiliereSortieDTO> modifierFiliere(
            @PathVariable Long id, 
            @RequestBody FiliereEntrerDTO filiereDTO) {
        FiliereSortieDTO result = metier.modifierFiliere(id, filiereDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiliereSortieDTO> trouverFiliereParId(@PathVariable Long id) {
        FiliereSortieDTO result = metier.trouverFiliereParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<FiliereSortieDTO>> listerToutesLesFilieres() {
        List<FiliereSortieDTO> result = metier.listerToutesLesFilieres();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/departement/{departementId}")
    public ResponseEntity<List<FiliereSortieDTO>> trouverFilieresParDepartement(@PathVariable Long departementId) {
        List<FiliereSortieDTO> result = metier.trouverFilieresParDepartement(departementId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerFiliere(@PathVariable Long id) {
        metier.supprimerFiliere(id);
        return ResponseEntity.noContent().build();
    }
}
