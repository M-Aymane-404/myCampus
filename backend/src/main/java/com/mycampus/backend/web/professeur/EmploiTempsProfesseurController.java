package com.mycampus.backend.web.professeur;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.professeur.emploi.EmploiTempsProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for professor timetable management
 */
@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emploi du Temps - Professeur", description = "API pour l'emploi du temps professeur")
@PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_LIRE')")
public class EmploiTempsProfesseurController {

    private final EmploiTempsProfesseurMetier emploiTempsProfesseurMetier;
    private final ProfesseurRepository professeurRepository;


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Professeur non trouvé")
    })
    @GetMapping(value="/emploi", produces = "application/json")
    public ResponseEntity<String> obtenirEmploiDuTemps(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Semestre semestre) {

        Professeur prof = professeurRepository.findById(user.getId())
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));

        String ppr = prof.getPpr();
        String json = emploiTempsProfesseurMetier.chargerEmploiTempsProfesseur(ppr, semestre);
        return ResponseEntity.ok(json);
    }
}
