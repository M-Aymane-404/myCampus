package com.mycampus.backend.web.administrateur.emploi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.administrateur.emploi.EmploiTempsMetier;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/etudiant/emploi")

@Slf4j
@Tag(name = "Emploi du Temps - Admin", description = "API pour l'emploi du temps ")
@PreAuthorize("hasAuthority('ADMIN_EMPLOI_GERER')")
public class EmploiTempsController {

    private final EmploiTempsMetier emploiTempsMetier;


    @GetMapping(value = "/{codeClasse}/{semester}", produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Emploi du temps récupéré avec succès",
            content = @Content(schema = @Schema(implementation = JsonNode.class))),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
        @ApiResponse(responseCode = "404", description = "Admin non trouvé")
    })
    public ResponseEntity<String> obtenirEmploiDuTemps(
                                                       @PathVariable("semester")Semestre semestre,
                                                       @PathVariable("codeClasse")String codeClasse

                                                       ) {
        


        String json = emploiTempsMetier.chargerEmploiDuTemps(codeClasse, semestre);
        return ResponseEntity.ok(json);
    }


}



