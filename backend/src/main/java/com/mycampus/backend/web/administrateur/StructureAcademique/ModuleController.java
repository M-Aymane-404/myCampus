package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.ModuleModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/modules")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_STRUCTURE_GERER')")
public class ModuleController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<ModuleSortieDTO> creerModule(@RequestBody ModuleEntrerDTO moduleDTO) {
        ModuleSortieDTO result = metier.creerModule(moduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{codeModule}")
    public ResponseEntity<ModuleSortieDTO> trouverModuleParCode(@PathVariable String codeModule) {
        ModuleSortieDTO result = metier.trouverModuleParCode(codeModule);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ModuleSortieDTO>> listerTousLesModules() {
        List<ModuleSortieDTO> result = metier.listerTousLesModules();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filiere/{codeFiliere}")
    public ResponseEntity<List<ModuleSortieDTO>> trouverModulesParFiliere(@PathVariable String codeFiliere) {
        List<ModuleSortieDTO> result = metier.trouverModulesParFiliere(codeFiliere);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{codeModule}")
    public ResponseEntity<ModuleSortieDTO> modifierModule(
            @PathVariable String codeModule, 
            @RequestBody ModuleModificationDTO moduleDTO) {
        ModuleSortieDTO result = metier.modifierModule(codeModule, moduleDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeModule}")
    public ResponseEntity<Void> supprimerModule(@PathVariable String codeModule) {
        metier.supprimerModule(codeModule);
        return ResponseEntity.noContent().build();
    }
}
