package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;
import com.mycampus.backend.metier.etudiant.annonces.GestionAnnoncesEtudiantMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiant/annonces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Annonces - Étudiant", description = "API pour la consultation des annonces par les étudiants")
@PreAuthorize("hasAuthority('ETUDIANT_ANNONCES_LIRE')")
public class AnnoncesEtudiantController {

    private final GestionAnnoncesEtudiantMetier gestionAnnoncesEtudiantMetier;

    @GetMapping
    @Operation(summary = "Lister les annonces visibles", description = "Récupère la liste des annonces visibles pour l'étudiant connecté: annonces globales et annonces de sa classe.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonces récupérées avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
        @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    public ResponseEntity<List<AnnonceSortieEtudiantDTO>> listerAnnoncesVisibles(@AuthenticationPrincipal CustomUserDetails user) {
         List<AnnonceSortieEtudiantDTO> result = gestionAnnoncesEtudiantMetier.listerAnnoncesVisibles(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trouver une annonce par ID", description = "Récupère une annonce spécifique si elle est visible pour l'étudiant connecté.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonce récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée ou non accessible"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<AnnonceSortieEtudiantDTO> trouverAnnonceParId(
            @Parameter(description = "ID de l'annonce") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        
         AnnonceSortieEtudiantDTO result = gestionAnnoncesEtudiantMetier.trouverAnnonceParId(id, user.getId());
        return ResponseEntity.ok(result);
    }
}
