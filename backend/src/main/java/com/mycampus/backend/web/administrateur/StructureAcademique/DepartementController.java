package com.mycampus.backend.web.administrateur.StructureAcademique;

import com.mycampus.backend.dto.administrateur.DonneEntrer.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.DepartementSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/departements")
@RequiredArgsConstructor
public class DepartementController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<DepartementSortieDTO> creerDepartement(@RequestBody DepartementEntrerDTO departementDTO) {
        DepartementSortieDTO result = metier.creerDepartement(departementDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{codeDepartement}")
    public ResponseEntity<DepartementSortieDTO> trouverDepartementParCode(@PathVariable String codeDepartement) {
        DepartementSortieDTO result = metier.trouverDepartementParCode(codeDepartement);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartementSortieDTO>> listerTousLesDepartements() {
        List<DepartementSortieDTO> result = metier.listerTousLesDepartements();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{codeDepartement}")
    public ResponseEntity<DepartementSortieDTO> modifierDepartement(
            @PathVariable String codeDepartement, 
            @RequestBody DepartementEntrerDTO departementDTO) {
        DepartementSortieDTO result = metier.modifierDepartement(codeDepartement, departementDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeDepartement}")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable String codeDepartement) {
        metier.supprimerDepartement(codeDepartement);
        return ResponseEntity.noContent().build();
    }
}
