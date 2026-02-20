package com.mycampus.backend.web.administrateur.imports;

import com.mycampus.backend.dto.administrateur.imports.RapportImportEtudiantsSortieDTO;
import com.mycampus.backend.enumerations.ModeImport;
import com.mycampus.backend.metier.administrateur.imports.GestionImportEtudiantsMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/etudiants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Import Étudiants - Administrateur", description = "API pour l'import en masse des étudiants")
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class ImportEtudiantsController {

    private final GestionImportEtudiantsMetier gestionImportEtudiantsMetier;

    @PostMapping(value = "/import-fichier", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des étudiants depuis un fichier", description = "Importe des étudiants en masse depuis un fichier CSV, XLSX ou XLS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import réussi",
            content = @Content(schema = @Schema(implementation = RapportImportEtudiantsSortieDTO.class))),
        @ApiResponse(responseCode = "400", description = "Fichier invalide ou données incorrectes"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<RapportImportEtudiantsSortieDTO> importerEtudiants(

            @Parameter(description = "Code de la classe", required = true) @RequestParam String codeClasse,
            @Parameter(description = "Fichier à importer (CSV, XLSX ou XLS)", required = true) @RequestPart("fichier") MultipartFile fichier,
            @Parameter(description = "Mode d'import: PARTIEL ou ATOMIC" +
                    "PARTIEL : insère les lignes valides uniquement. " +
                    "ATOMIC : annule tout l’import si une erreur est détectée.") @RequestParam(defaultValue = "PARTIEL") ModeImport modeImport,
            @Parameter(description = "Mot de passe par défaut (optionnel)") @RequestParam(required = false) String motDePasseDefaut

    ) {
        
        log.info("Demande d'import étudiants: classe={}, fichier={}, mode={}", codeClasse, fichier.getOriginalFilename(), modeImport);
        
        RapportImportEtudiantsSortieDTO result = gestionImportEtudiantsMetier.importerEtudiants(
                fichier, codeClasse, modeImport, motDePasseDefaut
        );
        
        log.info("Import terminé: {} lignes, {} insérés, {} ignorés, {} erreurs", 
                result.getTotalLignes(), result.getNombreInseres(), result.getNombreIgnores(), result.getNombreErreurs());
        
        return ResponseEntity.ok(result);
    }
}
