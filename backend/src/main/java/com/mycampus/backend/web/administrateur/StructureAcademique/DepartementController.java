package com.mycampus.backend.web.administrateur.StructureAcademique;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.DepartementModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.DepartementSortieDTO;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
    public ResponseEntity<DepartementSortieDTO> creerDepartement(@RequestBody @Valid DepartementEntrerDTO departementDTO) {
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
            @RequestBody @Valid DepartementModificationDTO departementDTO) {
        DepartementSortieDTO result = metier.modifierDepartement(codeDepartement, departementDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeDepartement}")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable String codeDepartement) {
        metier.supprimerDepartement(codeDepartement);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Fermer un département")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Département fermé"),
            @ApiResponse(responseCode = "404", description = "Département introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("{codeDepartement}/Fermer")
    public ResponseEntity<Void> FermerDepartement(@PathVariable String codeDepartement) {
        metier.FermerDepartement(codeDepartement);
        return ResponseEntity.noContent().build();
    }



    @Operation(summary = "Ouvrir un département")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Département Ouvert"),
            @ApiResponse(responseCode = "404", description = "Département introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("{codeDepartement}/Ouvrir")
    public ResponseEntity<Void> OuvrirDepartement(@PathVariable String codeDepartement) {
        metier.OuvrirDepartement(codeDepartement);
        return ResponseEntity.noContent().build();
    }
}
