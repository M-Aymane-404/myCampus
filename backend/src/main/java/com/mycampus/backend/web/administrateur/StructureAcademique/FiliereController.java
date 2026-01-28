package com.mycampus.backend.web.administrateur.StructureAcademique;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.FiliereModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.FiliereSortieDTO;
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
@RequestMapping("/api/admin/structure/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final GestionStructureAcademiqueMetier metier;

    @PostMapping
    public ResponseEntity<FiliereSortieDTO> creerFiliere(@RequestBody @Valid FiliereEntrerDTO filiereDTO) {
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
            @RequestBody @Valid FiliereModificationDTO filiereDTO) {
        FiliereSortieDTO result = metier.modifierFiliere(codeFiliere, filiereDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{codeFiliere}")
    public ResponseEntity<Void> supprimerFiliere(@PathVariable String codeFiliere) {
        metier.supprimerFiliere(codeFiliere);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Fermer un filière")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "filière fermé"),
            @ApiResponse(responseCode = "404", description = "filière introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("{codeFiliere}/Fermer")
    public ResponseEntity<Void> FermerFiliere(@PathVariable String codeFiliere) {
        metier.FermerFiliere(codeFiliere);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Ouvrir un filière")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "filière Ouvert"),
            @ApiResponse(responseCode = "404", description = "filière introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("{codeFiliere}/Ouvrir")
    public ResponseEntity<Void> OuvrirFiliere(@PathVariable String codeFiliere) {
        metier.OuvrirFiliere(codeFiliere);
        return ResponseEntity.noContent().build();
    }
}
