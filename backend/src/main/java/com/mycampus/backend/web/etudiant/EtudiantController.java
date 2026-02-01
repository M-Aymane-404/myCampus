package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.metier.etudiant.StructureEtudiantMapper.GestionStructureEtudiantMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/etudiant")
@RequiredArgsConstructor
public class EtudiantController {

    private final GestionStructureEtudiantMetier metier;

    @GetMapping("/{id}")
    @Operation(summary = "Trouver un étudiant connecté par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    public ResponseEntity<EtudiantSortieEtudiantDTO> trouverEtudiantConnecter(@PathVariable Long id) {
        EtudiantSortieEtudiantDTO result = metier.trouverEtudiantConnecter(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/modules")
    @Operation(summary = "Lister les modules d'un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modules trouvés"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    public ResponseEntity<List<ModuleSortieEtudiantDTO>> trouverModulesDEtudiantConnecter(
            @PathVariable Long id,
            @RequestParam(required = false) Semestre semestre) {
        List<ModuleSortieEtudiantDTO> result;
        if (semestre != null) {
            result = metier.trouverModulesDEtudiantConnecterParSemester(id, semestre);
        } else {
            result = metier.trouverModulesDEtudiantConnecter(id);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/modules/{codeModule}/matieres")
    @Operation(summary = "Lister les matières d'un module pour un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matières trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant ou module introuvable")
    })
    public ResponseEntity<List<MatiereSortieEtudiantDTO>> trouverMatiereParCodeModule(
            @PathVariable Long id,
            @PathVariable String codeModule) {
        List<MatiereSortieEtudiantDTO> result = metier.trouverMatiereParCodeModule(id, codeModule);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/matieres/{codeMatiere}/seances")
    @Operation(summary = "Lister les séances d'une matière pour un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Séances trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant ou matière introuvable")
    })
    public ResponseEntity<List<SeanceSortieEtudiantDTO>> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(
            @PathVariable Long id,
            @PathVariable String codeMatiere,
            @RequestParam(required = false) TypeSeance typeSeance) {
        List<SeanceSortieEtudiantDTO> result = metier.trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(id, codeMatiere, typeSeance);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seances/{seanceId}/ressources")
    @Operation(summary = "Lister les ressources d'une séance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ressources trouvées"),
            @ApiResponse(responseCode = "404", description = "Séance introuvable")
    })
    public ResponseEntity<List<FichierSeanceSortieEtudiantDTO>> trouverRessourceD_uneSeance(@PathVariable Long seanceId) {
        List<FichierSeanceSortieEtudiantDTO> result = metier.trouverRessourceD_uneSeance(seanceId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/presences")
    @Operation(summary = "Lister les présences d'un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Présences trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    public ResponseEntity<List<PresenceSortieEtudiantDTO>> trouverToutPresenceDuEtudiantConnecter(
            @PathVariable Long id,
            @RequestParam(required = false) StatusAbsence status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PresenceSortieEtudiantDTO> result = metier.trouverToutPresenceDuEtudiantConnecter(id, status, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/releve-notes")
    @Operation(summary = "Obtenir le relevé de notes d'un étudiant par semestre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relevé de notes trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable"),
            @ApiResponse(responseCode = "400", description = "Paramètre semestre manquant")
    })
    public ResponseEntity<ReleveDeNoteEtudiantParSessionDTO> trouverLeReleverDeNoteParSessionDuEtudiant(
            @PathVariable Long id,
            @RequestParam Semestre semestre) {
        ReleveDeNoteEtudiantParSessionDTO result = metier.trouverLeReleverDeNoteParSessionDuEtudiant(id, semestre);
        return ResponseEntity.ok(result);
    }
}
