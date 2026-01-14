package com.mycampus.backend.web.administrateur.StructureAcademique;

import com.mycampus.backend.dto.administrateur.DonneEntrer.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ClasseSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/structure/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<ClasseSortieDTO> creerClasse(@RequestBody ClasseEntrerDTO classeDTO) {
        ClasseSortieDTO result = metier.creerClasse(classeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClasseSortieDTO> modifierClasse(
            @PathVariable Long id, 
            @RequestBody ClasseEntrerDTO classeDTO) {
        ClasseSortieDTO result = metier.modifierClasse(id, classeDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClasseSortieDTO> trouverClasseParId(@PathVariable Long id) {
        ClasseSortieDTO result = metier.trouverClasseParId(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ClasseSortieDTO>> listerToutesLesClasses() {
        List<ClasseSortieDTO> result = metier.listerToutesLesClasses();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filiere/{filiereId}")
    public ResponseEntity<List<ClasseSortieDTO>> trouverClassesParFiliere(@PathVariable Long filiereId) {
        List<ClasseSortieDTO> result = metier.trouverClassesParFiliere(filiereId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerClasse(@PathVariable Long id) {
        metier.supprimerClasse(id);
        return ResponseEntity.noContent().build();
    }
}
