package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.MatiereModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.MatiereSortieAvecProfDTO;
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

    @GetMapping("/{codeMatiere}")
    public ResponseEntity<MatiereSortieDTO> trouverMatiereParCode(@PathVariable String codeMatiere) {
        MatiereSortieDTO result = metier.trouverMatiereParCode(codeMatiere);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<MatiereSortieDTO>> listerToutesLesMatieres() {
        List<MatiereSortieDTO> result = metier.listerToutesLesMatieres();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/module/{codeModule}")
    public ResponseEntity<List<MatiereSortieDTO>> trouverMatieresParModule(@PathVariable String codeModule) {
        List<MatiereSortieDTO> result = metier.trouverMatieresParModule(codeModule);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/module/{codeModule}/classe/{codeClasse}")
    public ResponseEntity<List<MatiereSortieAvecProfDTO>> trouverMatieresParModuleEtClasse(@PathVariable String codeModule, @PathVariable String codeClasse) {
        List<MatiereSortieAvecProfDTO> result = metier.trouverMatieresParProfAffecterEtModule(codeModule,codeClasse);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{codeMatiere}")
    public ResponseEntity<MatiereSortieDTO> modifierMatiere(
            @PathVariable String codeMatiere, 
            @RequestBody MatiereModificationDTO matiereDTO) {
        MatiereSortieDTO result = metier.modifierMatiere(codeMatiere, matiereDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeMatiere}")
    public ResponseEntity<Void> supprimerMatiere(@PathVariable String codeMatiere) {
        metier.supprimerMatiere(codeMatiere);
        return ResponseEntity.noContent().build();
    }
}
