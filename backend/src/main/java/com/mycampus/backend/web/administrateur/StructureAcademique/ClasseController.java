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

    @GetMapping("/{codeClasse}")
    public ResponseEntity<ClasseSortieDTO> trouverClasseParCode(@PathVariable String codeClasse) {
        ClasseSortieDTO result = metier.trouverClasseParCode(codeClasse);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ClasseSortieDTO>> listerToutesLesClasses() {
        List<ClasseSortieDTO> result = metier.listerToutesLesClasses();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filiere/{codeFiliere}")
    public ResponseEntity<List<ClasseSortieDTO>> trouverClassesParFiliere(@PathVariable String codeFiliere) {
        List<ClasseSortieDTO> result = metier.trouverClassesParFiliere(codeFiliere);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{codeClasse}")
    public ResponseEntity<ClasseSortieDTO> modifierClasse(
            @PathVariable String codeClasse, 
            @RequestBody ClasseEntrerDTO classeDTO) {
        ClasseSortieDTO result = metier.modifierClasse(codeClasse, classeDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeClasse}")
    public ResponseEntity<Void> supprimerClasse(@PathVariable String codeClasse) {
        metier.supprimerClasse(codeClasse);
        return ResponseEntity.noContent().build();
    }
}
