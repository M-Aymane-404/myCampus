package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<ModuleSortieDTO> creerModule(@RequestBody ModuleEntrerDTO moduleDTO) {
        ModuleSortieDTO result = metier.creerModule(moduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleSortieDTO> modifierModule(
            @PathVariable Long id, 
            @RequestBody ModuleEntrerDTO moduleDTO) {
        ModuleSortieDTO result = metier.modifierModule(id, moduleDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleSortieDTO> trouverModuleParId(@PathVariable Long id) {
        ModuleSortieDTO result = metier.trouverModuleParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ModuleSortieDTO>> listerTousLesModules() {
        List<ModuleSortieDTO> result = metier.listerTousLesModules();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filiere/{filiereId}")
    public ResponseEntity<List<ModuleSortieDTO>> trouverModulesParFiliere(@PathVariable Long filiereId) {
        List<ModuleSortieDTO> result = metier.trouverModulesParFiliere(filiereId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerModule(@PathVariable Long id) {
        metier.supprimerModule(id);
        return ResponseEntity.noContent().build();
    }
}
