package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.FiliereModificationDTO;
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

    @GetMapping("/{codeFiliere}")
    public ResponseEntity<FiliereSortieDTO> trouverFiliereParCode(@PathVariable String codeFiliere) {
        FiliereSortieDTO result = metier.trouverFiliereParCode(codeFiliere);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<FiliereSortieDTO>> listerToutesLesFilieres() {
        List<FiliereSortieDTO> result = metier.listerToutesLesFilieres();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/departement/{codeDepartement}")
    public ResponseEntity<List<FiliereSortieDTO>> trouverFilieresParDepartement(@PathVariable String codeDepartement) {
        List<FiliereSortieDTO> result = metier.trouverFilieresParDepartement(codeDepartement);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{codeFiliere}")
    public ResponseEntity<FiliereSortieDTO> modifierFiliere(
            @PathVariable String codeFiliere, 
            @RequestBody FiliereModificationDTO filiereDTO) {
        FiliereSortieDTO result = metier.modifierFiliere(codeFiliere, filiereDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeFiliere}")
    public ResponseEntity<Void> supprimerFiliere(@PathVariable String codeFiliere) {
        metier.supprimerFiliere(codeFiliere);
        return ResponseEntity.noContent().build();
    }
}
