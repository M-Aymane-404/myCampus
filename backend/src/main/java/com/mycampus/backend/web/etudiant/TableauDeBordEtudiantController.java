package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.etudiant.tableauDeBord.TableauDeBordEtudiantSortieDTO;
import com.mycampus.backend.metier.etudiant.tableauDeBord.TableauDeBordEtudiantMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant/tableau-de-bord")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tableau de Bord - Étudiant", description = "API pour le tableau de bord étudiant")
@PreAuthorize("hasAuthority('ETUDIANT_TABLEAU_DE_BORD_LIRE')")
public class TableauDeBordEtudiantController {

    private final TableauDeBordEtudiantMetier tableauDeBordEtudiantMetier;

    @GetMapping
    @Operation(summary = "Obtenir le tableau de bord", description = "Retourne le tableau de bord complet de l'étudiant: en-tête, dernières notes, et assiduité.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tableau de bord récupéré avec succès",
            content = @Content(schema = @Schema(implementation = TableauDeBordEtudiantSortieDTO.class))),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
        @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    public ResponseEntity<TableauDeBordEtudiantSortieDTO> obtenirTableauDeBord(
            Authentication authentication) {
        
        String emailEtudiant = authentication.getName();
        log.info("Demande du tableau de bord pour l'étudiant: {}", emailEtudiant);
        
        TableauDeBordEtudiantSortieDTO result = tableauDeBordEtudiantMetier.obtenirTableauDeBord(emailEtudiant);
        return ResponseEntity.ok(result);
    }
}
