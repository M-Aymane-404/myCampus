package com.mycampus.backend.web.etudiant.emploi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.etudiant.emploi.EmploiTempsEtudiantMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.repositorys.EtudiantRepository;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/etudiant/emploi")

@Slf4j
@Tag(name = "Emploi du Temps - Étudiant", description = "API pour l'emploi du temps étudiant")
@PreAuthorize("hasAuthority('ETUDIANT_TABLEAU_DE_BORD_LIRE')")
public class EmploiTempsEtudiantController {

    private final EmploiTempsEtudiantMetier emploiTempsEtudiantMetier;
    private final EtudiantRepository etudiantRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();


    @GetMapping(value = "/{semester}", produces = "application/json")
    @Operation(summary = "Obtenir l'emploi du temps", description = "Retourne l'emploi du temps de l'étudiant connecté selon sa classe et son semestre actuel.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Emploi du temps récupéré avec succès",
            content = @Content(schema = @Schema(implementation = JsonNode.class))),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
        @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    public ResponseEntity<String> obtenirEmploiDuTemps(@AuthenticationPrincipal CustomUserDetails user,
                                                       @PathVariable("semester")Semestre semestre
                                                       ) {
        
        log.info("Loading timetable for student ID: {}", user.getId());
        
        Etudiant etudiant = etudiantRepository.findByIdWithClasseAndFiliere(user.getId())
                .orElseThrow(() -> new UtilisateursException("Étudiant non trouvé"));
        
        if (etudiant.getClasse() == null) {
            log.warn("Student {} has no class assigned", user.getId());
            throw new UtilisateursException("L'étudiant n'est affecté à aucune classe");
        }
        
        if (etudiant.getSemestreActuel() == null) {
            log.warn("Student {} has no current semester", user.getId());
            throw new UtilisateursException("Le semestre actuel de l'étudiant n'est pas défini");
        }
        
        String codeClasse = etudiant.getClasse().getCodeClasse();

        
        log.info("Loading timetable for class: {}, semester: {}", codeClasse, semestre);

        String json = emploiTempsEtudiantMetier.chargerEmploiDuTempsEtudiant(codeClasse, semestre);
        return ResponseEntity.ok(json);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    @GetMapping(value = "/prochaine", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> obtenirProchaineSeance(@AuthenticationPrincipal CustomUserDetails user) {

        Etudiant etudiant = etudiantRepository.findByIdWithClasseAndFiliere(user.getId())
                .orElseThrow(() -> new UtilisateursException("Étudiant non trouvé"));

        if (etudiant.getClasse() == null) {
            throw new UtilisateursException("L'étudiant n'est affecté à aucune classe");
        }
        if (etudiant.getSemestreActuel() == null) {
            throw new UtilisateursException("Le semestre actuel de l'étudiant n'est pas défini");
        }

        String codeClasse = etudiant.getClasse().getCodeClasse();
        var semestre = etudiant.getSemestreActuel();

        JsonNode prochaineSeance = emploiTempsEtudiantMetier.getProchaineSeance(codeClasse, semestre);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("codeClasse", codeClasse);
        response.put("semestre", semestre.name());
        response.set("prochaineSeance", prochaineSeance == null ? objectMapper.nullNode() : prochaineSeance);

        return ResponseEntity.ok(response.toString());
    }
}



